package creditbureau;

import creditbureau.gui.CreditFrame;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messaging.LoanBrokerGateway;
import messaging.Reply;
import messaging.Request;

/**
 * This class represents one Credit Agency Application.
 * It should:
 *  1. Receive CreditRequest-s for a loan from the LoanBroker Messaging-Orianted Middleware (MOM).
 *  2. Randomly create CreditReply for each request (use method "getReply").
 *  3. Send the CreditReply from the LoanBroker MOM.
 */
public class CreditBureau {

    private Random random = new Random(); // for random generation of replies
    private CreditFrame frame; // GUI
    
    private LoanBrokerGateway gtw;
    
    public CreditBureau(String factoryName, String creditRequestQueue, String creditReplyQueue) {
        super();
        
        //reply and request queue are inverted because the request comes from the loanbroker
        //and he expects a reply
        gtw = new LoanBrokerGateway(factoryName, creditReplyQueue, creditRequestQueue) {
            
            @Override
            public void onRecievedReply(Reply r) {
                //no reply
            }

            @Override
            public void onRecievedRequest(Request r) {
                CreditRequest request = (CreditRequest)r;
                onCreditRequest(request);
            }
        };

        // create GUI
        frame = new CreditFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }
    
    public void start(){
        gtw.start();
    }
    
    /**
     * Processes a new request message by randomly generating a reply and sending it back.
     * @param message the credit request message
     */
    private void onCreditRequest(CreditRequest request) {
        try {
            frame.addRequest(request);
            CreditReply reply = computeReply(request);
            gtw.sendCreditReply(reply);
            frame.addReply(request, reply);
        } catch (Exception ex) {
            Logger.getLogger(CreditBureau.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   /**
    * Randomly generates a CreditReply given the request.
    * @param request is the Creditrequest for which the reply must be generated
    * @return a credit reply
    */
    private CreditReply computeReply(CreditRequest request) {
        int ssn = request.getSSN();

        int score = (int) (random.nextInt(600) + 300);
        int history = (int) (random.nextInt(19) + 1);

        return new CreditReply(ssn, score, history);
    }
}
