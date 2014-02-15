package client;

import client.gui.ClientFrame;
import messaging.LoanBrokerGateway;

/**
 * This class represents one Client Application.
 * It:
 *  1. Creates a ClientRequest for a loan.
 *  2. Sends it to the LoanBroker Messaging-Oriented Middleware (MOM).
 *  3. Receives the reply from the LoanBroker MOM.
 * 
 */
public class LoanTestClient {

    private ClientFrame frame; // GUI
    private LoanBrokerGateway gtw;

    public LoanTestClient(String name, String factoryName, String requestQueue, String replyQueue) {
        super();
        
        gtw = new LoanBrokerGateway(factoryName, requestQueue, replyQueue) {

            @Override
            public void onLoanOffer(ClientReply r) {
                processReply(r);
            }
        };
        
         // create the GUI
        frame = new ClientFrame(name) {

            @Override
            public void send(ClientRequest request) {
                sendRequest(request);
            }
        };

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }
    
    public void processReply(ClientReply reply){
        frame.addReply(null, reply);
    }
    
    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(ClientRequest request) {
        gtw.applyForLoan(request);
        //producer.send(session.createTextMessage(serializer.requestToString(request)));
        frame.addRequest(request);
    }
}