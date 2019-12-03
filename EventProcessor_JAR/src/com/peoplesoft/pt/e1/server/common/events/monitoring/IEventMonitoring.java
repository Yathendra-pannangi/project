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

/**
 * Interface for event monitoring.
 */
public interface IEventMonitoring
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
     * This method returns all subscribers.
     * @return List of <code>SubscriberMonitoringInfo</code> objects.
     * @throws EventMonitoringException ex
     */
    LinkedList getAllSubscribers() throws EventMonitoringException;

    /**
     * This method returns the failed events information.
     * @return A list of <code>FailedEventMessage</code> objects.
     * @throws EventMonitoringException ex
     */
    ArrayList getFailedEventsInfo() throws EventMonitoringException;
    
    /**
     * Delete all of the failed events.
     * @throws EventMonitoringException ex
     */
    void deleteAllFailedEvents() throws EventMonitoringException;
    
    /**
     * Delete a single failed event.
     * @param eventID  ID of the event to be deleted.
     * @throws EventMonitoringException ex
     */
    void deleteFailedEvent(String eventID) throws EventMonitoringException;
    
    /**
     * This method returns the events metrics.
     * @return <code>HashMap</code> of metric values.  The keys for the hash map are
     *         defined in the <code>EventMetrics</code> class.  The values in the hash map
     *         are of type <code>Long</code>.
     * @throws EventMonitoringException ex
     */
    HashMap getEventMetrics() throws EventMonitoringException;

    /**
     * This method returns the number of messages in a particular subscriber's queue.  Note,
     * this value is a snapshot in time and a second call this this method can return a different
     * value.
     * 
     * @param subcriberID   EnterpriseOne username of the subscriber.
     * @return  The number of events currently in the subscriber's queue.
     * @throws EventMonitoringException ex
     */
    long getNumberOfQueueMessages(String subcriberID) throws EventMonitoringException;

    /**
     * This method returns the number of messages that have been routed to the subscriber
     * since the subscriber was created.
     * 
     * @param subcriberID   EnterpriseOne username of the subscriber.
     * @return  The number of events routed to the subscriber.
     * @throws EventMonitoringException ex
     */
    long getNumberOfRoutedMessages(String subcriberID) throws EventMonitoringException;
    
    /**
     * Purge all messages fropm a subscriber's queue.
     * @param  subscriberID   EnterpriseOne username of the subscriber.
     * @throws EventMonitoringException ex
     */
    void purgeSubscriberQueue(String subscriberID) throws EventMonitoringException;

    /**
     * This method returns the current storage space of all queus
     * on a file system.
     * @return long
     * @throws EventMonitoringException ex
     */
    long getCurrentQueueStorageSpace() throws EventMonitoringException;
    
    /**
     * This method returns the storage space available of all queus
     * on a file system.
     * @return long
     * @throws EventMonitoringException ex
     */
    long getStorageSpaceRemaining() throws EventMonitoringException;

}
