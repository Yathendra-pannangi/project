//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.enterprise.events.connectorsvc;


//=================================================
// Imports from javax namespace
//=================================================
//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;

import com.jdedwards.system.security.SecurityServerException;
import com.jdedwards.system.security.SecurityServerInstance;
import com.jdedwards.system.security.SecurityServerResponse;

import com.jdedwards.system.security.SecurityToken;

import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.EventTypeDefinition;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;
import com.peoplesoft.pt.e1.common.events.connectorsvc.MSMQSubscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscribedToEventType;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberDeliveryTransport;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription;
import com.peoplesoft.pt.e1.server.enterprise.events.common.DeliveryTransportType;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventRouter;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.*;


//=================================================
// Imports from org namespace
//=================================================

/**
 * Main implementation class for the IConnectorService interface.
 */
public class ConnectorServiceLogic implements IConnectorService {
    //=================================================
    // Non-public static class fields.
    //=================================================
    private static EventTypeCache sEventTypeCache = EventTypeCache.getInstance();
    private static SubscriberCache sSubscriberCache = SubscriberCache.getInstance();
    private static E1Logger sE1Logger = JdeLog.getE1Logger(ConnectorServiceLogic.class.getName());
    private static EventTypeTemplateBuilder sTemplateBuilder = null;
    private static SchemaGenerationLogic sgLogic = null;

    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Instance member fields.
    //=================================================
    private EventRouter mEventRouter = new EventRouter();
    private Vector eventVector = new Vector();

    //=================================================
    // Constructors.
    //=================================================

