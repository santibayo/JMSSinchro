package mx.qro.messaging.client;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static int ackMode;
    private String clientQueueName;
    private boolean wait;
    private boolean transacted = false;
    private MessageProducer producer;
    private Destination tempDest = null;
    private Session session = null;
    private Thread thread=null;
    Connection connection;
    static {
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    public Client(String queue, String host) throws JMSException {
        this.clientQueueName = queue;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host);
        connection = connectionFactory.createConnection();
        connection.start();
    }
    public void connect(){
        try {
            
            session = connection.createSession(transacted, ackMode);
            Destination adminQueue = session.createQueue(clientQueueName);            
            this.producer = session.createProducer(adminQueue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            this.tempDest = session.createTemporaryQueue();
            
        } catch (JMSException e) {
            //Handle the exception appropriately
        }
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
    
    public boolean isWaiting(){
        return this.wait;
    }
    
    public void send(String mesg, SecurityBean secBean, CliResponse r) throws JMSException {
        synchronized (this){
            this.wait = true;
        }
        //Now create the actual message you want to send
        MessageConsumer responseConsumer = session.createConsumer(tempDest);
        this.setThread(Thread.currentThread());
        ResponseListener rl = new ResponseListener(this);
        rl.setCliRspn(r);

        responseConsumer.setMessageListener(rl);
        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setText(mesg);
        txtMessage.setObjectProperty("security", secBean.getSchema() + "://" + secBean.getValue());

        
        txtMessage.setJMSReplyTo(tempDest);

        
        String correlationId = this.createRandomString();
        rl.setCorrId(correlationId);
        txtMessage.setJMSCorrelationID(correlationId);
        this.producer.send(txtMessage);
        System.out.println("Send");        
        while (r.getResponse() == null) {
            try {
                synchronized(this){
                    this.wait();                          
                }
                this.wait =false;
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                synchronized(this){
                    this.notify();
                }
            }
        }

    }

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
       
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    public void close()throws JMSException{
        connection.close();
    }
    
    public static void main(String[] args) throws JMSException {
        Client c = new Client("QCliOut", "localhost:61616");
        c.connect();
        
        SecurityBean secBean = new SecurityBean();
        secBean.setSchema("ref");
        secBean.setValue("Admin123509");
        CliResponse cliResponse = new CliResponse();
        
        for (int a=0;a<10;a++){
            c.send("Risas totales", secBean, cliResponse);
            if (!c.isWaiting()){
            System.out.println(cliResponse.getResponse());
            }
            
        }
    }
}
