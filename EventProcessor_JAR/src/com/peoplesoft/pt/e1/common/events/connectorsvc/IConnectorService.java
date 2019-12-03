//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.connectorsvc;

//=================================================
// Imports from java namespace
//=================================================
import java.util.LinkedList;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * The root interface for the Connector Services portion of the event processing
 * system.  It defines the main methods that Connector clients will call
 * through an EJB interface.
 */
public interface IConnectorService
{
    //=================================================
    // Public static final fields.
    //=================================================

    // The following event categories must match those of the F90701.EDEVNTTYPE field.
    /** The Real-Time Event category. */
    String CATEGORY_REALTIME = "RTE";
    /** The Workflow Event category. */
    String CATEGORY_WORKFLOW = "WF";
    /** The XAPI Event category. */
    String CATEGORY_XAPI     = "XAPI";
    /** The Z-File Event category. */
    String CATEGORY_ZFILE    = "ZFILE";

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Returns the JMS queue's JNDI name for the given username.
     * 
     * @param credentials the credential information needed to validate the user
     * @return JNDI name of the queue or null if no subscriber is found for the given username
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     */
    String getQueueJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, EventProcessingException;

    /**
     * Returns the JNDI name of the queue connection factory.
     * 
     * @param credentials the credential information needed to validate the user
     * @return JNDI name of the queue connection factory
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if other Event Processing System errors occur
     */
    String getQueueConFactJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException;
    
    /**
     * Retrieves all the subscriptions for the user.
     * 
     * @param credentials the credential information needed to validate the user
     * @return a LinkedList of com.peoplesoft.pt.e1.common.events.Subscription
     *         objects
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     */
    LinkedList getSubscriptions(ConnectorCredentials credentials) 
        throws InvalidCredentialsException, NoSubscriberException, EventProcessingException;

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
     */
    String getEventTemplate(ConnectorCredentials credentials, String host, int port,
        String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException, EventProcessingException;

	/**
	 * Retrieves the event schema for the given event category, event type,
	 * and EnterpriseOne environment.
	 *
	 * @param credentials the credential information needed to validate the user
	 * @param host the EnterpriseOne server name to be used to retrieve Z event templates to generate schema
	 * @param port the EnterpriseOne server port to be used to retrieve Z event templates to generate schema
	 * @param category the event category
	 * @param type the event type
	 * @param environment the EnterpriseOne environment corresponding to the event
	 * @return the event schema
	 * @throws NotSupportedException thrown when the event template request for
	 *                               the given event category is not supported
	 * @throws InvalidCredentialsException if the user supplied invalid credentials
	 * @throws EventProcessingException if the event processing system encounters
	 *                                  other errors handling this request
	 */
	String getEventSchema(ConnectorCredentials credentials, String host, int port,
		String category, String type, String environment)
		throws NotSupportedException, InvalidCredentialsException, EventProcessingException;

    /**
     * Retrieves the list of event types, both active and inactive,
     * that are valid for a given environment.
     * 
     * @param credentials the credential information needed to validate the user
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return a LinkedList of com.peoplesoft.pt.e1.common.events.EventTypeDefinition
     *         objects
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
    LinkedList getEventList(ConnectorCredentials credentials, String environment)
        throws InvalidCredentialsException, EventProcessingException;
        
    /**
     * Retrieves the list of all event types, both active and inactive,
     * for any EnterpriseOne environment.
     * 
     * @param credentials the credential information needed to validate the user
     * @return a LinkedList of the event types as Strings
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
    LinkedList getEventList(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException;

    /**
	 * Retrieves the list of event types irrespective of subscriber,
	 * that are valid for a given environment.
	 *
	 * @param credentials the credential information needed to validate the user
	 * @param environment the EnterpriseOne environment corresponding to the event
	 * @return a LinkedList of com.peoplesoft.pt.e1.common.events.EventTypeDefinition
	 *         objects
	 * @throws InvalidCredentialsException if the user supplied invalid credentials
	 * @throws EventProcessingException if the EventTypeCache cannot retrieve
	 *                                  the list of events
	 */
	LinkedList getEventListForEnv(ConnectorCredentials credentials, String environment)
		throws InvalidCredentialsException, EventProcessingException;

    /**
     * Retrieves the list of <code>com.peoplesoft.pt.e1.common.events.connectorsvc.
     * MSMQSubscriber</code> objects that are currently in the SubscriberCache.
     * 
     * @return the list of MSMQSubscriber objects
     * @throws EventProcessingException if the SubscriberCache cannot retrieve the list
     *                                  of MSMQ subscribers
     */
    LinkedList getMSMQInfo() throws EventProcessingException;
	 /**
     * Checks if the user is an active MSMQ subscriber.
     * This method allows the security bypass for MSMQ users
     * 
     * @param credentials the credential information needed to validate the user
     * @return a boolean of the valid MSMQUser
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
	boolean isMSMQUser(ConnectorCredentials credentials) throws EventProcessingException;
}