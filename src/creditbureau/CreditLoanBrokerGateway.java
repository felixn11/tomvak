/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package creditbureau;

import javax.jms.Message;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;
import messaging.requestreply.IRequestReplySerializer;

/**
 *
 * @author user
 */
public abstract class CreditLoanBrokerGateway {
    private IRequestReplySerializer creditSerializer;
    private AsynchronousReplier asyncReplier;

    public CreditLoanBrokerGateway(String factoryName, String requestRecieverQueue) {
        try {
            creditSerializer = new CreditSerializer();
            asyncReplier = new AsynchronousReplier(factoryName, requestRecieverQueue, creditSerializer) {

                @Override
                public Message beforeReply(Message request, Message reply) {
                    return reply;
                }
            };
            asyncReplier.setRequestListener(new IRequestListener() {

                public void receivedRequest(Object request) {
                    onRecievedRequest(request);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void start(){
        asyncReplier.start();
    }
    
    public void sendCreditReply(CreditRequest request, CreditReply reply) {
        if(!asyncReplier.sendReply(request, reply)){
            System.err.println("@@@ Error send reply in CreditLoanBrokerGateway");
        }
    }
    
    public abstract void onRecievedRequest(Object r);
}
