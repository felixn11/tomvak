/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Message;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

/**
 *
 * @author user
 */
public abstract class ClientGateway {
    
    protected ClientSerializer clientSerializer;
    private AsynchronousReplier asyncReplier;
    
    public ClientGateway(String clientRequestQueue){
        try {
            clientSerializer = new ClientSerializer();
            asyncReplier = new AsynchronousReplier("queueConnectionFactory", clientRequestQueue, clientSerializer) {

                @Override
                public Message beforeReply(Message request, Message reply) {
                    return reply;
                }
            };
            asyncReplier.setRequestListener(new IRequestListener() {

                public void receivedRequest(Object request) {
                    receivedLoanRequest((ClientRequest)request);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ClientGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void start(){
        asyncReplier.start();
    }
    
    public void sendClientReply(ClientRequest request, ClientReply reply){
        asyncReplier.sendReply(request, reply);
    }
    
    public abstract void receivedLoanRequest(ClientRequest r);    
}
