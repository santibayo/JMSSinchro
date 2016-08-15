/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package activemqtest;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author C019301
 */
public class Test2 implements ExceptionListener, MessageListener ,CompletionListener{
    private Session session=null;
    public Test2(Session s){
        session = s;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JMSException {
        // TODO code application logic here
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Test2 t =new Test2(session);
        connection.setExceptionListener(t);

        // Create a Session
        

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("QCliOut");

        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(t);

        /*
        // Wait for a message
        Message message = consumer.receive();

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Received: " + text);
        } else {
            System.out.println("Received: " + message);
        }
         */
        /*
        consumer.close();
        session.close();
        connection.close();*/
    }

    @Override
    public void onException(JMSException exception) {
        exception.printStackTrace();
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("Server MSG RECIEVED");
        
        try {
            TextMessage response = session.createTextMessage();
            String security = (String) message.getObjectProperty("security");
            System.out.println(security);
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String str = textMessage.getText();
                System.out.println(str);
                // Obtenemos mensaje
                System.out.println(">>>FO!!");
                // Escribimos mensaje
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                MessageProducer reply = session.createProducer(null);
                reply.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                response.setText("Success!!!!!");
                reply.send(message.getJMSReplyTo(), response);
                //reply.send(message.getJMSReplyTo(), message,(CompletionListener)this);
                System.out.println(">>>OUT!!");
            }else{ 
                 System.out.println("Error");
            }
        } catch (JMSException ee) {
            ee.printStackTrace();
        }
    }

    @Override
    public void onCompletion(Message message) {
        System.out.println("completo: "+message.toString());
    }

    @Override
    public void onException(Message message, Exception exception) {
        System.out.println("completo: "+message.toString());
        exception.printStackTrace();
    }

}
