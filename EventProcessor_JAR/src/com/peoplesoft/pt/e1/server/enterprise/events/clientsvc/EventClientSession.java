//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.clientsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.clientsvc.ClientSessionClosedException;
import com.peoplesoft.pt.e1.common.events.clientsvc.ClientSessionStoppedException;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  This class represents a remote event client session.
 */
abstract class EventClientSession
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventClientSession.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    private long mLastAccessTime;

    private String mSessionId;
    
    private String mConnectionFactoryJndiName;
    
    private String mQueueJndiName;
    
    /** Used to create the QueueSession member. */
    private QueueConnection mQueueConnection = null;

    /** Used to create the QueueReceiver member. */
    private QueueSession mQueueSession = null;

    /** Used to receive JMS messages. */
    private QueueReceiver mQueueReceiver = null;
    
    private boolean mIsInitialized = false;
    
    private boolean mIsStarted = false;
    
    private boolean mIsClosed = true;
    
    private Message mLastUnacknowledgedMessage = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param sessionId  the ID of the session.
     * 
     *  @param credentials  credentials of the event subscriber.
     * 
     *  @param connFactoryJndi   JNDI name of the queue connection factory.
     * 
     *  @param queueJndi  JNDI name of the JMS queue.
     */
    protected EventClientSession(
        String sessionId,
        ConnectorCredentials credentials,
        String connFactoryJndi,
        String queueJndi)
    {
        updateAccessTime();

        mSessionId = sessionId;
        mConnectionFactoryJndiName = connFactoryJndi;
        mQueueJndiName = queueJndi;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Initialize the session.
     * 
     *  @throws EventProcessingException  error during initialization.
     */
    abstract void initialize()
        throws EventProcessingException;
    
    /**
     *  Initialize the session with the given factory and queue.  This method
     *  is intended to be used by sub-classes.
     * 
     *  @param queueConFact  queue connection factory to use.
     * 
     *  @param queue  the JMS queue to use.
     * 
     *  @throws EventProcessingException  error during initialization.
     */
    void initializeSession(QueueConnectionFactory queueConFact, Queue queue)
        throws EventProcessingException
    {
        updateAccessTime();
                
        //
        //  Create the QueueConnection, QueueSession, and QueueReceiver objects.
        //
        try
        {
            mQueueConnection = queueConFact.createQueueConnection();
            mQueueSession = mQueueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            mQueueReceiver = mQueueSession.createReceiver(queue);
        }
        catch (JMSException e)
        {
            String msg = "error creating necessary JMS queue session objects.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null,null, e);
            throw new EventProcessingException(msg, e);
        }
             
        mIsInitialized = true;
        mIsClosed = false;
    }

    /**
     *  Acknowledges all consumed events of the session.
     * 
     *  @throws ClientSessionStoppedException  the session is currently stopped.
     * 
     *  @throws ClientSessionClosedException  the session has already been closed.
     * 
     *  @throws EventProcessingException  error acknowledging events.
     */
    public void acknowledgeEvents()
        throws ClientSessionStoppedException, ClientSessionClosedException, EventProcessingException
    {
        updateAccessTime();
        checkIsInitialized();
        checkIsClosed();
        checkIsStopped();
        
        if (mLastUnacknowledgedMessage != null)
        {
            try
            {
                mLastUnacknowledgedMessage.acknowledge();
                mLastUnacknowledgedMessage = null;
            }
            catch (JMSException e)
            {
                String msg = "failed to acknowledge event: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,  e);
                throw new EventProcessingException(msg, e);
            }
        }
    }
    
    /**
     *  Receive the next available event.  If an event is not available the caller
     *  Will block until either an event is available for delivery or the specified
     *  time-out period has elapsed.  A timeout of zero never expires, and the call
     *  blocks indefinitely.
     * 
     *  @param timeout the timeout value (in milliseconds).
     * 
     *  @return the event or <code>null</code> if no event was available within the
     *          timeout period.
     * 
     *  @throws ClientSessionStoppedException  the session is currently stopped.
     * 
     *  @throws ClientSessionClosedException  the session has already been closed.
     * 
     *  @throws EventProcessingException  error receiving events.
     */
    public EventMessage receive(long timeout) 
        throws ClientSessionStoppedException, ClientSessionClosedException, EventProcessingException
    {
        updateAccessTime();
        checkIsInitialized();
        checkIsClosed();
        checkIsStopped();
        
        try
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "event client calling receive, sessionId="
                        + mSessionId
                        + ", timeout="
                        + timeout, null, null, null);
            }
            
            EventMessage event = null;
            Message jmsMessage = mQueueReceiver.receive(timeout);
            if (jmsMessage == null)
            {
                event = null;
            }
            else if (jmsMessage instanceof ObjectMessage)
            {
                Object obj = ((ObjectMessage)jmsMessage).getObject();
                if (obj instanceof EventMessage)
                {
                    event = (EventMessage)obj;
                    if (event != null)
                    {
                        mLastUnacknowledgedMessage = jmsMessage;
                    }
                }
                else
                {
                    String msg = "unknown object type in JMS message: " + obj.getClass().getName();
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
                    throw new EventProcessingException(msg);
                }
            }
            else
            {
                String msg = "unknown JMS message type: " + jmsMessage.getClass().getName();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                throw new EventProcessingException(msg);
            }
            
            return event;
        }
        catch (JMSException e)
        {
            String msg = "error receiving JMS message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    /**
     *  Close the event session.
     * 
     *  @throws EventProcessingException  error closing the session.
     */
    public void close() throws EventProcessingException
    {
        updateAccessTime();
        checkIsInitialized();

        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"closing event client session, sessionId=" + mSessionId, null, null, null);
        }

        try
        {
            if (mIsStarted)
            {
                stop();
            }
            try
            {
                if (mQueueReceiver != null)
                {
                    mQueueReceiver.close();
                    mQueueReceiver = null;
                }
                if (mQueueSession != null)
                {
                    mQueueSession.close();
                    mQueueSession = null;
                }
                if (mQueueConnection != null)
                {
                    mQueueConnection.close();
                    mQueueConnection = null;
                }
            }
            catch (JMSException e)
            {
                String msg = "error closing JMS queue session";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        finally
        {
            mIsClosed = true;
        }
    }
    
    /**
     *  Stop the current event session.
     * 
     *  @throws ClientSessionClosedException  the session has already been closed.
     * 
     *  @throws EventProcessingException  error stopping the session.
     */
    public void stop() throws ClientSessionClosedException, EventProcessingException
    {
        updateAccessTime();
        checkIsInitialized();
        checkIsClosed();
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"stopping event client session, sessionId=" + mSessionId, null, null, null);
        }
        
        mIsStarted = false;
    }
    
    /**
     *  Start the event session.
     * 
     *  @throws ClientSessionClosedException  the session has already been closed.
     * 
     *  @throws EventProcessingException  error starting the session.
     */
    public void start() throws ClientSessionClosedException, EventProcessingException
    {
        updateAccessTime();
        checkIsInitialized();
        checkIsClosed();
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"starting event client session, sessionId=" + mSessionId, null, null, null);
        }
        
        try
        {
            mQueueConnection.start();
            mIsStarted = true;
        }
        catch (JMSException e)
        {
            String msg = "error starting JMS queue session";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    /**
     *  Get the time the session was last accessed.
     * 
     *  @return  time of last session access.
     */
    public long getLastAccessTime()
    {
        return mLastAccessTime;
    }
    
    /**
     *  Get the session's session ID.
     *  
     *  @return the session ID.
     */
    public String getSessionId()
    {
        return mSessionId;
    }
    
    private void updateAccessTime()
    {
        mLastAccessTime = System.currentTimeMillis();
    }
    
    private void checkIsInitialized() throws EventProcessingException
    {
        if (!mIsInitialized)
        {
            String msg = "event session not initialized";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
            throw new EventProcessingException(msg);
        }
    }
    
    private void checkIsClosed() throws ClientSessionClosedException
    {
        if (mIsClosed)
        {
            String msg = "event session has already been closed";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new ClientSessionClosedException(msg);
        }
    }
    
    private void checkIsStopped() throws ClientSessionStoppedException
    {
        if (!mIsStarted)
        {
            String msg = "event session has already been closed";
            throw new ClientSessionStoppedException(msg);
        }
    }
    
    /**
     * @return  connection factory JNDI name.
     */
    String getConnectionFactoryJndiName()
    {
        return mConnectionFactoryJndiName;
    }

    /**
     * @return  queue JNDI name.
     */
    String getQueueJndiName()
    {
        return mQueueJndiName;
    }
}
