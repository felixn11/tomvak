/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import client.*;
import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import loanbroker.gui.LoanBrokerFrame;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {

    /**
     *  Connection to the JMS
     */
    private Connection connection; // JMS connection
    protected Session session; // JMS session
      /*
     * Connection to the TestClient
     */
    private MessageProducer clientProducer; // for sending messages to the Client
    private MessageConsumer clientConsumer; // for receiving messages from the Client
    private ClientSerializer clientSerializer; // serializer ClientRequest ClientReply to/from XML:
    /*
     * Connection to the CreditBuerau
     */
    private MessageProducer creditProducer; // for sending messages to the CreditBuerau
    private MessageConsumer creditConsumer; // for receiving messages from the CreditBuerau
    private CreditSerializer creditSerializer; // serializer CreditRequest CreditReply to/from XML:

    /*
     * Connection to the Bank
     */
    private MessageProducer bankProducer; // for sending messages to the Bank
    private MessageConsumer bankConsumer; // for receiving messages from the Bank
    private BankSerializer bankSerializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:


    private LoanBrokerFrame frame; // GUI

    /**
     * Intializes attributes, and registers itself (method onClinetRequest) as
     * the listener for new client requests
     * @param factoryName
     * @param clientRequestQueue
     * @param clientReplyQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankRequestQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String factoryName, String clientRequestQueue, String clientReplyQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception {
        super();
        // connecting to the JMS 
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(factoryName);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        /*
         * setting up the LoanTestClient connection
         */
        Destination clientSenderDestination = (Destination) jndiContext.lookup(clientReplyQueue);
        clientProducer = session.createProducer(clientSenderDestination);
        Destination clientReceiverDestination = (Destination) jndiContext.lookup(clientRequestQueue);
        clientConsumer = session.createConsumer(clientReceiverDestination);
        clientConsumer.setMessageListener(new MessageListener() {

            public void onMessage(Message msg) {
                onClientRequest((TextMessage) msg);
            }
        });
        clientSerializer = new ClientSerializer();

        /*
         * setting up the CreditBureau connection
         */
        Destination creditSenderDestination = (Destination) jndiContext.lookup(creditRequestQueue);
        creditProducer = session.createProducer(creditSenderDestination);
        Destination creditReceiverDestination = (Destination) jndiContext.lookup(creditReplyQueue);
        creditConsumer = session.createConsumer(creditReceiverDestination);
        creditConsumer.setMessageListener(new MessageListener() {

            public void onMessage(Message msg) {
                onCreditReply((TextMessage) msg);
            }
        });
        creditSerializer = new CreditSerializer();


        /*
         * setting up the Bank connection
         */
        Destination bankSenderDestination = (Destination) jndiContext.lookup(bankRequestQueue);
        bankProducer = session.createProducer(bankSenderDestination);
        Destination bankReceiverDestination = (Destination) jndiContext.lookup(bankReplyQueue);
        bankConsumer = session.createConsumer(bankReceiverDestination);
        bankConsumer.setMessageListener(new MessageListener() {

            public void onMessage(Message msg) {
                onBankReply((TextMessage) msg);
            }
        });
        bankSerializer = new BankSerializer();
        /*
         * Make the GUI
         */
        frame = new LoanBrokerFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }

    /**
     * This method is called when a new client request arrives.
     * It generates a CreditRequest and sends it to the CreditBureau.
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(TextMessage message) {
        try {
            ClientRequest request = clientSerializer.requestFromString(message.getText()); // de-serialize ClientRequest from the message
            frame.addObject(null, request); // add the request to the GUI
            CreditRequest credit = createCreditRequest(request); // generate CreditRequest
            TextMessage creditRequestMesage = session.createTextMessage(creditSerializer.requestToString(credit)); // serialize CreditRequest into a message
            creditProducer.send(creditRequestMesage); // send the credit request message to the CreditBureau
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This method is called when a new credit reply arrives.
     * It generates a BankQuoteRequest and sends it to the Bank.
     * @param message the incomming message containng the CreditReply
     */
    private void onCreditReply(TextMessage msg) {
        try {
            CreditReply reply = creditSerializer.replyFromString(msg.getText()); // de-serialize CreditReply from the message
            frame.addObject(null, reply); // add the reply to the GUI
            BankQuoteRequest bank = createBankRequest(null, reply); // generate BankQuoteRequest
            TextMessage bankRequestMessage = session.createTextMessage(bankSerializer.requestToString(bank)); // serialize BankQuoteRequest into a message
            bankProducer.send(bankRequestMessage); // send the bank quote request message to the Bank
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This method is called when a new bank quote reply arrives.
     * It generates a ClientReply and sends it to the LoanTestClient.
     * @param message the incomming message containng the BankQuoteReply
     */
    private void onBankReply(TextMessage msg) {
        try {
            BankQuoteReply reply = bankSerializer.replyFromString(msg.getText()); // de-serialize CreditReply from the message
            frame.addObject(null, reply); // add the reply to the GUI
            ClientReply client = createClientReply(reply); // generate ClientReply
            TextMessage clientReplyMessage = session.createTextMessage(clientSerializer.replyToString(client)); // serialize ClientReply into a message
            clientProducer.send(clientReplyMessage);  // send the client reply message to the LoanTestClient
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Generates a credit request based on the given client request.
     * @param clientRequest
     * @return
     */
    private CreditRequest createCreditRequest(ClientRequest clientRequest) {
        return new CreditRequest(clientRequest.getSSN());
    }
    /**
     * Generates a bank quote reguest based on the given client request and credit reply.
     * @param creditReply
     * @return
     */
    private BankQuoteRequest createBankRequest(ClientRequest clientRequest, CreditReply creditReply) {
        int ssn = creditReply.getSSN();
        int score = creditReply.getCreditScore();
        int history = creditReply.getHistory();
        int amount = 100;
        int time = 24;
        if (clientRequest != null){
            amount = clientRequest.getAmount();
            time = clientRequest.getTime();
        }
        return  new BankQuoteRequest(ssn, score, history, amount, time);
    }
    /**
     * Generates a client reply based on the given bank quote reply.
     * @param creditReply
     * @return
     */
    private ClientReply createClientReply(BankQuoteReply reply) {
        return new ClientReply(reply.getInterest(), reply.getQuoteId());
    }

    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
