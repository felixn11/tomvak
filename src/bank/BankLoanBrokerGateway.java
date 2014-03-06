/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bank;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import javax.jms.Message;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;
import messaging.requestreply.IRequestReplySerializer;

/**
 *
 * @author user
 */
public abstract class BankLoanBrokerGateway {
    private IRequestReplySerializer bankSerializer;
    private AsynchronousReplier asyncReplier;

    public BankLoanBrokerGateway(String factoryName, String requestRecieverQueue) {
        try {
            bankSerializer = new BankSerializer();
            asyncReplier = new AsynchronousReplier(factoryName, requestRecieverQueue, bankSerializer);
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
    
    public void sendBankQuoteReply(BankQuoteRequest request, BankQuoteReply reply) {
        asyncReplier.sendReply(request, reply);
    }
    
    public abstract void onRecievedRequest(Object r);
}
