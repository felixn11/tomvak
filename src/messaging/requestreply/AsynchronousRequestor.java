package messaging.requestreply;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Hashtable;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import messaging.MessagingGateway;

/**
 * This class is used for sending requests and receiving replies
 * in asynchronous communication.This class inherits the MessagingGateway,
 * i.e., it has access to a MessageSender and MessageReceiver.
 * @param <REQUEST> is the domain class for requests
 * @param <REPLY> is the domain class for replies
 * @author Maja Pesic
 */
public class AsynchronousRequestor<REQUEST, REPLY> {
    /**
     * Class Pair is just used to make it possible to store
     * pairs of REQUEST, ReplyListener in a hashtable!
     */
    private class Pair {

        private IReplyListener<REQUEST, REPLY> listener;
        private REQUEST request;
        private String correlationID;

        private Pair(IReplyListener<REQUEST, REPLY> listener, REQUEST request) {
            this.listener = listener;
            this.request = request;
        }
        
        public void setCorrelationID(String corID){
            this.correlationID = corID;
        }
        
        public String getCorrelationID(){
            return correlationID;
        }
    }
    
    /**
     * For sending and receiving messages
     */
    private MessagingGateway gateway;

    /**
     * contains registered reply listeners for each sent request
     */
    private Hashtable<String, Pair> listeners;
    /**
     * used for serialization of requests and replies
     */
    private final IRequestReplySerializer<REQUEST, REPLY> serializer;
    
    private Destination requestSenderDest;
    private Destination replyReceiverDest;

    /**
     * The only constructor. This constructor does the following:
     * 1. creates the serializer and listener.
     * 2. registeres itself as the listener on the MessageReceiver (method onReply)
     * @param factoryName
     * @param requestSenderQueue
     * @param replyReceiverQueue
     * @param serializer
     * @throws java.lang.Exception
     */
    public AsynchronousRequestor(String factoryName, String requestSenderQueue, String replyReceiverQueue, final IRequestReplySerializer<REQUEST, REPLY> serializer) throws Exception {
        super();
        this.serializer = serializer;
        this.listeners = new Hashtable<String, Pair>();
        
        // connecting to the JMS
        Context jndiContext = new InitialContext();
        requestSenderDest = (Destination) jndiContext.lookup(requestSenderQueue);
        replyReceiverDest = (Destination) jndiContext.lookup(replyReceiverQueue);

        gateway = new MessagingGateway(replyReceiverDest);
        gateway.setListener(new MessageListener() {

            public void onMessage(Message msg) {
                onReply((TextMessage)msg);
            }
        });
    }

    /**
     * Opens JMS connection in order to be able to send messages and to start
     * receiving messages.
     */
    public void start() {
        gateway.start();
    }

    /**
     * @todo implement this method!
     * Sends one request. Immediately, a listener is registered for this request.
     * This listener will be notified when (later) a reply for this request arrives.
     * This method should:
     * 1. create a Message for the request (use serializer).
     * 2. set the JMSReplyTo of the Message to be the Destination of the gateway's receiver.
     * 3. send the Message
     * 4. register the listener to belong to the JMSMessageID of the request Message
     * 
     * @param request is the request object (a domain class) to be sent
     * @param listener is the listener that will be notified when the reply arrives for this request
     */
    public synchronized void sendRequest(REQUEST request, IReplyListener<REQUEST, REPLY> listener) {
        try {
            String message = serializer.requestToString(request);
            Message msg = gateway.createMsg(message);
            
            msg.setJMSReplyTo(replyReceiverDest);
            
            //System.out.println(String.format("registered request: {%s}, {%s}, {%s}", requestID, listener.toString(), request.toString()));
            gateway.send(msg, requestSenderDest);
            
            String requestID = msg.getJMSMessageID();
            listeners.put(requestID, new Pair(listener, request));
        } catch (JMSException ex) {
            System.err.println(String.format("JMSException in AsynchronuosRequest.sendReqest() : %s", ex.getMessage()));
        }
    }

    /**
     * @todo implement this method!
     * This method is invoked for processing of a single reply when it arrives.
     * This method should be registered on the MessageReceiver.
     * This method should:
     * 1. get the registered listener for the JMSCorrelationID of the Message
     * 2. de-serialize the REPLY from the Message
     * 3. notify the listener about the arrival of the REPLY
     * 4. unregister the listener
     * @param message the reply message
     */
    private synchronized void onReply(TextMessage message) {
        try {
            String messageID = message.getJMSCorrelationID();
            REPLY replyMes = serializer.replyFromString(message.getText());
            
            Pair pair = listeners.get(messageID);
            
            pair.listener.onReply(pair.request, replyMes);
            listeners.remove(messageID);
            
        } catch (JMSException ex) {
            System.err.println(String.format("JMS Exception in AsynchronuosRequestor.onReply() : %s", ex.getMessage()));
        } catch (NullPointerException nex){
            System.err.println(String.format("Null Pointer in AsynchronuosRequestor.onReply(): %s " , nex.toString()));
        }
    }
}
