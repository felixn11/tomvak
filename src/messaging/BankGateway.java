/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author user
 */
public class BankGateway {
    protected BankSerializer serializer;   
    
    private AsynchronousRequestor asyncRequestor;
    
    public BankGateway(String bankRequestQueue, String bankReplyQueue){
        try {
            serializer = new BankSerializer();
            asyncRequestor = new AsynchronousRequestor("queueConnectionFactory", bankRequestQueue, bankReplyQueue, serializer);
        } catch (Exception ex) {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendBankRequest(BankQuoteRequest r, IReplyListener l){
        asyncRequestor.sendRequest(r, l);
    }
    
    public void start(){
        asyncRequestor.start();
    }
}
