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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Singleton class for managing client session objects.
 */
final class EventClientSessionManager
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventClientSessionManager.class.getName());
    
    /**  This hash map contains the currently active event sessions.  */
    private static HashMap sEventSessions = new HashMap();
    
    private static final String SECTION = "EVENTS";
    
    private static final String SESSION_CHECK_INTERVAL = "eventClientSessionCheckInterval";
    private static final String SESSION_TIMEOUT = "eventClientSessionTimeout";
    private static final String USE_EMBEDDED_MQ = "useEmbeddedMQ";
    
    private static final long SESSION_CHECK_INTERVAL_DEFAULT = 60000;
    private static final long SESSION_TIMEOUT_DEFAULT = 3600000;
    
    private static long sSessionCheckInterval = SESSION_CHECK_INTERVAL_DEFAULT;
    private static long sSessionTimeout = SESSION_TIMEOUT_DEFAULT;
    
    private static Timer sTimer;

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    static 
    {
        //
        //  Read INI file values.
        //
        String intervalString =
            JdeProperty.getProperty(
                SECTION,
                SESSION_CHECK_INTERVAL,
                Long.toString(SESSION_CHECK_INTERVAL_DEFAULT));
        if ((intervalString != null) && (intervalString.length() > 0))
        {
            sSessionCheckInterval = Long.parseLong(intervalString);
        }
        String timeoutString =
            JdeProperty.getProperty(
                SECTION,
                SESSION_TIMEOUT,
                Long.toString(SESSION_TIMEOUT_DEFAULT));
        if ((timeoutString != null) && (timeoutString.length() > 0))
        {
            sSessionTimeout = Long.parseLong(timeoutString);
        }
        
        //
        //  Start a timer for removing expired event sessions.
        //
        sTimer = new Timer(true);
        sTimer.schedule(new SessionCheck(), sSessionCheckInterval, sSessionCheckInterval);
    }
    
    /**
     *  Don't allow instances to be created.
     */
    private EventClientSessionManager()
    {}

    //=================================================
    // Methods.
    //=================================================
        
    /**
     *  Create a new event session.
     * 
     *  @param credentials  credentials of the event subscriber creating the session.
     * 
     *  @return the event session.
     * 
     *  @throws EventProcessingException  an error occured while creating the session.
     */
    static EventClientSession createSession(ConnectorCredentials credentials)
        throws EventProcessingException
    {
        //
        //  Get the connection factor and queue JNDI names.
        //
        IConnectorService service = ConnectorServiceFactory.getConnectorService();
        String connFactoryJndi = service.getQueueConFactJNDIName(credentials);
        String queueJndi = service.getQueueJNDIName(credentials);
        
        //
        //  Create a new session.
        //
        String sessionId = generateSessionId();
        EventClientSession session = null;
        if (useEmbeddedMQ())
        {
            session =
                new EventClientSessionEmbeddedMQ(
                    sessionId,
                    credentials,
                    connFactoryJndi,
                    queueJndi);
        }
        else
        {
            session =
                new EventClientSessionStdJms(sessionId, credentials, connFactoryJndi, queueJndi);
        }
        
        //
        //  Initialize the session.
        //
        try
        {
            session.initialize();
        }
        catch (Exception e)
        {
            String msg = "failed to initialize event client session: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        //
        //  Save the session in the hash map.
        //
        putSession(session);
        
        return session;
    }
    
    /**
     *  Get an existing event session.
     * 
     *  @param sessionId  the session ID.
     * 
     *  @return  the session or <code>null</code> if no session exists.
     */
    static EventClientSession getSession(String sessionId)
    {
        EventClientSession session = null;
        synchronized(sEventSessions)
        {
            session = (EventClientSession)sEventSessions.get(sessionId);
        }
        return session;
    }
    
    /**
     *  Removes an existing session.
     * 
     *  @param sessionId  session ID of the session to remove.
     */
    static void removeSession(String sessionId)
    {
        EventClientSession session = null;
        synchronized(sEventSessions)
        {
            session = (EventClientSession)sEventSessions.remove(sessionId);
        }
        if (session != null)
        {
            //
            //  Make sure the session is closed.
            //
            try
            {
                session.close();
            }
            catch (Throwable t)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"failed to close event session: " + t.getMessage(),null,null, t);
            }
        }
    }
    
    private static void putSession(EventClientSession session)
    {
        synchronized(sEventSessions)
        {
            sEventSessions.put(session.getSessionId(), session);
        }
    }
    
    /**
     *  Generate a new unique session ID.
     * 
     *  @return  a new session ID.
     */
    private static String generateSessionId()
    {
        //
        //  Construct a random number seed.
        //
        long seed = Runtime.getRuntime().freeMemory();
        seed = seed ^ Runtime.getRuntime().totalMemory();
        seed = seed ^ System.currentTimeMillis();
        Random random = new Random(seed);
        
        boolean done = false;
        String value = null;
        while (!done)
        {
            //
            //  Generate a random session ID.
            //
            StringBuffer buffer = new StringBuffer();
            buffer.append(Math.abs(random.nextLong()));
            buffer.append(Math.abs(random.nextLong()));
            value = buffer.toString();
            
            //
            //  Verify that it's not already in use.
            //
            if (getSession(value) == null)
            {
                done = true;
            }
        }
        
        return value;
    }
    
    /**
     *  Determine if IBM Embedded MQ or standard JMS should be used.
     */
    private static boolean useEmbeddedMQ()
    {
        boolean result = JdeProperty.getProperty(SECTION, USE_EMBEDDED_MQ, false);
        return result;
    }
    
    /**
     *  Timer task class the check for expired sessions.
     */
    private static class SessionCheck extends TimerTask
    {
        public void run()
        {
            synchronized(sEventSessions)
            {
                //
                //  Run through all the session checking to see if any have expired.
                //
                long now = System.currentTimeMillis();
                for (Iterator iter = sEventSessions.values().iterator();iter.hasNext();)
                {
                    EventClientSession session = (EventClientSession)iter.next();
                    long delta = now - session.getLastAccessTime();
                    if (delta > sSessionTimeout)
                    {
                        //
                        //  The session has expired.
                        //
                        try
                        {
                            if (sE1Logger.isDebug())
                            {
                                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                                    "removing expired event session, ID=" + session.getSessionId(), null,null, null);
                            }
                            session.close();
                            iter.remove();                            
                        }
                        catch (Throwable t)
                        {
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                "failed to close expired event session: " + t.getMessage(),null, null, t);
                        }
                    }
                }
            }
        }
    }
}
