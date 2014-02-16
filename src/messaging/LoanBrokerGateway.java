/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
import creditbureau.CreditReply;
import creditbureau.CreditSerializer;
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

    protected ClientSerializer clientSerializer; // for serializing ClientRequest and ClientReply to/from XML
    protected CreditSerializer creditSerializer; // for serializing CreditRequest and CreditReply to/from XML
            
    private MessagingGateway gtw;

    public LoanBrokerGateway(String factoryName, String requestQueue, String replyQueue) {
        clientSerializer = new ClientSerializer();
        creditSerializer = new CreditSerializer();
        
        gtw = new MessagingGateway(factoryName, requestQueue, replyQueue);
        
        gtw.setListener(new MessageListener() {
            
            public void onMessage(Message msg) {
                try {
                    System.out.println("LoanBrokerGateway got message");
                    TextMessage message = (TextMessage) msg;
                    String messageText = message.getText();
                    System.out.println("received message : " + messageText);
                    if(messageText.contains("ClientReply")){
                        System.out.println("client reply");
                        Reply r = clientSerializer.replyFromString(messageText);
                        onRecievedReply(r);
                    } else if(messageText.contains("CreditRequest")){
                        System.out.println("credit request");
                        Request r = creditSerializer.requestFromString(messageText);
                        onRecievedRequest(r);
                    }
                    
                } catch (JMSException ex) {
                    System.err.println("Error in LoanBrokerGateway listener onMessage while processing received message from messaging gateway");
                    System.err.println(String.format("Error message: %s", ex.getMessage()));
                } 
            }
        });
    }
    
    public void start(){
        gtw.start();
    }

    public void applyForLoan(ClientRequest r) {
        System.out.println("sending request to loanBroker");
        String serR = clientSerializer.requestToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }
    
    public void sendCreditReply(CreditReply r){
        System.out.println("send reply?");
        String serR = creditSerializer.replyToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }

    public abstract void onRecievedReply(Reply r);
    
    public abstract void onRecievedRequest(Request r);
}
