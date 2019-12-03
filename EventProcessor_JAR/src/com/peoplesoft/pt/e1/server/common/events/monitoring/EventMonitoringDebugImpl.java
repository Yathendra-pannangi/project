//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.common.events.monitoring;

//=================================================
//Imports from java namespace
//=================================================
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================

//=================================================
//Imports from org namespace
//=================================================

//=================================================
//Imports from junit namespace
//=================================================

/**
 * This class is created to help saw debug.
 */
public class EventMonitoringDebugImpl implements IEventMonitoring
{
    //=================================================
    // Non-public static class fields.
    //=================================================

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
     *  {@inheritDoc}
     */
    public LinkedList getAllSubscribers() throws EventMonitoringException
    {
        LinkedList subscribers = new LinkedList();
        for(int i=0;i<2;i++)
        {
            SubscriberMonitoringInfo subInfo = new SubscriberMonitoringInfo();
            subInfo.setDescription("This is a test subcriber#" + " " + i);
            subInfo.setUsername("UserName"+i);
            subInfo.setIsActive(true);
            subscribers.add(subInfo);       
        }  
        return subscribers;     
    }
    
    /**
     *  {@inheritDoc}
     */
    public ArrayList getFailedEventsInfo() throws EventMonitoringException
    {
        ArrayList list = new ArrayList();
        for(int i=0;i<2;i++)
        {
            FailedEventMessage event = new FailedEventMessage();
            event.setEventID("1" +i);
            event.setType("RTE");
            event.setCategory("");
            event.setEnvironment("TEDVNIS2");
            event.setSequenceNumber(1245 + i);
            event.setDateTime(null);
            event.setHost("LocalHost");
            event.setBSFN("BSFN");
            event.setFailedMessage("This is a failed message# " + i);
            list.add(event);  
        }          
        return list;  
    }
    
    /**
     *  {@inheritDoc}
     */
    public void deleteAllFailedEvents() throws EventMonitoringException
    {
        return;
    }
    
    /**
     *  {@inheritDoc}
     */
    public void deleteFailedEvent(String eventID) throws EventMonitoringException
    {
        return;
    }
    
    /**
      *  {@inheritDoc}
      */
    public HashMap getEventMetrics() throws EventMonitoringException
    {
        HashMap map = new HashMap();
        for(int i=0;i<2;i++)
        {   
            EventMetrics metrics = new EventMetrics();
            metrics.setMatricName("Metric# " +i);
            metrics.setMatricCount(1);       
            map.put("Metric# " + i, metrics);          
        }      
        return map;      
    } 

    /**
     *  {@inheritDoc}
     */
    public long getNumberOfQueueMessages(String subcriberID) throws EventMonitoringException
    {   
        return 5;              
    }

    /**
     *  {@inheritDoc}
     */
    public long getNumberOfRoutedMessages(String subcriberID) throws EventMonitoringException
    {   
        return 23;              
    }

    /**
     *  {@inheritDoc}
     */
    public void purgeSubscriberQueue(String subcriberID) throws EventMonitoringException
    {   
        return;              
    }
    
    /**
     *  {@inheritDoc}
     */
    public long getCurrentQueueStorageSpace() throws EventMonitoringException
    {
        return 12679;
    }
    
    /**
     *  {@inheritDoc}
     */
    public long getStorageSpaceRemaining() throws EventMonitoringException
    {
        return 1267;
    }
}
