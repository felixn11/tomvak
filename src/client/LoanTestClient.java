package client;

import client.gui.ClientFrame;
import messaging.LoanBrokerGateway;
import messaging.Reply;
import messaging.Request;

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
    private ClientLoanBrokerGateway gtw;

    public LoanTestClient(String name, String factoryName, String requestQueue, String replyQueue) {
        super();
        
        gtw = new ClientLoanBrokerGateway(factoryName, requestQueue, replyQueue) {
            
            @Override
            public void onRecievedReply(ClientRequest req, ClientReply rep) {
                processReply(req, rep);
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
    
    public void start(){
        gtw.start();
    }
    
    public void processReply(ClientRequest request, ClientReply reply){
        frame.addReply(request, reply);
    }
    
    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(ClientRequest request) {
        gtw.applyForLoan(request);
        frame.addRequest(request);
    }
}