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

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.net.JdeConnectionManager;
import com.jdedwards.system.net.JdeNetConnectFailedException;
import com.jdedwards.system.net.JdeNetHostException;
import com.jdedwards.system.net.JdeNetTimeoutException;
import com.jdedwards.system.net.JdeSocket;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSQueue;
//=================================================
//Imports from org namespace
//=================================================

//=================================================
//Imports from junit namespace
//=================================================

/**
 * This class manages JMSQueue and JDENet connections.
 */
class AdaptorUtil
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    private static E1Logger sE1Logger =
            JdeLog.getE1Logger(AdaptorUtil.class.getName());
    
    private static AdaptorUtil sUtil = new AdaptorUtil();         
    //=================================================
    // Public static final fields.
    //=================================================  
    
    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    /**
     * Returns the new instance of this class.
     * @return JMSQueueUtility instance
     */
    static AdaptorUtil getInstance()
    {
        return sUtil;
    }
      
    /**
     * This method creates a JMS queue connection and returns a JMSQueue.
     * @param queueName name of the queue, to open the connection. 
     * @param queueFactory name of the factory where the queue is located.
     * @param acknowledge queue session aknowledge value.
     * @param transacted  should the session be transacted?
     * @return JMSQueue queue object. 
     * @throws EventProcessingException ex
     */
    JMSQueue createQueueConnection(String queueName, 
                                          String queueFactory, 
                                          int acknowledge,
                                          boolean transacted)
        throws EventProcessingException
    {    
        JMSQueue queue = new JMSQueue(queueName, queueFactory, transacted, acknowledge);
        try
        {
            queue.initialize();
        }
        catch(EventProcessingException ex)
        {
            String msg = "Error creating queue connection for queu Name: " + queueName + ex;
            throw new EventProcessingException(msg, ex);
        }
        return queue;
    }
   
    /**
     *  Creates a jdenet socket connection. 
     *  @return JdeSocket a socket.
     *  @param host String 
     *  @param port int 
     *  @param timeout int
     *  @throws AdaptorException ex
     */
    JdeSocket createJDENetConnection(String host, int port, int timeout) 
        throws AdaptorException
    {
        JdeSocket soc = null;
        try
        {             
            soc = JdeConnectionManager.getManager().checkout(host, port, timeout);
            soc.setTimeout(timeout);
        }
        catch (JdeNetConnectFailedException e)
        {
            String msg = "JDENet connect failed. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new AdaptorException(msg, e);
        }
        catch (JdeNetHostException e)
        {
            String msg = "JDENet connect failed. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new AdaptorException(msg, e);
        }
        catch (JdeNetTimeoutException e)
        {
            /**
             * Socket is still good
             */
            String msg = "JDENet sock timed out. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null,null, e);
            throw new AdaptorException(msg, e);
        }
        finally
        {
            /**
             * If the connection is BAD, notify the pool that this connection is bad.
             */
            if (soc != null)
            {
                if (soc.isSocketGood())
                {
                    JdeConnectionManager.getManager().checkin(soc);
                }  
                else
                {
                    JdeConnectionManager.getManager().checkinBad(soc);
                }             
            }
        }
        return soc;
    }
    
    /**
     *  Return a bad JDENet connection.
     * 
     *  @param socket  The bad socket.
     */
    void returnBadSocket(JdeSocket socket)
    {
        JdeConnectionManager.getManager().checkinBad(socket);
    }

    /**
     * 
     */
    public void shutdown()
    {
    }
}
