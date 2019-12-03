//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.common.events.monitoring.internal;

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
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
//=================================================
//Imports from org namespace
//=================================================

/**
 * Remote interface for Enterprise Bean: EventMonitoring.
 */
public interface EventMonitoring extends javax.ejb.EJBObject
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
     * This method calls the EventMonitoringLogic to get all subscribers.
     * @return LinkedList collection of subscribers.
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    LinkedList getAllSubscribers() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method returns the failed events information.
     * @return ArrayList
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    ArrayList getFailedEventsInfo() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method delets the failed events.
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    void deleteAllFailedEvents() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * Delete a single failed event.
     * @param eventID  ID of the event to be deleted.
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    void deleteFailedEvent(String eventID)
        throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method returns the events metrics.
     * @return EventMessage
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    HashMap getEventMetrics() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method returns the current storage space of all queus
     * on a file system.
     * @return long
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    long getCurrentQueueStorageSpace() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method returns the storage space remaining of all queus
     * on a file system.
     * @return long
     * @throws java.rmi.RemoteException ex
     * @throws EventProcessingException ex
     */
    long getStorageSpaceRemaining() throws java.rmi.RemoteException, EventProcessingException;
    
    /**
     * This method returns the number of messages in a 
     *  particular queue.
     * @param  subscriberID string
     * @return Number of messages.
     * @throws EventProcessingException ex
     * @throws java.rmi.RemoteException ex
     */
    long getNumberOfQueueMessages(String subscriberID)
        throws EventProcessingException, java.rmi.RemoteException;

    /**
     * This method returns the number of messages that have been routed to the subscriber
     * since the subscriber was created.
     * 
     * @param subcriberID   EnterpriseOne username of the subscriber.
     * @return  The number of events routed to the subscriber.
     * @throws EventProcessingException ex
     * @throws java.rmi.RemoteException ex
     */
    long getNumberOfRoutedMessages(String subcriberID) 
        throws EventProcessingException, java.rmi.RemoteException;
    
    /**
     * Purge all messages fropm a subscriber's queue.
     * @param  subscriberID   EnterpriseOne username of the subscriber.
     * @throws EventProcessingException ex
     * @throws java.rmi.RemoteException ex
     */
    void purgeSubscriberQueue(String subscriberID)
        throws EventProcessingException, java.rmi.RemoteException;
}
