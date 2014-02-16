/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import client.*;
import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import loanbroker.gui.LoanBrokerFrame;
import messaging.BankGateway;
import messaging.ClientGateway;
import messaging.CreditGateway;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {
    
    private ClientGateway clientGtw;
    private CreditGateway creditGtw;
    private BankGateway bankGtw;

    private LoanBrokerFrame frame; // GUI

    /**
     * Intializes attributes, and registers itself (method onClinetRequest) as
     * the listener for new client requests
     * @param factoryName
     * @param clientRequestQueue
     * @param clientReplyQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankRequestQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String factoryName, String clientRequestQueue, String clientReplyQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception {
        super();
        
        clientGtw = new ClientGateway(factoryName, clientRequestQueue, clientReplyQueue) {
            @Override
            public void onRecievedRequest(ClientRequest r) {
                onClientRequest(r);
            }
        };
        
        creditGtw = new CreditGateway(factoryName, creditRequestQueue, creditReplyQueue) {
            @Override
            public void onRecievedReply(CreditReply r) {
                onCreditReply(r);
            }
        };
        
        bankGtw = new BankGateway(factoryName, bankRequestQueue, bankReplyQueue) {

            @Override
            public void onRecievedReply(BankQuoteReply r) {
                onBankReply(r);
            }
        };
        
        /*
         * Make the GUI
         */
        frame = new LoanBrokerFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }

    /**
     * This method is called when a new client request arrives.
     * It generates a CreditRequest and sends it to the CreditBureau.
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(ClientRequest request) {
        frame.addObject(null, request);
        CreditRequest credit = createCreditRequest(request);
        creditGtw.sendCreditRequest(credit);
    }
    
    /**
     * This method is called when a new credit reply arrives.
     * It generates a BankQuoteRequest and sends it to the Bank.
     * @param message the incomming message containng the CreditReply
     */
    private void onCreditReply(CreditReply reply) {
        frame.addObject(null, reply);
        BankQuoteRequest bank = createBankRequest(null, reply);
        bankGtw.sendBankRequest(bank);
    }
    
    /**
     * This method is called when a new bank quote reply arrives.
     * It generates a ClientReply and sends it to the LoanTestClient.
     * @param message the incomming message containng the BankQuoteReply
     */
    private void onBankReply(BankQuoteReply reply) {
        frame.addObject(null, reply);
        ClientReply client = createClientReply(reply);
        clientGtw.sendClientReply(client);
    }
    /**
     * Generates a credit request based on the given client request.
     * @param clientRequest
     * @return
     */
    private CreditRequest createCreditRequest(ClientRequest clientRequest) {
        return new CreditRequest(clientRequest.getSSN());
    }
    /**
     * Generates a bank quote reguest based on the given client request and credit reply.
     * @param creditReply
     * @return
     */
    private BankQuoteRequest createBankRequest(ClientRequest clientRequest, CreditReply creditReply) {
        int ssn = creditReply.getSSN();
        int score = creditReply.getCreditScore();
        int history = creditReply.getHistory();
        int amount = 100;
        int time = 24;
        if (clientRequest != null){
            amount = clientRequest.getAmount();
            time = clientRequest.getTime();
        }
        return  new BankQuoteRequest(ssn, score, history, amount, time);
    }
    /**
     * Generates a client reply based on the given bank quote reply.
     * @param creditReply
     * @return
     */
    private ClientReply createClientReply(BankQuoteReply reply) {
        return new ClientReply(reply.getInterest(), reply.getQuoteId());
    }

    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        bankGtw.start();
        creditGtw.start();
        clientGtw.start();
    }
}
