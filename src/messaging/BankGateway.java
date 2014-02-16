/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import client.ClientRequest;
import creditbureau.CreditReply;
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
public abstract class BankGateway {
    
    protected BankSerializer serializer;
    
    private MessagingGateway gtw;
    
    public BankGateway(String factoryName, String bankRequestQueue, String bankReplyQueue){
        serializer = new BankSerializer();
        
        gtw = new MessagingGateway(factoryName, bankRequestQueue, bankReplyQueue);
        
        gtw.setListener(new MessageListener() {

            public void onMessage(Message msg) {
                try {
                    TextMessage message = (TextMessage) msg;
                    String messageText = message.getText();
                    BankQuoteReply r = serializer.replyFromString(messageText);
                    onRecievedReply(r);
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void sendBankRequest(BankQuoteRequest r){
        String serR = serializer.requestToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }
    
    public void start(){
        gtw.start();
    }
    
    public abstract void onRecievedReply(BankQuoteReply r);  
}
