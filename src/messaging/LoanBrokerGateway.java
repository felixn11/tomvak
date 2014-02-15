/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Responsible for converting the application request to a JMS message, this
 * message will be send by using the MessagingGateway
 *
 * Also responsible for converting a received JMS message to an application
 * request this message will be passed by the MessagingGateway, converted and
 * passed to the application
 *
 * @author user
 */
public abstract class LoanBrokerGateway {

    protected ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML
    private MessagingGateway gtw;

    public LoanBrokerGateway(String factoryName, String requestQueue, String replyQueue) {
        serializer = new ClientSerializer();
        gtw = new MessagingGateway(factoryName, requestQueue, replyQueue);
        
        gtw.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {                    
                    TextMessage message = (TextMessage) msg;
                    ClientReply r = serializer.replyFromString(message.getText());
                    onLoanOffer(r);
                } catch (JMSException ex) {
                    System.err.println("Error in LoanBrokerGateway listener onMessage while processing received message from messaging gateway");
                    System.err.println(String.format("Error message: %s", ex.getMessage()));
                }
            }
        });
    }

    public void applyForLoan(ClientRequest r) {
        String serR = serializer.requestToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }

    public abstract void onLoanOffer(ClientReply r);
}
