//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.common.events.connectorsvc.internal;


//=================================================
// Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;

//=================================================
// Imports from java namespace
//=================================================
import java.rmi.RemoteException;

import java.util.LinkedList;

//=================================================
// Imports from javax namespace
//=================================================
import javax.ejb.EJBObject;


//=================================================
// Imports from org namespace
//=================================================

/**
 * Remote interface for Enterprise Bean: ConnectorService.  This is duplicate interface
 * to the com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService that must
 * remain identical and must additionally throw a RemoteException for each method.
 */
public interface ConnectorService extends EJBObject {
    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Methods.
    //=================================================

    /**
     * Returns the JMS queue's JNDI name for the given username.  The SecurityServerResponse
     * is used to ensure that the username has an active EnterpriseOne session.
     *
     * @param credentials the credential information needed to validate the user
     * @return JNDI name of the queue or null if no subscriber is found for the given username
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     * @throws RemoteException if there is a problem during the EJB method call
     */
    String getQueueJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, 
            EventProcessingException, RemoteException;

    /**
     * Returns the JNDI name of the queue connection factory.
     *
     * @param credentials the credential information needed to validate the user
     * @return JNDI name of the queue connection factory
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if other Event Processing System errors occur
     * @throws RemoteException if there is a problem during the EJB method call
     */
    String getQueueConFactJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException, 
            RemoteException;

    /**
     * Retrieves all the subscriptions for the user.
     *
     * @param credentials the credential information needed to validate the user
     * @return the Collection of subscriptions
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     * @throws RemoteException if there is a problem during the EJB method call
     */
    LinkedList getSubscriptions(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, 
            EventProcessingException, RemoteException;

    /**
     * Retrieves the event template for the given event category, event type,
     * and EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param host the EnterpriseOne server name to be used to retrieve Z event templates
     * @param port the EnterpriseOne server port to be used to retrieve Z event templates
     * @param category the event category
     * @param type the event type
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return the event template
     * @throws NotSupportedException thrown when the event template request for
     *                               the given event category is not supported
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the event processing system encounters
     *                                  other errors handling this request
     * @throws RemoteException if there is a problem during the EJB method call
     */
    String getEventTemplate(ConnectorCredentials credentials, String host,
        int port, String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException, 
            EventProcessingException, RemoteException;
    /**
     * Retrieves the event schema for the given event category, event type,
     * and EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param host the EnterpriseOne server name to be used to retrieve Z event templates
     * @param port the EnterpriseOne server port to be used to retrieve Z event templates
     * @param category the event category
     * @param type the event type
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return the event schema
     * @throws NotSupportedException thrown when the event template request for
     *                               the given event category is not supported
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the event processing system encounters
     *                                  other errors handling this request
     * @throws RemoteException if there is a problem during the EJB method call
     */
    String getEventSchema(ConnectorCredentials credentials, String host,
        int port, String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException, 
            EventProcessingException, RemoteException;       
   
    /**
     * Retrieves the list of event types, both active and inactive,
     * that are valid for a given environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return an Enumeration of the event types
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     * @throws RemoteException if there is a problem during the EJB method call
     */
    LinkedList getEventList(ConnectorCredentials credentials, String environment)
        throws InvalidCredentialsException, EventProcessingException, 
            RemoteException;

    /**
     * Retrieves the list of all event types, both active and inactive,
     * for any EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @return a LinkedList of the event types as Strings
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     * @throws RemoteException if there is a problem during the EJB method call
     */
    LinkedList getEventList(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException, 
            RemoteException;
    
    /**
     * Retrieves the list of event types irrespective of subscriber
     * that are valid for a given environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return an Enumeration of the event types
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     * @throws RemoteException if there is a problem during the EJB method call
     */
    LinkedList getEventListForEnv(ConnectorCredentials credentials, String environment)
        throws InvalidCredentialsException, EventProcessingException, 
            RemoteException;
            
    /**
     * Retrieves the list of <code>com.peoplesoft.pt.e1.common.events.connectorsvc.
     * MSMQSubscriber</code> objects that are currently in the SubscriberCache.
     *
     * @return the list of MSMQSubscriber objects
     * @throws EventProcessingException if the SubscriberCache cannot retrieve the list
     *                                  of MSMQ subscribers
     * @throws RemoteException if there is a problem during the EJB method call
     */
    LinkedList getMSMQInfo() throws EventProcessingException, RemoteException;

    //checks if the user is an MSMQ subscriber...Used to bypass security
    boolean isMSMQUser(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException, 
            RemoteException;
}
