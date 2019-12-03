//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.adaptor;

//=================================================
//Imports from java namespace
//=================================================
import java.io.IOException;

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.QueueReceiver;
import javax.jms.Session;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.JdeNetUtil;
import com.jdedwards.system.net.JdeSocket;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSQueue;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;

//=================================================
//Imports from org namespace
//=================================================

//=================================================
//Imports from junit namespace
//=================================================

/**
 * This class creates a JDENetTransport. Waits till the
 * message is put in subscriber queue. After message is received
 * JDENetTransport will try to send it. If fails, wait for specified
 * timeout and try again. Message will not be deleted from the queue till
 * it is sent successfully to the subscriber. 
 */
public class JDENetTransport extends Thread
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    private static final int XML_DISPATCH_KERNEL_MSG = 13515;
    
    private static final int MSG_FLAGS = 0x0001;
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(JDENetTransport.class.getName());
     
    private static long sBackOffTime = JdeProperty.getProperty("JDENET", "BackOffTime", 30000);
        
    private static int sTimeout = 
                JdeProperty.getProperty("JDENET", "enterpriseServerTimeout", 90000);
    
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private Subscriber mSubscriber = null;
    
    private QueueReceiver mReceiver = null;
    
    private boolean mRun = true;
    
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Constructor to set subscriber for JDENet Transport.
     * @param subscriber Subcriber
     */
    public JDENetTransport(Subscriber subscriber)
    {
        mSubscriber = subscriber;
    }
    
    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Returns the subscriber associated with that thread.
     * @return Subscriber
     */
    public Subscriber getSubscriber()
    {
        return mSubscriber;
    }

    /**
     * This method starts a new thread for each subscriber to transport the
     * event message via JDENet.
     */
    public void run()
    {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Starting JDENET transport for subscriber: " 
                            + mSubscriber.getUsername(), null, null, null);
        }
        AdaptorUtil util = AdaptorUtil.getInstance();
        String queueName = mSubscriber.getQueueDescription().getQueueJNDIName();
        JMSQueue queue = null;
        try
        {
            queue =
                util.createQueueConnection(
                    queueName,
                    JMSNames.Q_CON_FACTORY,
                    Session.CLIENT_ACKNOWLEDGE,
                    true);
            mReceiver = queue.getQueueReceiver();
        }
        catch(EventProcessingException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                "Error creating a JDENET Adaptor queue connection: " + ex.getMessage(),null, null,
                ex);
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                "JDENET adaptor thread aborting for subscriber: " + mSubscriber.getUsername(), null, null,null);
            return;
        }        
        
        //
        //  Enter the main loop.  This loop continues processing until the thread
        //  is instructed to shutdown.
        //
        Message message = null;
        while (getRun())
        {
            //
            //  Get the network transport parameters for this subscriber.
            //
            HostInfo hostInfo = null;
            try
            {
                hostInfo = getHostInfo();
            }
            catch (EventProcessingException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                    "Failed to get JDENET host information, thread aborting for subscriber: "
                        + mSubscriber.getUsername(),null, null,e);
                break;
            }
            
            //
            //  Attempt to open a socket.  If the open fails then the thread will wait
            //  and loop around and try to open the socket again.
            //
            JdeSocket soc = openSocket(hostInfo);

            //
            //  If we have a good socket then wait for an event to arrive and try sending it.
            //
            while ((soc != null) && (soc.isSocketGood()) && getRun())
            {
                try
                {
                    //
                    //  Do not overwrite the previous message.
                    //
                    if (message == null)
                    {
                        message = mReceiver.receive();
                    }
                    if (message != null)
                    {
                        //
                        //  Check that the message is of the proper type.
                        //
                        if (!(message instanceof ObjectMessage))
                        {
                            String msg =
                                "Invalid JMS message type sent from Event Processing System. "
                                    + "Event will not be sent.";
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
                            queue.getQueueSession().commit();
                            continue;
                        }

                        //
                        //  Extract the object out of the message.
                        // 
                        ObjectMessage objMessage = (ObjectMessage)message;
                        EventMessage eventMessage = null;
                        try
                        {
                            eventMessage = (EventMessage)objMessage.getObject();
                        }
                        catch (JMSException e)
                        {
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error retrieving EventMessage from JMS message.", null, null,e);
                            queue.getQueueSession().commit();
                            continue;
                        }

                        //
                        //  Build a JDENET message that contains the XML payload.
                        //
                        String payLoad = eventMessage.getXMLPayload();
                        JdeMsg netMsg =
                            new JdeMsg(XML_DISPATCH_KERNEL_MSG,
                                        MSG_FLAGS,
                                        JdeNetUtil.NET_DEFAULT_PRIORITY);
                        //XMLDiapatch kernel excepts bytes only. 
                        //So you cannot pass a unicode string or a UTF-8 String  
                        netMsg.addData(payLoad.getBytes());
                        //
                        //  Send the message.
                        //
                        netMsg.send(soc);
                        //
                        //  Acknowledge that the message has been processed.
                        //
                        //message.acknowledge();
                        queue.getQueueSession().commit();

                        //
                        //  Null the message so that the next one is received above.
                        //
                        message = null;
                        if (sE1Logger.isDebug())
                        {
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JDENet Apdatpor message with message length: "
                                            + payLoad.length() + " Byte Length: "
                                            + payLoad.getBytes().length, null, null, null);
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JDENet Apdatpor sent a message with message type : " 
                                        + XML_DISPATCH_KERNEL_MSG + " and payload: " + payLoad, null, null, null);
                        }
                    }
                }
                catch (IOException ex)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                        "IOException occured while sending a message in JDENetTransport. "
                            + ex.getMessage(),null, null,
                        ex);
                    break;
                }
                catch (Exception ex)
                {
                    //
                    //  If the thread has been commanded to shutdown then don't log
                    //  an error.
                    //
                    if (getRun())
                    {
                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                            "Exception occured while sending a message in "
                                + "JDENetTransport. "
                                + ex.getMessage(),null, null,
                            ex);
                    }
                    break;
                }

            } // End of while good socket loop

            //
            //  Unable to send the event.  Rollback the queue session so that the
            //  event message is delivered again.
            //
            try
            {
                queue.getQueueSession().rollback();
                message = null;
            }
            catch (JMSException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception from JMS system: " + e.getMessage(),null, null, e);
            }
            
            //
            //  Mark the socket as bad.
            //
            util.returnBadSocket(soc);
            soc = null;
            
            //
            //  Wait before trying to open a new socket connection.
            //
            if (getRun())
            {
                waitBeforeRetry(this);
            }
            
        }  //  End of main while loop
        
        //
        //  Shutdown the JMS queue.
        //
        try
        {
            queue.shutdown();
        }
        catch (EventProcessingException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error shuting down JMS queue: " + e.getMessage(),null, null, e);
        }
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JDENET transport stopped for subscriber: " + mSubscriber.getUsername(), null, null, null);
    }

    
    private void waitBeforeRetry(JDENetTransport transport)
    {
        try
        {
            synchronized (transport)
            {
                this.wait(sBackOffTime);
            }
        }
        catch (InterruptedException e)
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JDENETTransport thread interrupted", null, null, null);
        }
    }

    /**
     * Stops the JdenetTransport Adaptor thread.
     */
    public void stopJdenetTransport()
    {        
        setRun(false);
        interrupt();
        try
        {
            mReceiver.close();
        }
        catch (JMSException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"JDENETTransport: queue receiver close() failed: " + ex.getMessage(),null, null, ex);
        }
    }
    
    private HostInfo getHostInfo() throws EventProcessingException
    {
        //
        //  Get the network transport parameters for this subscriber.
        //
        String host = mSubscriber.getDeliveryTransport().getProperty(
                                    F90711_EventTransportParameter.JDENET_HOST_NAME);
        String portString = mSubscriber.getDeliveryTransport().getProperty(
                                    F90711_EventTransportParameter.JDENET_PORT_NUMBER);
        String timeoutString = mSubscriber.getDeliveryTransport().getProperty(
                                    F90711_EventTransportParameter.JDENET_TIMEOUT);
        
        //
        //  Verify we have a host and port.
        //
        if ((host == null) || (host.trim().length() == 0))
        {
            String msg = "Bad host configured for JDENET subscriber: " + mSubscriber.getUsername();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        if ((portString == null) || (portString.trim().length() == 0))
        {
            String msg = "Bad port configured for JDENET subscriber: " + mSubscriber.getUsername();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
            throw new EventProcessingException(msg);
        }
        
        //
        //  Parse the port number.
        //
        int port = 0;
        try
        {
            port = Integer.parseInt(portString);
        }
        catch(NumberFormatException ex)
        {
            String msg = "Error parsing JDENET port for subscriber: " + mSubscriber.getUsername();
            msg += ".  Port string = [" + portString + "]";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, ex);
            throw new EventProcessingException(msg, ex);
        }
        
        //
        //  Parse the timeout value.
        //
        int timeout = 0;
        try
        {
            if((timeoutString != null) && (timeoutString.length() > 0))
            {
                timeout = Integer.parseInt(timeoutString);
            }
            else
            {
                timeout = sTimeout;
            }
        }
        catch(NumberFormatException ex)
        {
            String msg =
                "Error parsing JDENET timeout for subscriber: "
                    + mSubscriber.getUsername()
                    + ".  Port string = ["
                    + portString
                    + "]";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, ex);
        }
        return new HostInfo(host, port, timeout);
    }
    
    /**
     *  Attempt to open a socket.  If the open fails then the thread will wait
     *  and loop around and try to open the socket again.
     */
    private JdeSocket openSocket(HostInfo hostInfo)
    {
        JdeSocket socket = null;
        while (socket == null)
        {
            try
            {
                socket =
                    AdaptorUtil.getInstance().createJDENetConnection(
                        hostInfo.getHost(),
                        hostInfo.getPort(),
                        hostInfo.getTimeout());
            }
            catch (Exception ex)
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Waiting to retry JDENet connection for subscriber: "
                        + mSubscriber.getUsername(), null, null, null);
                waitBeforeRetry(this);
                continue;
            }
            
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Created a JDENet connection in JDENetTransportAdaptor with: ", null, null, null);
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "host="
                        + hostInfo.getHost()
                        + " port="
                        + hostInfo.getPort()
                        + " timeout="
                        + hostInfo.getTimeout(), null, null, null);
            }
        } 
        return socket;
    }
    
    private class HostInfo
    {
        private int mPort;
        private int mTimeout;
        private String mHost;
        
        /**
         *  Constructor.
         */
        public HostInfo(String host, int port, int timeout)
        {
            mHost    = host;
            mPort    = port;
            mTimeout = timeout;
        }
        
        /**
         * @return
         */
        public String getHost()
        {
            return mHost;
        }

        /**
         * @return
         */
        public int getPort()
        {
            return mPort;
        }

        /**
         * @return
         */
        public int getTimeout()
        {
            return mTimeout;
        }
    }
    
    /**
     *  These two methods for getting and setting run must be synchronized since
     *  the value of run is used for communication between threads.
     */
    private synchronized boolean getRun()
    {
        return mRun;
    }
    
    private synchronized void setRun(boolean run)
    {
        mRun = run;
    }
}
