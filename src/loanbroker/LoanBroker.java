/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import client.*;
import creditbureau.CreditReply;
import java.util.ArrayList;
import loanbroker.gui.LoanBrokerFrame;
import messaging.BankGateway;
import messaging.ClientGateway;
import messaging.CreditGateway;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {

    private ClientGateway clientGateway;
    private CreditGateway creditGateway;
    private BankGateway bankGateway;
    /**
     *  the collection of active clientRequests
     */
    private ArrayList<ClientRequestProcess> activeClientProcesses;
    private LoanBrokerFrame frame;

    /**
     * Intializes attributes, and registers itself (method onClientRequest) as
     * the listener for new client requests
     * @param connectionName
     * @param clientRequestQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankRequestQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String clientRequestQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception{
        super();
        frame = new LoanBrokerFrame();
        activeClientProcesses = new ArrayList<ClientRequestProcess>();
        clientGateway = new ClientGateway(clientRequestQueue) {

            @Override
            public void receivedLoanRequest(ClientRequest request) {
                onClientRequest(request);
            }
        };

        creditGateway = new CreditGateway(creditRequestQueue, creditReplyQueue);
        bankGateway = new BankGateway(bankRequestQueue,bankReplyQueue);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                frame.setVisible(true);
            }
        });
    }

    /**
     * When a new client reques arrives:
     * 1. a new instance of ClientRequestProcess is created for this request,
     * 2. method  notifyFinished is implemented to remove the process from activeClientProcesses
     * 3. the new process is added to activeClientProcesses
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(ClientRequest request) {
        final ClientRequestProcess p = new ClientRequestProcess(request, creditGateway, clientGateway, bankGateway) {

            @Override
            void notifySentClientReply(ClientRequestProcess process) {
                activeClientProcesses.remove(process);
            }

            @Override
            void notifyReceivedCreditReply(ClientRequest clientRequest, CreditReply reply) {
               frame.addObject(clientRequest, reply);
            }

            @Override
            void notifyReceivedBankReply(ClientRequest clientRequest, BankQuoteReply reply) {
                frame.addObject(clientRequest, reply);
            }
        };        
        activeClientProcesses.add(p);
        frame.addObject(null,request);
    }

    /**
     * starts all gateways
     */
    public void start() {
        clientGateway.start();
        creditGateway.start();
        bankGateway.start();
    }
}