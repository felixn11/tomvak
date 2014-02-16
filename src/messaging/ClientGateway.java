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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author user
 */
public abstract class ClientGateway {
    protected ClientSerializer clientSerializer;
    
    private MessagingGateway gtw;
    
    public ClientGateway(String factoryName, String clientRequestQueue, String clientReplyQueue){
        clientSerializer = new ClientSerializer();
        
        //clientReplyQueue and clientRequestQueue are swapped, because the request serves a reply
        gtw = new MessagingGateway(factoryName, clientReplyQueue, clientRequestQueue);
        
        gtw.setListener(new MessageListener() {

            public void onMessage(Message msg) {
                try {
                    TextMessage message = (TextMessage) msg;
                    String messageText = message.getText();
                    ClientRequest r = clientSerializer.requestFromString(messageText);
                    onRecievedRequest(r);
                } catch (JMSException ex) {
                    Logger.getLogger(ClientGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void start(){
        gtw.start();
    }
    
    public void sendClientReply(ClientReply r){
        String serR = clientSerializer.replyToString(r);
        Message msg = gtw.createMsg(serR);
        gtw.send(msg);
    }
    
    public abstract void onRecievedRequest(ClientRequest r);    
}
