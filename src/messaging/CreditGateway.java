/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;
import messaging.requestreply.IRequestReplySerializer;

/**
 *
 * @author user
 */
public class CreditGateway {
    
    protected IRequestReplySerializer serializer;
    
    private AsynchronousRequestor asyncRequestor;
    
    private IReplyListener listener;
    
    public CreditGateway(String creditRequestQueue, String creditReplyQueue){
        try {
            serializer = new CreditSerializer();
            asyncRequestor = new AsynchronousRequestor("queueConnectionFactory", creditRequestQueue, creditReplyQueue, serializer);
        } catch (Exception ex) {
            Logger.getLogger(CreditGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendCreditRequest(CreditRequest r, IReplyListener list){
        this.listener = list;
        asyncRequestor.sendRequest(r, listener);
    }
    
    public void start(){
        asyncRequestor.start();
    } 

    public void setMessageListener(IReplyListener msl) {
        this.listener = msl;
    }
    
}
