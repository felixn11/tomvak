
import bank.Bank;
import client.ClientRequest;
import client.LoanTestClient;
import creditbureau.CreditBureau;
import loanbroker.LoanBroker;

/**
 * This application tests the LoanBroker system.
 * 
 */
public class RunMessaging {

    private static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {
        try {
            // read the queue names from file "MESSAGING.ini"  
            JMSSettings queueNames = new JMSSettings("MESSAGING_CHANNELS.ini");
            final String factoryName = queueNames.get(JMSSettings.CONNECTION);
            final String clientRequestQueue = queueNames.get(JMSSettings.LOAN_REQUEST);
            final String clientReplyQueue = queueNames.get(JMSSettings.LOAN_REPLY);
            final String creditRequestQueue = queueNames.get(JMSSettings.CREDIT_REQUEST);
            final String creditReplyQueue = queueNames.get(JMSSettings.CREDIT_REPLY);
            final String ingRequestQueue = queueNames.get(JMSSettings.BANK_1);
            final String bankReplyQueue = queueNames.get(JMSSettings.BANK_REPLY);

            // create a LoanBroker middleware
            LoanBroker broker = new LoanBroker(clientRequestQueue, creditRequestQueue, creditReplyQueue, ingRequestQueue, bankReplyQueue);
            System.out.println("Broker initialized");
            // create a Client Application
            LoanTestClient client = new LoanTestClient("The Hypotheker", factoryName, clientRequestQueue, clientReplyQueue);
            // create the CreditBuerau Application
            System.out.println("Client initialized");
            CreditBureau creditBuerau = new CreditBureau(factoryName, creditRequestQueue,creditReplyQueue);
            System.out.println("CreditBureau initialized");
            // create one Bank application
            Bank ing = new Bank("ING", factoryName, ingRequestQueue, bankReplyQueue, DEBUG_MODE);
            
            // open all connections in the broker, client and credit applications
            broker.start();
            creditBuerau.start();
            ing.start();
            client.start();
            System.out.println("Started services");

            // send three requests
            client.sendRequest(new ClientRequest(1, 100000, 24));
            client.sendRequest(new ClientRequest(2, 88888, 5));
            client.sendRequest(new ClientRequest(3, 100, 5));
            System.out.println("Send 3 requests");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
