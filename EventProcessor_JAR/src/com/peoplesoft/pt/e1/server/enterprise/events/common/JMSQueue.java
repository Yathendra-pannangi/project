//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.enterprise.events.common;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.NamingException;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;

//=================================================
// Imports from org namespace
//=================================================

/**
 * JMSQueue is a utility class for creating the necessary
 * JMS Queue configurations.
 */
public class JMSQueue
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(JMSQueue.class.getName());

    private static final String DEFAULT_Q_CON_FACTORY_JNDI_NAME = JMSNames.Q_CON_FACTORY;

    //=================================================
    // Instance member fields.
    //=================================================

    private String mQueueJNDIName;

    private String mConnectionFactoryJNDIName = DEFAULT_Q_CON_FACTORY_JNDI_NAME;

    private Queue mQueue = null;
    
    private QueueConnection mQueueConnection = null;

    private QueueSession mQueueSession = null;
    
    private QueueBrowser mQueueBrowser = null;

    private QueueSender mQueueSender = null;
    
    private QueueReceiver mQueueReceiver = null;
    
    //Default is auto acknowledge.
    private int mQueueAcknowledge = Session.AUTO_ACKNOWLEDGE;
    
    private boolean mTransacted = false;

    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     * 
     *  @param queueJNDIName  The JNDI name for the queue.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the queue connection factory.
     *                                    If this value is <code>null</code> a default value
     *                                    will be used.
     * 
     *  @param transacted  Create the queue session as transacted?
     * 
     *  @param acknowledge The queue session acknowledgement value.
     */
    public JMSQueue(
        String queueJNDIName,
        String connectionFactoryJNDIName,
        boolean transacted,
        int acknowledge)
    {
        mQueueJNDIName = queueJNDIName;
        if (connectionFactoryJNDIName != null)
        {
            mConnectionFactoryJNDIName = connectionFactoryJNDIName;
        }
        mQueueAcknowledge = acknowledge;
        mTransacted = transacted;
    }
    
    /**
     *  Constructor.
     * 
     *  @param queueJNDIName  The JNDI name for the queue.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the queue connection factory.
     *                                    If this value is <code>null</code> a default value
     *                                    will be used.
     * 
     *  @param transacted  Create the queue session as transacted?
     */
    public JMSQueue(String queueJNDIName, String connectionFactoryJNDIName, boolean transacted)
    {
        mQueueJNDIName = queueJNDIName;
        if (connectionFactoryJNDIName != null)
        {
            mConnectionFactoryJNDIName = connectionFactoryJNDIName;
        }
        mTransacted = transacted;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Initialize the output queue.
     * 
     *  @throws EventProcessingException  An error occured during initialization.
     */
    public void initialize() throws EventProcessingException
    {
        //
        //  Lookup the queue conneciton factory.
        //
        QueueConnectionFactory connectionFactory = null;
        try
        {
            connectionFactory = (QueueConnectionFactory)JNDIUtil.lookup(mConnectionFactoryJNDIName);

        }
        catch (NamingException e)
        {
            String msg = "JNDI lookup of QueueConnectionFactory failed: " + e.getMessage();
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Lookup the queue.
        //
        try
        {
            mQueue = (Queue)JNDIUtil.lookup(mQueueJNDIName);
        }
        catch (NamingException e)
        {
            String msg =
                "JNDI lookup of Queue failed: "
                    + "queueJNDIName= "
                    + mQueueJNDIName
                    + " message="
                    + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Create the queue connection, session, and sender.
        //
        try
        {
            mQueueConnection = connectionFactory.createQueueConnection();
            mQueueConnection.start();
            mQueueSession = mQueueConnection.createQueueSession(mTransacted, mQueueAcknowledge);
            mQueueSender = mQueueSession.createSender(mQueue);
            mQueueReceiver = mQueueSession.createReceiver(mQueue);
            mQueueBrowser = mQueueSession.createBrowser(mQueue);
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
            mQueueSender = null;
            mQueueSession = null;
            mQueueConnection = null;
            
            StringBuffer buffer = 
                new StringBuffer("Failed to initialize queue connection and session:");
            buffer.append(System.getProperty("line.separator"));
            buffer.append("    queueJNDIName=").append(mQueueJNDIName);
            buffer.append(System.getProperty("line.separator"));
            buffer.append("    exception: ").append(e.getMessage());
            String msg = buffer.toString();

            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        mInitialized = true;
    }

    /**
     *  Shutdown the output queue.
     * 
     *  @throws EventProcessingException  An error occured during the shutdown.
     */
    public void shutdown() throws EventProcessingException
    {
        mInitialized = false;
        try
        {
            mQueueSender.close();
            mQueueSender = null;
            mQueueReceiver.close();
            mQueueReceiver = null;
            mQueueBrowser.close();
            mQueueBrowser = null;
            mQueueSession.close();
            mQueueSession = null;
            mQueueConnection.close();
            mQueueConnection = null;
        }
        catch (JMSException e)
        {
            String msg = "Failed to shutdown EventQueue: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }

    /**
     * @return  The current queue session.
     */
    public QueueSession getQueueSession()
    {
        return mQueueSession;
    }

    /**
     * @return  <code>true</code> = has been initialized, <code>false</code> = has not been
     *          initialized.
     */
    public boolean isInitialized()
    {
        return mInitialized;
    }

    /**
     * @return  Queue queue.
     */
    public Queue getQueue()
    {
        return mQueue;
    }
    
    /**
     * @return  The current Queue Browser.
     */
    public QueueBrowser getQueueBrowser()
    {
        return mQueueBrowser;
    }
    
    /**
     * @return  The current Queue sender.
     */
    public QueueSender getQueueSender()
    {
        return mQueueSender;
    }

    /**
     * @return  The current queue receiver.
     */
    public QueueReceiver getQueueReceiver()
    {
        return mQueueReceiver;
    }
    
    /**
     * @return  JNDI name of the queue.
     */
    public String getQueueJNDIName()
    {
        return mQueueJNDIName;
    }

}
