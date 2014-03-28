/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.bank;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import java.util.ArrayList;
import messaging.requestreply.IReplyListener;

/**
 * Each object of this class collects all BankQuoteReplies for one BankQuoteRequest.
 * @author Maja Pesic
 */
class BankQuoteAggregate {

    private BankQuoteRequest request;
    private ArrayList<BankQuoteReply> replies;
    private int nrExpectedReplies;
    private BankQuoteReply bestReply = null;
    private IReplyListener<BankQuoteRequest, BankQuoteReply> replyListener;

    BankQuoteAggregate(BankQuoteRequest request, int nrExpectedReplies, IReplyListener<BankQuoteRequest, BankQuoteReply> listener) {
        super();
        this.request = request;
        this.replies = new ArrayList<BankQuoteReply>();
        this.nrExpectedReplies = nrExpectedReplies;
        this.replyListener = listener;
    }

    /**
     * Add a new reply for this request.
     * @param reply is the reply that arrived and has to be processed
     * @return returns true if this is the last expected reply, false otherwise
     * @todo Implement this method:
     * 1. add the reply to the list of arrived replies
     * 2. if this reply is the best (i.e., the lowest interest), assign it to the bestReply
     * 3. return true if this is the last expected reply (use nrExpectedReplies and collection replies)
     */
    boolean addReply(BankQuoteReply reply) {
        replies.add(reply);

        if (bestReply != null) {
            if (reply.getInterest() < bestReply.getInterest()) {
                bestReply = reply;
            }
        } else {
            bestReply = reply;
        }

        if (replies.size() == nrExpectedReplies) {
            return true;
        } else {
            return false;
        }
    }

    void notifyListener() {
        if (replyListener != null) {
            replyListener.onReply(request, bestReply);
        }
    }
}
