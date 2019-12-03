//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.jdenet;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.NamingException;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.ListenerContext;
import com.jdedwards.system.net.ProcessKernel;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TriggerMessage;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This class listens for jdenet trigger messages from the 
 * event generator. When a trigger message is received it is
 * passed off to a JMS queue to trigger the transfer agent to
 * get commited events from the database.
 * @author Bhushan Bhale
 * @version 1.0
 */
class TriggerHandler extends ProcessKernel
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(TriggerHandler.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================   

    //=================================================
    // Instance member fields.
    //================================================= 

    private QueueConnection mQueueConnection;

    private QueueSession mQueueSession;

    private Queue mQueue;

    private QueueSender mQueueSender;

    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================

    /**
     * Constructor.
     */
    public TriggerHandler()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * This method checks the TriggerQueue for any messages. If queue is
     * empty it will create a new ObjectMessage and will put it in TriggerQueue.
     * 
     * @param m the message
     * @throws TriggerHandlerException  Error during processing.
     * @see ProcessKernel
     */
    public void process(JdeMsg m) throws TriggerHandlerException
    {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering TriggerQueue Process", null, null, null);
        }

        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(TriggerHandler.class.getName() + " not initialized");
        }
        sendTriggerMessage();
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Exiting TriggerQueue Process", null, null, null);
        }
    }

    /**
     * This method sends a trigger message to a JMS queue.
     * @throws TriggerHandlerException Error during processing.
     */
    void sendTriggerMessage() throws TriggerHandlerException
    {
        QueueBrowser browser = null;
        try
        {
            browser = mQueueSession.createBrowser(mQueueSender.getQueue());
            if (!browser.getEnumeration().hasMoreElements())
            {
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Sending a trigger message", null, null, null);
                }
                ObjectMessage message = mQueueSession.createObjectMessage(new TriggerMessage());
                mQueueSender.send(message);
            }
        } 
        catch (JMSException ex)
        {
            String msg = "QueueBrowser creation failed: " + ex.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,ex);
            throw new TriggerHandlerException(msg);
        }
        finally
        {
            if (browser != null)
            {
                try
                {
                    browser.close();
                }
                catch (JMSException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close JMS QueueBrowser: " + e.getMessage(), null, null, e);
                }
            }
            browser = null;
        }
    }

    /**
     * This method looks up TriggerQueue using JNDI lookup and then creates a connection,
     * queue session and queue sender. If any one on these creation fails it throws
     * TriggerListenerException and logs it.
     * @param context the context  
     * @throws TriggerHandlerException Error during processing.
     * @see ProcessKernel
     */
    public void initialize(ListenerContext context) throws TriggerHandlerInitializationException
    {
        //
        //  Get the JNDI initial context to look-up JMS.
        //
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initializing TriggerListener", null, null, null);
        }

        //
        //  Lookup the queue connection factory.
        //
        QueueConnectionFactory connectionFactory = null;
        try
        {
            connectionFactory =
                (QueueConnectionFactory)JNDIUtil.lookup(JMSNames.Q_CON_FACTORY);
        } 
        catch (NamingException e)
        {
            String msg = "JNDI lookup of QueueConnectionFactory failed: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new TriggerHandlerInitializationException(msg);
        }
        
        //
        //  Lookup the queue.
        //
        try
        {
            mQueue = (Queue)JNDIUtil.lookup(JMSNames.TRIGGER_Q);
        } 
        catch (NamingException e)
        {
            String msg = "JNDI lookup of Queue failed: queueName= " + JMSNames.TRIGGER_Q
                         + " message=" + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new TriggerHandlerInitializationException(msg);
        }

        //
        //  Create the queue connection, session, and sender.
        //
        try
        {
            mQueueConnection = connectionFactory.createQueueConnection();
            mQueueSession = mQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            mQueueSender = mQueueSession.createSender(mQueue);
        } 
        catch (JMSException e)
        {
            //
            //  Clean-up the session and connection.
            //
            if (mQueueSession != null)
            {
                try
                {
                    mQueueSession.close();
                } 
                catch (Exception e1)
                {
                    //  Ignore.
                }
            }
            if (mQueueConnection != null)
            {
                try
                {
                    mQueueConnection.close();
                } 
                catch (Exception e1)
                {
                    //  Ignore.
                }
            }
            mQueue = null;
            mQueueSender = null;
            mQueueSession = null;
            mQueueConnection = null;

            String msg = "Failed to initialize queue connection and session: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new TriggerHandlerInitializationException(msg);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initialize TriggerListerner complete", null, null, null);
        }
        mInitialized = true;
    }

    /**
     * This is a clean up method. This method stops TriggerTimerTask and
     * cleans up the queue environment.
     * @throws TriggerHandlerException ex
     */
    public void shutdown() throws TriggerHandlerException
    {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"TriggerListener is shutting down", null, null, null);
        }
        mInitialized = false;
        try
        {
            mQueue = null;
            mQueueSender.close();
            mQueueSender = null;
            mQueueSession.close();
            mQueueSession = null;
            mQueueConnection.close();
            mQueueConnection = null;
        } 
        catch (JMSException e)
        {
            String msg = "Failed to shutdown TriggerQueue: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new TriggerHandlerException(msg);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"TriggerListener shutdown complete", null, null, null);
        }
    }
}
