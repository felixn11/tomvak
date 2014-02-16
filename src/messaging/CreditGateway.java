/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author user
 */
public abstract class CreditGateway {
    
    protected CreditSerializer serializer;
    
    private MessagingGateway gtw;
    
    public CreditGateway(String factoryName, String creditRequestQueue, String creditReplyQueue){
        serializer = new CreditSerializer();
        
        gtw = new MessagingGateway(factoryName, creditRequestQueue, creditReplyQueue);
        
        gtw.setListener(new MessageListener() {

            public void onMessage(Message msg) {
                try {
                    TextMessage message = (TextMessage) msg;
                    String messageText = message.getText();
                    CreditReply r = serializer.replyFromString(messageText);
                    onRecievedReply(r);
                } catch (JMSException ex) {
                    Logger.getLogger(CreditGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void sendCreditRequest(CreditRequest r){
        String serR = serializer.requestToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }
    
    public void start(){
        gtw.start();
    }
    
    public abstract void onRecievedReply(CreditReply r);    
    
}