    /**
     * Default constructor.
     */
    public ConnectorServiceLogic() {
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * {@inheritDoc}
     */
    public String getQueueJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, 
            EventProcessingException {
        validateUser(credentials);

        // return the  queue JNDI name 
        Subscriber subscriber = findSubscriber(credentials.getUsername());
        SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();

	    return subscriber.getQueueDescription().getQueueJNDIName();

    }

    /**
     * {@inheritDoc}
     */
    public String getQueueConFactJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException {
        validateUser(credentials);

        // return the proper queue connection factory JNDI name based on whether subscriber uses
        // MQSeries or some other transport
        Subscriber subscriber = findSubscriber(credentials.getUsername());
        SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();

        if (transport.getTransportType().equals(DeliveryTransportType.MQSERIES_QUEUE)) {
            return transport.getProperty(F90711_EventTransportParameter.MQSERIES_CON_FACTORY_NAME);
        } else {
            return JMSNames.Q_CON_FACTORY;
        }
    }

    /**
     *  {@inheritDoc}
     */
    public LinkedList getSubscriptions(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, 
            EventProcessingException {
        validateUser(credentials);

        // get subscription for user
        Subscriber subscriber = findSubscriber(credentials.getUsername());
        List serverSubList = subscriber.getAllSubscriptions();
        LinkedList clientSubList = new LinkedList();

        // convert Subscription objects to bean equivalents
        Iterator iter = serverSubList.iterator();

        while (iter.hasNext()) {
            Subscription serverSub = (Subscription) iter.next();
            clientSubList.add(convertSubscriptionToBean(serverSub));
        }

        return clientSubList;
    }

    /**
     *  {@inheritDoc}
     */
    public String getEventTemplate(ConnectorCredentials credentials,
        String host, int port, String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException, 
            EventProcessingException {
        validateUser(credentials);

        return sTemplateBuilder.getTemplate(credentials, host, port, category,
            type, environment);
    }

    /**
	 *  {@inheritDoc}
	 */
	public String getEventSchema(ConnectorCredentials credentials,
		String host, int port, String category, String type, String environment)
		throws NotSupportedException, InvalidCredentialsException,
			EventProcessingException {
			String template = "";
			template = getEventTemplate(credentials,host,port,category,type,environment);
                      
                      sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"SGU:Template received for ( Category:"+category+ ",Type"+type+",Env"+environment+")",null,null,null);  
                      String newTemplateStr = template.replaceAll(">[ \\n\\t\\r]*<","><");
                      newTemplateStr = newTemplateStr.substring(newTemplateStr.indexOf("<"),newTemplateStr.length());
                      
                      if(sgLogic == null)
			 sgLogic = SchemaGenerationLogic.getInstance();

                      String schemaStr = sgLogic.generateSchema(newTemplateStr);
                      sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"SGU:Schema Generated for ( Category:"+category+ ",Type"+type+",Env"+environment+")",null,null,null);  
                      schemaStr.replaceAll("[><]*",">\\n<");
                      sgLogic = null;
                      return schemaStr;
	  }

    /**
     * {@inheritDoc}
     */
    public LinkedList getEventList(ConnectorCredentials credentials,
        String environment)
        throws InvalidCredentialsException, EventProcessingException {
        validateUser(credentials);

        // get list of EventTypeDefinition objects associated with environment
        List eventTypeDefList = sEventTypeCache.getEventsForEnvironment(environment);

        // sort through each EventTypeDefinition and create the Connector's equivalent
        // EventTypeDefinition object
        LinkedList conEventTypeList = new LinkedList();
        Iterator iter = eventTypeDefList.iterator();

        while (iter.hasNext()) {
            com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition eventTypeDef =
                (com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition) iter.next();

            String category = eventTypeDef.getCategory();
            String type = eventTypeDef.getType();
            String env = eventTypeDef.getEnvironment();
            boolean isActive = eventTypeDef.isActive();
            boolean isSub = false;

            // determine whether the EventTypeDefinition has even a single subscription
            Collection allSubs = sSubscriberCache.getAllSubscribers();
            Iterator subIter = allSubs.iterator();

            while (subIter.hasNext()) {
                Subscriber subscriber = (Subscriber) subIter.next();
                Collection subscriptions = subscriber.getAllSubscriptions();
                Iterator subscripIter = subscriptions.iterator();

                while (subscripIter.hasNext()) {
                    Subscription subscription = (Subscription) subscripIter.next();

                    if (mEventRouter.matchSubscription(category, type,
                                environment, subscription)) {
                        isSub = true;

                        break;
                    }
                }

                // stop searching through list once a single subscription is found
                if (isSub) {
                    EventTypeDefinition conEventTypeDef = new EventTypeDefinition(category,
                            type, env, isActive, isSub);
                    conEventTypeList.add(conEventTypeDef);

                    break;
                }
            }
        }

        return conEventTypeList;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedList getEventList(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException {
        validateUser(credentials);

        return new LinkedList(sEventTypeCache.getAllEventTypes());
    }

    /**
     * {@inheritDoc}
     */
    public LinkedList getEventListForEnv(ConnectorCredentials credentials,
        String environment)
        throws InvalidCredentialsException, EventProcessingException {

         // get list of EventTypeDefinition objects associated with environment
         List eventTypeDefList = sEventTypeCache.getEventsForEnvironment(environment);
         sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"getting event list for environment "+environment,null,null,null);
         // sort through each EventTypeDefinition and create the Connector's equivalent
         // EventTypeDefinition object
         LinkedList conEventTypeList = new LinkedList();
         Iterator iter = eventTypeDefList.iterator();

         while (iter.hasNext()) {
             com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition eventTypeDef =
                 (com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition) iter.next();

             String category = eventTypeDef.getCategory();
             String type = eventTypeDef.getType();
             String env = eventTypeDef.getEnvironment();
             boolean isActive = eventTypeDef.isActive();
             boolean isSub = false;

             // determine whether the EventTypeDefinition has even a single subscription
             Collection allSubs = sSubscriberCache.getAllSubscribers();
             Iterator subIter = allSubs.iterator();

             while (subIter.hasNext()) {
                 Subscriber subscriber = (Subscriber) subIter.next();
                 Collection subscriptions = subscriber.getAllSubscriptions();
                 Iterator subscripIter = subscriptions.iterator();

                 while (subscripIter.hasNext()) {
                     Subscription subscription = (Subscription) subscripIter.next();

                     if (mEventRouter.matchSubscription(category, type,
                                 environment, subscription)) {
                         isSub = true;

                         break;
                     }
                 }
             }
          
            if(!dupEntry(type)){
                 EventTypeDefinition conEventTypeDef = new EventTypeDefinition(category,
                                 type, env, isActive, isSub);
                 conEventTypeList.add(conEventTypeDef);
            }
          
         }
         
         sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Got event list for environment "+environment+" , no. of events in the list are "+conEventTypeList.size(),null,null,null);
         return conEventTypeList;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedList getMSMQInfo() throws EventProcessingException {
        LinkedList subList = new LinkedList();

        // get list of active subscribers
        Collection subscribers = sSubscriberCache.getActiveSubscribers();

        for (Iterator iter = subscribers.iterator(); iter.hasNext();) {
            // get subscriber name, queue name, and label
            Subscriber subscriber = (Subscriber) iter.next();

            if (subscriber.getDeliveryTransport().getTransportType().equals(DeliveryTransportType.MSMQ)) {
                String queueName = subscriber.getDeliveryTransport()
                                             .getProperty(F90711_EventTransportParameter.MSMQ_QUEUE_NAME);
                String queueLabel = subscriber.getDeliveryTransport()
                                              .getProperty(F90711_EventTransportParameter.MSMQ_QUEUE_LABEL);

                // create subscriber object and add to list
                MSMQSubscriber msSub = new MSMQSubscriber(subscriber.getUsername(),
                        queueName, queueLabel);
                subList.add(msSub);
            }
        }

        return subList;
    }

    /**
     * Initializes the appropriate EventTypeTemplateBuilder instance.
     */
    public void initialize() {
        // initialize the proper type of event template builder
        sTemplateBuilder = new EnterpriseOneLegacyTemplateBuilder();
        sTemplateBuilder.initialize();
    }

    /**
     * Shuts down the EventTypeTemplateBuilder instance.
     */
    public void shutdown() {
        sTemplateBuilder.shutdown();
    }

    /**
     * Validates that the user supplied valid EnterpriseOne credentials.
     *
     * @param credentials the credential information needed to validate the user
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws EventProcessingException if there is a problem in communication with the
     *                                  SecurityServer
     */
    private void validateUser(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException {
        // allow subscribers for MSMQ delivery transport types to bypass authentication

            if ((null == credentials.getSecurityToken()) ||
                    "".equals(credentials.getSecurityToken())) {
                Collection subscribers = sSubscriberCache.getActiveSubscribers();

                for (Iterator iter = subscribers.iterator(); iter.hasNext();) {
                    Subscriber subscriber = (Subscriber) iter.next();

                    if (subscriber.getUsername().equalsIgnoreCase(credentials.getUsername()) &&
                            subscriber.getDeliveryTransport().getTransportType()
                                          .equals(DeliveryTransportType.MSMQ)) {
                        return;
                    }
                }            // no MSMQ match for user without password, which is invalid
            throw new InvalidCredentialsException("Username " +
                credentials.getUsername() +
                " must supply a token to the events processing system.");
                
        }

        // validate user against Security Server
        SecurityServerResponse ssr = null;

        try {
ssr = SecurityServerInstance.getInstance().validateToken(new SecurityToken(credentials.getSecurityToken()));
        } catch (SecurityServerException e) {
            String errorMsg = "Security Server error while validating user.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg,null, null, e);
            throw new EventProcessingException(errorMsg, e);
        }

        // check for response from Security Server
        if (ssr == null) {
            String errorMsg = "No response from Security Server while validating user.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null,null);
            throw new EventProcessingException(errorMsg);
        }

        // check to see if Security Server responded that credentials were valid
        if (!ssr.isLoginValid()) {
            String errorMsg = "Invalid credentials supplied in event processing request.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null,null);
            throw new InvalidCredentialsException(errorMsg);
        }
    }

    /**
     * Finds the Subscriber instance that is associated with the given username.
     *
     * @param username the EnterpriseOne username
     * @return the Subscriber instance associated with the username
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     */
    private Subscriber findSubscriber(String username)
        throws EventProcessingException, NoSubscriberException {
        // Find username among subscribers and return corresponding Subscriber object
        Iterator iterator = sSubscriberCache.getAllSubscribers().iterator();

        while (iterator.hasNext()) {
            Subscriber subscriber = (Subscriber) iterator.next();

            if (username.equals(subscriber.getUsername())) { // found a subscriber with the same username

                return subscriber;
            }
        }

        // no subscribers match the given username
        String msg = "No event subscribers were found for username: " +
            username;
        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
        throw new NoSubscriberException(msg);
    }

    /**
     * Converts a com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription object
     * to a com.peoplesoft.pt.e1.common.events.connectorsvc.Subscription object.
     *
     * @param serverSub the com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription
     *                  object to be converted
     * @return the converted com.peoplesoft.pt.e1.common.events.connectorsvc.Subscription
     *         bean
     */
    private com.peoplesoft.pt.e1.common.events.Subscription convertSubscriptionToBean(
        Subscription serverSub) {
        // extract out the SubscribedToEventType set
        HashSet beanEventTypeSet = new HashSet();
        Collection eventTypes = serverSub.getSubscribedToEventTypes();
        Iterator iter = eventTypes.iterator();

        while (iter.hasNext()) {
            SubscribedToEventType serverEventType = (SubscribedToEventType) iter.next();
            com.peoplesoft.pt.e1.common.events.SubscribedToEventType eventTypeBean =
                new com.peoplesoft.pt.e1.common.events.SubscribedToEventType(serverEventType.getCategory(),
                    serverEventType.getType());
            beanEventTypeSet.add(eventTypeBean);
        }

        // extract out the environment set
        HashSet beanEnvSet = new HashSet();
        Collection envs = serverSub.getSubscribedToEnvironments();
        iter = envs.iterator();

        while (iter.hasNext()) {
            beanEnvSet.add((String) iter.next());
        }

        return new com.peoplesoft.pt.e1.common.events.Subscription(serverSub.getName(),
            serverSub.getDescription(), beanEventTypeSet, beanEnvSet);
    }

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
    public boolean isMSMQUser(ConnectorCredentials credentials)
        throws EventProcessingException {
        boolean isMSMQUser = false;
        Collection subscribers = sSubscriberCache.getActiveSubscribers();

        for (Iterator iter = subscribers.iterator(); iter.hasNext();) {
            Subscriber subscriber = (Subscriber) iter.next();

            if (subscriber.getUsername().equalsIgnoreCase(credentials.getUsername()) &&
                    subscriber.getDeliveryTransport().getTransportType().equals(DeliveryTransportType.MSMQ)) {
                isMSMQUser = true;

                break;
            }
        }

        return isMSMQUser;
    }
    // Checks whether its new eventType or not
    private boolean dupEntry(String eventType){
	        Iterator eveIter = eventVector.iterator();
	        boolean dupEntry = false;
	        while(eveIter.hasNext()) {
	                if(eveIter.next().equals(eventType)){
	                    dupEntry = true;
	                }
	            }
	        if(!dupEntry){
	            eventVector.add(new String("eventType"));
	            dupEntry = false;
	        }
	        return dupEntry;
    }
}
