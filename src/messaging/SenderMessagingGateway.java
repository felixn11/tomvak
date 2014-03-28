/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author user
 */
public class SenderMessagingGateway {
    
    /*
     * Connection to JMS
     */
    private Connection connection; // to connect to the JMS
    protected Session session; // session for making messages, producers and consumers

    private MessageProducer producer; // for sending messages
    private MessageConsumer consumer; // for receiving messages
    
    private String factoryName = "queueConnectionFactory";
    
    public SenderMessagingGateway(String destinationName){
        try {
            // connect to the sender channel
            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(factoryName);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = getDestination(destinationName);
            producer = session.createProducer(destination);
            
        } catch (JMSException ex) {
            System.err.println(String.format("JMSException in MessagingGateway constructor: %s", ex.getMessage()));
        } catch (NamingException ex) {
            System.err.println(String.format("JMSException in MessagingGateway constructor: %s", ex.getMessage()));
        }
    }
    
    public static Destination getDestination(String destinationName){
        Destination requestorDestination = null;
        try {
            Context jndiContext = new InitialContext();
            requestorDestination = (Destination) jndiContext.lookup(destinationName);
        } catch (NamingException ex) {
            System.err.println(String.format("JMSException in MessagingGateway constructor: %s", ex.getMessage()));
        }
        return requestorDestination;
    }
    
    public void openConnection() {
        try {
            connection.start();
        } catch (JMSException ex) {
            System.err.println(String.format("JMSException in MessagingGateway start() : %s", ex.getMessage()));
        }
    }
    
    public Message createMsg(String body){
        Message msg = null;
        try {
            msg = session.createTextMessage(body);
        } catch (JMSException ex) {
            System.err.println(String.format("JMSException in MessagingGateway createMsg() : %s", ex.getMessage()));
        }
        return msg;
    }
    
    public void send(Message msg){
        try {            
            producer.send(msg);
        } catch (JMSException ex) {
           System.err.println(String.format("JMSException in MessagingGateway send(Message msg, Destination requestorDestination) : \n %s", ex.getMessage()));
        }
    }

    public void send(Message msg, Destination requestorDestination) {
        try {            
            producer.send(requestorDestination, msg);
        } catch (JMSException ex) {
           System.err.println(String.format("JMSException in MessagingGateway send(Message msg, Destination requestorDestination) : \n %s", ex.getMessage()));
        }
    }
    
    public void send(Message msg, String requestorDestination) {
        try {            
            producer.send(getDestination(requestorDestination), msg);
        } catch (JMSException ex) {
           System.err.println(String.format("JMSException in MessagingGateway send(Message msg, Destination requestorDestination) : \n %s", ex.getMessage()));
        }
    }
}