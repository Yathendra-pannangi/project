//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.transferagent;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
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
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.common.CommittedEventKey;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Manages an event output queue.
 */
final class EventQueue
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**
     * The maximum number of output queues the system supports.
     * <br>
     * Note: if this value is increased then corresponding additional queues
     * need to be defined in the run-time environment and addtional instances
     * of the EventProcessor Message Driven EJB need to be defined.
     */
    public static final int MAX_EVENT_QUEUES = 4;
        
    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventQueue.class.getName());
    
    private static final String EVENT_SECTION = "EVENTS";
    
    private static final String NUM_QUEUES_PROP = "processingConcurrency";

    //=================================================
    // Instance member fields.
    //=================================================
    
    private int             mQueueIndex;
    
    private QueueConnection mQueueConnection;
    
    private QueueSession    mQueueSession;
    
    private Queue           mQueue;
    
    private QueueSender     mQueueSender;
    
    private boolean         mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Private constructor to prevent outside classes from creating event queues.
     * Use the static <code>getEventQueues</code> method.
     */
    private EventQueue()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Get the queues to be used to send messages to the event processor.
     * 
     * @return  An array of queues.
     * 
     * @throws EventProcessingException  Error occured during initialization.
     */
    static EventQueue[] getEventQueues() throws EventProcessingException
    {
        //
        //  Allocate the queues.
        //
        int numQueues = getNumEventQueues();
        EventQueue[] queues = new EventQueue[numQueues];
                
        for (int i = 0; i < numQueues; i++)
        {
            queues[i] = new EventQueue();
            queues[i].initialize(i);
        }
        return queues;
    }
    
    /**
     * Initialize the queue.
     * 
     * @param queueIndex  The index of the output queue.  Valid index ranges are 0
     *                    to <code>MAX_EVENT_QUEUES - 1</code>.
     * 
     * @throws EventProcessingException  Error occured during initialization.
     */
    private void initialize(int queueIndex) throws EventProcessingException
    {
        //
        //  Make sure the queue index is within the valid range.
        //
        if ((queueIndex < 0) || (queueIndex >= MAX_EVENT_QUEUES))
        {
            throw new IndexOutOfBoundsException("queueIndex out of range: "
                                               + Integer.toString(queueIndex));
        }
        mQueueIndex = queueIndex;
        
        //
        //  Lookup the queue conneciton factory.
        //
        QueueConnectionFactory connectionFactory = null;
        try
        {
            connectionFactory
                = (QueueConnectionFactory)JNDIUtil.lookup(JMSNames.Q_CON_FACTORY);
        }
        catch (NamingException e)
        {
            String msg = "JNDI lookup of QueueConnectionFactory failed: " + e.getMessage();
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        //
        //  Lookup the queue.
        //
        String queueName = getQueueJNDIName(queueIndex);
        try
        {
            mQueue = (Queue)JNDIUtil.lookup(queueName);
        }
        catch (NamingException e)
        {
            String msg = "JNDI lookup of Queue failed: "
                                 + "queueName= " + queueName
                                 + " message=" + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,  e);
            throw new EventProcessingException(msg, e);
        }
        
        //
        //  Create the queue connection, session, and sender.
        //
        try
        {
            mQueueConnection = connectionFactory.createQueueConnection();
            mQueueSession = mQueueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
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
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,  e);
            throw new EventProcessingException(msg, e);
        }
        
        mInitialized = true;
    }
    
    /**
     *  Shutdown the queue.
     * 
     * @throws EventProcessingException  An error occured during the shutdown.
     */
    void shutdown() throws EventProcessingException
    {
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
            String msg = "Failed to shutdown EventQueue: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,  e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    /**
     *  Send an event to the queue.
     * 
     *  @param event  The event to be sent.
     * 
     *  @throws IllegalStateException  Not initialized before call.
     * 
     *  @throws EventProcessingException  Error while attempting to send the event.
     */
    void sendEvent(CommittedEventKey event) throws IllegalStateException, EventProcessingException
    {
        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(EventQueue.class.getName()
                                            + " not initialized");
        }

        try
        {
            if (sE1Logger.isDebug())
            {
                StringBuffer buffer = new StringBuffer(150);
                buffer.append("Sending committed event:");
                buffer.append(" category=").append(event.getCategory());
                buffer.append(" type=").append(event.getType());
                buffer.append(" environment=").append(event.getOriginatingEnvironment());
                buffer.append(" userSession=").append(event.getOriginatingUserSession());
                buffer.append(" seqNum=").append(event.getSequenceNumber());
                buffer.append(" queueIndex=").append(mQueueIndex);
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, buffer.toString(),null, null, null);
            }
            ObjectMessage message = mQueueSession.createObjectMessage(event);
            mQueueSender.send(message);
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Committed event message sent", null, null, null);
            }
        }
        catch (JMSException e)
        {
            String msg = "Failed to send committed event message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    /**
     *  Commit any sends made on the queue.
     * 
     *  @throws EventProcessingException  Error while attempting to commit.
     */
    void commit() throws EventProcessingException
    {
        try
        {
            mQueueSession.commit();
        }
        catch (JMSException e)
        {
            String msg = "Failed to commit event message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    /**
     *  Rollback any sends made on the queue.
     * 
     *  @throws EventProcessingException  Error while attempting to rollback.
     */
    void rollback() throws EventProcessingException
    {
        try
        {
            mQueueSession.rollback();
        }
        catch (JMSException e)
        {
            String msg = "Failed to rollback event message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    private static synchronized int getNumEventQueues()
    {
        int numQueues = JdeProperty.getProperty(EVENT_SECTION, NUM_QUEUES_PROP, 1);
        
        //
        //  Make sure the configured number of queues is not out of range.
        //
        if (numQueues > MAX_EVENT_QUEUES)
        {
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, "Configured event concurrency reduced from " + numQueues
                         + " to " + MAX_EVENT_QUEUES, null, null, null);
            numQueues = MAX_EVENT_QUEUES;
        }
        
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Event processing conurrency = " + numQueues, null, null, null);
        
        return numQueues;
    }
    
    /**
     *  Generate the JNDI name of a queue based on its index.  The JNDI name is generated
     *  by appending the two digit queue index to the end of the root event queue name.
     *  The assumption is that there will never be more then 100 event processors.
     */
    private String getQueueJNDIName(int i)
    {
        //
        //  Make sure the index is not to big.
        //
        if (i > 100)
        {
            throw new IndexOutOfBoundsException("Event queue index to large, index=" + i);
        }
        
        final int TEN = 10;
        String name = JMSNames.EVENT_Q_ROOT;
        int tens = (i / TEN) % TEN;
        int ones = i % TEN;
        name += Integer.toString(tens) + Integer.toString(ones);
        return name;
    }
}
