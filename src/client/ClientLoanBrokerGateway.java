/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import messaging.Reply;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;
import messaging.requestreply.IRequestReplySerializer;

/**
 *
 * @author user
 */
public abstract class ClientLoanBrokerGateway {

    private IRequestReplySerializer clientSerializer;

    private AsynchronousRequestor asyncRequestor;
    
    private IReplyListener listener;

    public ClientLoanBrokerGateway(String factoryName, String requestQueue, String replyQueue) {
        try {
            clientSerializer = new ClientSerializer();
            asyncRequestor = new AsynchronousRequestor(factoryName, requestQueue, replyQueue, clientSerializer);
        } catch (Exception ex) {
            Logger.getLogger(ClientLoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void start(){
        asyncRequestor.start();
    }
    
    public void applyForLoan(ClientRequest r) {
        listener = new IReplyListener() {
                public void onReply(Object request, Object reply) {
                    onRecievedReply((ClientRequest)request, (ClientReply)reply);
                }
        };
        asyncRequestor.sendRequest(r, listener);
    }
    
    public abstract void onRecievedReply(ClientRequest req, ClientReply rep);
}
