//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.common.events.monitoring;

//=================================================
//Imports from java namespace
//=================================================
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.internal.EventMonitoring;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Implements the Monitoring Service EJB client.
 */
public class EventMonitoringImpl implements IEventMonitoring
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventMonitoringImpl.class.getName());
    //=================================================
    // Instance member fields.
    //=================================================
    private EventMonitoring mEventMonitoringEJB = null;
    //=================================================
    // Constructors.
    //=================================================
    /**
     *  Constructor.
     * 
     *  @param  ejb  Remote interface to the admin service EJB.
     */
    EventMonitoringImpl(EventMonitoring ejb)
    {
        if (ejb == null)
        {
            throw new NullPointerException();
        }
        mEventMonitoringEJB = ejb;
    }
    
    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  {@inheritDoc}
     */
    public LinkedList getAllSubscribers() throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getAllSubscribers();
        }
        catch (RemoteException e)
        {
            String msg = "Failed to get All subcribers. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Failed to get All subcribers. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    public void getAllSubscribersMangementConsole() throws EventMonitoringException
    {
        try
        {
            mEventMonitoringEJB.getAllSubscribers();
        }
        catch (RemoteException e)
        {
            String msg = "Failed to get All subcribers. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Failed to get All subcribers. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }

    /**
     *  {@inheritDoc}
     */
    public ArrayList getFailedEventsInfo() throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getFailedEventsInfo();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Failed Events Info. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error getting Failed Events Info. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    public void getFailedEventsInfoManagementConsole() throws EventMonitoringException
    {
        try
        {
             mEventMonitoringEJB.getFailedEventsInfo();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Failed Events Info. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error getting Failed Events Info. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    /**
     *  {@inheritDoc}
     */
    public void deleteAllFailedEvents() throws EventMonitoringException
    {
        try
        {
            mEventMonitoringEJB.deleteAllFailedEvents();
        }
        catch (RemoteException e)
        {
            String msg = "Error deleting failed events. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error deleting failed events. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    /**
     *  {@inheritDoc}
     */
    public void deleteFailedEvent(String eventID) throws EventMonitoringException
    {
        try
        {
            mEventMonitoringEJB.deleteFailedEvent(eventID);
        }
        catch (RemoteException e)
        {
            String msg = "Error deleting one failed event. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error deleting one failed event. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    /**
      *  {@inheritDoc}
      */
    public HashMap getEventMetrics() throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getEventMetrics();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Events Metrics. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error getting Events Metrics. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
    public void getEventMetricsManagementConsole() throws EventMonitoringException
    {
        try
        {
            mEventMonitoringEJB.getEventMetrics();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Events Metrics. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error getting Events Metrics. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }

    /**
     *  {@inheritDoc}
     */
    public long getNumberOfQueueMessages(String subcriberID) throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getNumberOfQueueMessages(subcriberID);
        }
        catch (RemoteException e)
        {
            String msg =
                "Error getting number of messages in queue. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg =
                "Error getting number of messages in queue. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }

    /**
     *  {@inheritDoc}
     */
    public long getNumberOfRoutedMessages(String subcriberID) throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getNumberOfRoutedMessages(subcriberID);
        }
        catch (RemoteException e)
        {
            String msg =
                "Error getting number of routed messages. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg =
                "Error getting number of routed messages. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void purgeSubscriberQueue(String subcriberID) throws EventMonitoringException
    {
        try
        {
            mEventMonitoringEJB.purgeSubscriberQueue(subcriberID);
        }
        catch (RemoteException e)
        {
            String msg =
                "Error purging subscriber queue. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg =
                "Error purging subscriber queue." + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }

    /**
     *  {@inheritDoc}
     */
    public long getCurrentQueueStorageSpace() throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getCurrentQueueStorageSpace();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Queue storage space. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
        catch (EventProcessingException e)
        {
            String msg = "Error getting Queue storage space. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }

    }
    
    /**
     *  {@inheritDoc}
     */
    public long getStorageSpaceRemaining() throws EventMonitoringException
    {
        try
        {
            return mEventMonitoringEJB.getStorageSpaceRemaining();
        }
        catch (RemoteException e)
        {
            String msg = "Error getting Queue storage space. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        } 
        catch (EventProcessingException e)
        {
            String msg = "Error getting Queue storage space. " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventMonitoringException(msg, e);
        }
    }
    
}
