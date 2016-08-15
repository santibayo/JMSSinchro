/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.qro.messaging.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author C019301
 */
public class ResponseListener implements MessageListener{
    
    public ResponseListener(Client client){
        this.client= client;
    }
    CliResponse cliRspn;
    Client client;
    String corrId;

    public String getCorrId() {
        return corrId;
    }

    public void setCorrId(String corrId) {
        this.corrId = corrId;
    }
    

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
    

    public CliResponse getCliRspn() {
        return cliRspn;
    }

    public void setCliRspn(CliResponse cliRspn) {
        this.cliRspn = cliRspn;
    }
    
    
     public void onMessage(Message message) {
        String messageText = null;
        
        try {
            if (message instanceof TextMessage) {
                String corrId = message.getJMSCorrelationID();
                if (corrId.equals(this.getCorrId())){
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                System.out.println("messageText = " + messageText);
                this.cliRspn.setResponse(messageText);
                System.out.println(this.client.getThread());
                System.out.println(Thread.currentThread());
                }
                
                
            }
        } catch (JMSException e) {
            //Handle the exception appropriately
            e.printStackTrace();
        }finally {
            synchronized(this.client){
                    this.client.notify();
                }
                
        }
    }
}
