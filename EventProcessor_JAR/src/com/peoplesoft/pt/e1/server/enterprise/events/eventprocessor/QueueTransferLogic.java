package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;

import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberDeliveryTransport;
import javax.jms.*;
import java.util.*;
import com.peoplesoft.pt.e1.server.enterprise.events.common.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription;
import javax.ejb.EJBException;
import javax.naming.CommunicationException;

import javax.crypto.NoSuchPaddingException;
import javax.ejb.EJBException;
import javax.naming.CommunicationException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import com.jdedwards.mgmt.agent.E1Agent;
import org.apache.commons.codec.binary.Base64;

public class QueueTransferLogic {

    private static E1Logger sE1Logger = JdeLog.getE1Logger(QueueTransferLogic.class.getName());
    private QueueSession qs = null;
    private QueueConnection qc = null;
    private SubscriberManager mEventRouter = null;
    private EventMessage event = null;
    private EventRouter mRouter = new EventRouter();
    private Vector cons = new Vector();
    private static final String SECTION = "EVENTS";
    private static final String JNDI_USER = "jndiuser";
    private static final String JNDI_PASSWORD = "jndipassword";
    private static boolean continueSend = false;
    public QueueTransferLogic() {
    }

    public void transferEventstoESB(javax.jms.Message msg,javax.ejb.MessageDrivenContext mMessageDrivenCtx) {
        javax.jms.QueueSender snd = null;
        try
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Sending event to ESBSubscriber starts ", null, null, null);
            if(msg instanceof ObjectMessage) {
                ObjectMessage objMessage = (ObjectMessage)msg;
                event = (EventMessage)objMessage.getObject();
            }
            Collection subscribers = SubscriberCache.getInstance().getActiveSubscribers();
                for (Iterator iter = subscribers.iterator(); iter.hasNext();)
                {
                    Subscriber subscriber = (Subscriber) iter.next();
                    SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
                    List subscriptions = subscriber.getActiveSubscriptions();

                    for (Iterator iter2 = subscriptions.iterator(); iter2.hasNext();)
                    {
                        Subscription subscription = (Subscription) iter2.next();
                        boolean matches = mRouter.matchSubscription(event.getCategory(),
                                event.getType(), event.getEnvironment(), subscription);
                                if(matches) {
                                continueSend = true;
                                break;
                                }
                    }
                    if((transport.getTransportType().equals(DeliveryTransportType.JMSQUEUE)) && continueSend)
                    {

                            String ctxFactory = transport.getProperty("JMSQICF");
                            String pvdrURL= transport.getProperty("JMSQPURL");
                            String conFactJNDI = transport.getProperty("JMSQCFNM");
                            String queueName= transport.getProperty("JMSQNM");
                            String userName = JdeProperty.getProperty(SECTION, JNDI_USER, null);
                            String pWord = JdeProperty.getProperty(SECTION, JNDI_PASSWORD, null);
                           
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"MDB ctxFactory Paramters for JMSQUEUE Subscribers are "+ctxFactory, null, null, null);
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"MDB pvdrURL Paramters for JMSQUEUE Subscribers are "+pvdrURL, null, null, null);
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"MDB confactjndi Paramters for JMSQUEUE Subscribers are "+conFactJNDI, null, null, null);
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"MDB queuename Paramters for JMSQUEUE Subscribers are "+queueName, null, null, null);
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"User name from property file is "+userName, null, null, null);
                            //sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Passwrod from property file is  "+pWord, null, null, null);

                            try
                            {
                                Hashtable env = new Hashtable();
                                env.put(Context.INITIAL_CONTEXT_FACTORY,ctxFactory);
                                env.put(Context.PROVIDER_URL, pvdrURL);
                                if(ctxFactory.equals("com.evermind.server.rmi.RMIInitialContextFactory") || ctxFactory.equals("weblogic.jndi.WLInitialContextFactory"))
                                {
                                    if(Base64.isBase64(userName.getBytes())){
                                        try {
                                            if(userName.length() > 11 && userName.endsWith("="))
                                                userName = new String(E1Agent.getAgent().decrypt(Base64.decodeBase64(userName.getBytes())));
                                        } catch (InvalidKeyException e) {
                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"InvalidKeyException in JNDI UserName "+e.toString(), null, null, e);
                                                                        } catch (NoSuchAlgorithmException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"NoSuchAlgorithmException in JNDI UserName  "+e.toString(), null, null, e);
                                                                        } catch (NoSuchPaddingException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"NoSuchPaddingException in JNDI UserName  "+e.toString(), null, null, e);
                                                                        } catch (InvalidKeySpecException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"InvalidKeySpecException in JNDI UserName  "+e.toString(), null, null, e);
                                                                        } catch (IOException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"IOException Occurs in JNDI UserName "+e.toString(), null, null, e);
                                                                        }
                                    }                            
                                    if(Base64.isBase64(pWord.getBytes())){
                                        try {
                                                if(pWord.length() > 11 && pWord.endsWith("="))
                                                    pWord = new String(E1Agent.getAgent().decrypt(Base64.decodeBase64(pWord.getBytes())));
                                                                        } catch (InvalidKeyException e) {
                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"InvalidKeyException in JNDI password "+e.toString(), null, null, e);
                                                                        } catch (NoSuchAlgorithmException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"NoSuchAlgorithmException in JNDI password  "+e.toString(), null, null, e);
                                                                        } catch (NoSuchPaddingException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"NoSuchPaddingException in JNDI password  "+e.toString(), null, null, e);
                                                                        } catch (InvalidKeySpecException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"InvalidKeySpecException in JNDI password  "+e.toString(), null, null, e);
                                                                        } catch (IOException e) {
                                                                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"IOException Occurs in JNDI password "+e.toString(), null, null, e);
                                                                        }
                                    }
                                    env.put(Context.SECURITY_PRINCIPAL, userName);
                                    env.put(Context.SECURITY_CREDENTIALS, pWord);
                                }
                                // Create initial context and lookup
                                javax.naming.Context ctx = new InitialContext(env);
                                QueueConnectionFactory qcf =
                                    (QueueConnectionFactory)ctx.lookup(conFactJNDI);

                                javax.jms.Queue q = (javax.jms.Queue) ctx.lookup(queueName);
                                qc  = qcf.createQueueConnection();
                                cons.add(qc);
                                qs  = qc.createQueueSession(true,0);
                                snd = qs.createSender(q);
                                qc.start();
                                TextMessage message = qs.createTextMessage();
                                message.setText(event.getXMLPayload());
                                snd.send(message);
                                
			//SAR - 8533645 
                                if(ctxFactory.equals("com.evermind.server.rmi.RMIInitialContextFactory") || ctxFactory.equals("weblogic.jndi.WLInitialContextFactory"))
                                {
                                	qs.commit();
                                 }
                                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Message Sent to JMSQUEUE Subscriber  ", null, null, null);

                            }
                            catch(JMSException j) {
                                    StringBuffer buffer =
                                        new StringBuffer(300);
                                    buffer.append("Failed to send Event to JMSQUEUE Subscriber "+subscriber.getUsername());
                                    buffer.append(" with the following Queue Details. Please check the values and try again ");
                                    buffer.append("[ContextFactory -" + ctxFactory);
                                    buffer.append("][pvdrURL -" + pvdrURL);
                                    buffer.append("][conFactJNDI -" + conFactJNDI);
                                    buffer.append("][queueName -" + queueName);
                                    buffer.append("][userName -" + userName);
                                    //buffer.append("][pWord -" + pWord);
                                    buffer.append("exception :").append(j.getMessage());
                                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"JMSException Occurs while sending the event "+buffer.toString(), null, null, j);
                                    closeConnection();
                                    mMessageDrivenCtx.setRollbackOnly();
                           }
                            catch(EJBException e){
                                StringBuffer buffer =
                                    new StringBuffer(300);
                                buffer.append("Failed to send Event to JMSQUEUE Subscriber "+subscriber.getUsername());
                                buffer.append(" with the following Queue Details. Please check the values and try again ");
                                buffer.append("[ContextFactory -" + ctxFactory);
                                buffer.append("][pvdrURL -" + pvdrURL);
                                buffer.append("][conFactJNDI -" + conFactJNDI);
                                buffer.append("][queueName -" + queueName);
                                buffer.append("][userName -" + userName);
                                //buffer.append("][pWord -" + pWord);
                                buffer.append("exception :").append(e.getMessage());
                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"EJBException Occurs while sending the event "+buffer.toString(), null, null, e);
                                closeConnection();
                                mMessageDrivenCtx.setRollbackOnly();
                            }
                            catch(CommunicationException c) {
                                StringBuffer buffer =
                                    new StringBuffer(300);
                                buffer.append("Failed to send Event to JMSQUEUE Subscriber "+subscriber.getUsername());
                                buffer.append(" with the following Queue Details. Please check the values and try again ");
                                buffer.append("[ContextFactory -" + ctxFactory);
                                buffer.append("][pvdrURL -" + pvdrURL);
                                buffer.append("][conFactJNDI -" + conFactJNDI);
                                buffer.append("][queueName -" + queueName);
                                buffer.append("][userName -" + userName);
                                //buffer.append("][pWord -" + pWord);
                                buffer.append("exception :").append(c.getMessage());
                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Communication Exception Occurs while sending the event "+buffer.toString(), null, null, c);
                                closeConnection();
                                mMessageDrivenCtx.setRollbackOnly();
                            }
                            catch(Exception e) {
                                StringBuffer buffer =
                                    new StringBuffer(300);
                                buffer.append("Failed to send Event to JMSQUEUE Subscriber "+subscriber.getUsername());
                                buffer.append(" with the following Queue Details. Please check the values and try again ");
                                buffer.append("[ContextFactory -" + ctxFactory);
                                buffer.append("][pvdrURL -" + pvdrURL);
                                buffer.append("][conFactJNDI -" + conFactJNDI);
                                buffer.append("][queueName -" + queueName);
                                buffer.append("][userName -" + userName);
                                //buffer.append("][pWord -" + pWord);
                                buffer.append("exception :").append(e.getMessage());
                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception Occurs while sending the event "+buffer.toString(), null, null, e);
                                closeConnection();
                                mMessageDrivenCtx.setRollbackOnly();
                            }
                            catch(Throwable t) {
                                StringBuffer buffer =
                                    new StringBuffer(300);
                                buffer.append("Failed to send Event to JMSQUEUE Subscriber "+subscriber.getUsername());
                                buffer.append(" with the following Queue Details. Please check the values and try again ");
                                buffer.append("[ContextFactory -" + ctxFactory);
                                buffer.append("][pvdrURL -" + pvdrURL);
                                buffer.append("][conFactJNDI -" + conFactJNDI);
                                buffer.append("][queueName -" + queueName);
                                buffer.append("][userName -" + userName);
                                //buffer.append("][pWord -" + pWord);
                                buffer.append("exception :").append(t.getMessage());
                                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Throwable Occurs while sending the event "+buffer.toString(), null, null, t);
                                closeConnection();
                                mMessageDrivenCtx.setRollbackOnly();
                            }
                            finally {
                                if(null != qs) qs.close();
                            }
                    } //if
                    continueSend = false;
                } // for
        } //try
        catch(EventProcessingException e) {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"EventProcessorException in MDB is  "+e.toString(), null, null, e);
        }
        catch(JMSException j){
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"JMSEXxception in MDB is  "+j.toString(), null, null, j);
        }

    }
    public void closeConnection(){
            Iterator c = cons.iterator();
            for(int i =0;i<cons.size();i++)
            {
                try
                {
                    if(null != qc)
                    {
                        QueueConnection qcc = (QueueConnection)cons.get(i);
                        qcc.close();
                    }
                }
                catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
            cons.removeAllElements();
    }
}
