/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.bank;

import bank.BankQuoteRequest;
import java.util.*;
import messaging.SenderMessagingGateway;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

/**
 * This class contains a collection of SenderGateways for the banks.
 * Given a secific request, it determmines to which banks the request has to be sent.
 * @author Maja Pesic
 */
class BanksSenderRouter {

    /**
     * The list of senders of all participating banks.
     */
    private ArrayList<BankSender> banks;
   
    /**
     * The only constructor.
     * @param connectionName for JMS
     * @throws NamingException
     * @throws JMSException
     */
    public BanksSenderRouter() throws Exception {
        super();
        banks = new ArrayList<BankSender>();
    }

    void addBank(String factory, String destination, String expression) {
        try {
            banks.add(new BankSender(factory, destination, expression));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Iterable<SenderMessagingGateway> getEligibleBanks(BankQuoteRequest request) {
        ArrayList<SenderMessagingGateway> e = new ArrayList<SenderMessagingGateway>();
        for (BankSender bank : banks) {
            if (bank.canHandleRequest(request)) {
                e.add(bank.getSender());
            }
        }
        return e;
    }

    void openConnection() {
        for (BankSender bank : banks) {
            bank.getSender().openConnection();
        }
    }

    /**
     * This class represents the sender to ONE bank.
     */
    private class BankSender {
/*
     one sender for one bank, you can either:
      - use existing class MessagingGateway and use it only for sending, or
     - create a new class SenderMessagingGateway that only can send, and can not receive 
*/
        private SenderMessagingGateway sender;

        private final String expression;
        private final Evaluator evaluator;

        BankSender(String factoryName, String destinationName, String expression) throws Exception {
            super();
            sender = new SenderMessagingGateway(destinationName);
            this.expression = expression;
            this.evaluator = new Evaluator();
        }

        /**
         * returns whether this bank is willing to handle this loan request.
         * @param request
         * @return
         */
        boolean canHandleRequest(BankQuoteRequest request) {
            if (expression != null) {
                try {
                    evaluator.putVariable("amount", Integer.toString(request.getAmount()));
                    evaluator.putVariable("credit", Integer.toString(request.getCredit()));
                    evaluator.putVariable("history", Integer.toString(request.getHistory()));

                    String result = evaluator.evaluate(expression);
                    return result.equals("1.0");
                } catch (EvaluationException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                return true;
            }
        }

        SenderMessagingGateway getSender(){
            return sender;
        }
    }
}
