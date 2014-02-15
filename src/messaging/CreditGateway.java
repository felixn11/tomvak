/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messaging;

import creditbureau.CreditSerializer;

/**
 *
 * @author user
 */
public abstract class CreditGateway {
    
    MessagingGateway msgGtw;
    CreditSerializer serializer;
    
    public CreditGateway(){
        
    }
    
}
