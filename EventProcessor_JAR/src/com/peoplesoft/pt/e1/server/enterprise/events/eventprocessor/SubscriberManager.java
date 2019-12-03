//==========================================================================
//
//  Copyright © [2004]
//  PeopleSoft, Inc.
//  All rights reserved. PeopleSoft Proprietary and Confidential.
//  PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
//  of PeopleSoft, Inc.
//
//==========================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;


//=================================================
// Imports from java namespace
//=================================================
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================
//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberDeliveryTransport;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription;

import com.peoplesoft.pt.e1.server.enterprise.events.common.DeliveryTransportType;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventRouter;

import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Manages the collection of event subscribers and their queues.
 */
class SubscriberManager
{
    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Static class fields.
    //=================================================
    private static E1Logger sE1Logger = JdeLog.getE1Logger(SubscriberManager.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    private boolean mInitialized = false;

    /**
     * Which subscriber cache version re we currently initialized with.  The cache contents can
     * change at run-time so we need to keep track of this to see if we need to re-initialize the
     * subscriber information.
     */
    private int mCacheVersion = -1;

    /** Subscriber information. */
    private List mSubscribers = new LinkedList();

    /** List of Topic Subscribers */
    private List mTopicSubscribers = new LinkedList();
    private EventRouter mRouter = new EventRouter();

    //=================================================
    // Constructors.
    //=================================================
    //=================================================
    // Methods.
    //=================================================

    /**
     * Perform initialization when started up. Must be called before the first call to
     * <code>routeEvent()</code>.
     *
     * @throws EventProcessingException An error occured during initialization.
     */
    public void initialize() throws EventProcessingException
    {
        if(sE1Logger.isDebug())
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering EventRouter.initialize()", null, null, null);

        //
        //  Initialize the subscriber information.
        //
        initializeSubscribers();
	if(sE1Logger.isDebug())
	    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initialized Queue/Topic Subscribers", null, null, null);
        mInitialized = true;
	if(sE1Logger.isDebug())
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Leaving EventRouter.initialize()", null, null, null);
    }

    /**
     * Frees resources when shutting down.
     */
    public void shutdown()
    {
        mInitialized = false;

        //
        //  Itterate through all the subscriber queues and shut them down.
        //
        for (Iterator iter = mSubscribers.iterator(); iter.hasNext();)
        {
            SubscriberInfo info = (SubscriberInfo) iter.next();

            try
            {
                if (info.getQueue() != null)
                {
                    info.getQueue().shutdown();
                }
            }
            catch (EventProcessingException e)
            {
                String msg = "Failed to shutdown output jms destination: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
            }
        }

        //
        //  Itterate through all the subscriber topics and shut them down.
        //
        for (Iterator iter = mTopicSubscribers.iterator(); iter.hasNext();)
        {
            SubscriberInfo info = (SubscriberInfo) iter.next();

            try
            {
                if (info.getTopic() != null)
                {
                    info.getTopic().shutdown();
                }
            }
            catch (EventProcessingException e)
            {
                String msg = "Failed to shutdown output jms destination: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
            }
        }
    }

    /**
     * Determines which subscribers to send an event to based on  their active event subscriptions.
     *
     * @param event The event to route.
     *
     * @return Collection of subscriber queues to send the event to.  If the collection is empty
     *         then the event did not match any subscriptions.
     *
     * @throws EventProcessingException An error occured while routing the event.
     * @throws IllegalStateException DOCUMENT ME!
     */
    public Collection getInterestedSubscriberQueues(EventMessage event)
        throws EventProcessingException
    {
        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(SubscriberManager.class.getName() + " not initialized");
        }

        //
        //  Check to see if the cache version has changed.
        //
        int currentCacheVersion = SubscriberCache.getInstance().getVersionNumber();

        if (currentCacheVersion != mCacheVersion)
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Subscriber cache version has changed: "
                        + mCacheVersion
                        + " -> "
                        + currentCacheVersion, null, null, null);
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Re-initializing subscriber queues", null, null, null);
            }

            try
            {
                initializeSubscribers();
            }
            catch (EventProcessingException e)
            {
                String msg = "Failed to initialize event subscribers: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
                throw new EventProcessingException(msg, e);
            }
        }

        List result = new ArrayList(mSubscribers.size());

        //
        //  Iterate through all the subscribers checking to see if a subscriber
        //  has a matching subscription.
        //
        int check = 0;
        for (Iterator iter = mSubscribers.iterator(); iter.hasNext();)
        {
            SubscriberInfo subInfo = (SubscriberInfo) iter.next();
            if(subInfo.getSubscriber().getDeliveryTransport().getTransportType().equals(DeliveryTransportType.JMSQUEUE))
            {
            /* For all ESB Subsubscriber, the the event will delivered to internal queue ESBQueue00 first
            and no need add it for each subscriber. Just add it once.
            */
                    if(check==0)
                    {
                        result.add(subInfo.getQueue());
                        check++;
                    }
            }
            else
            {
            //TODO Check for Topic subscriber, if not do the following
                if (subInfo.getTopic() == null)
                {
                    List subscriptions = subInfo.getSubscriber().getActiveSubscriptions();

                    for (Iterator iter2 = subscriptions.iterator(); iter2.hasNext();)
                    {
                        Subscription subscription = (Subscription) iter2.next();
                        boolean matches = mRouter.matchSubscription(event.getCategory(),
                                event.getType(), event.getEnvironment(), subscription);

                        if (matches)
                        {
                            //
                            //  The subscription is a match.  Add it to the result set.  Sicne we
                            //  already have a match there is no need to continue checking so break
                            //  out of the loop.
                            //
                            result.add(subInfo.getQueue());

                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determines which subscribers to send an event to based on  their active event subscriptions.
     *
     * @param event The event to route.
     *
     * @return Collection of subscriber topics to send the event to.  If the collection is empty
     *         then the event did not match any subscriptions.
     *
     * @throws EventProcessingException An error occured while routing the event.
     * @throws IllegalStateException DOCUMENT ME!
     */
    public Collection getInterestedSubscriberTopic(EventMessage event)
        throws EventProcessingException
    {
        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(SubscriberManager.class.getName() + " not initialized");
        }

        //
        //  Check to see if the cache version has changed.
        //
        int currentCacheVersion = SubscriberCache.getInstance().getVersionNumber();

        if (currentCacheVersion != mCacheVersion)
        {
	        if (sE1Logger.isDebug())
            {
	            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
	                "Subscriber cache version has changed: "
	                    + mCacheVersion
	                    + " -> "
	                    + currentCacheVersion, null, null, null);
	            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Re-initializing subscriber queues/topics", null, null, null);
            }

            try
            {
                initializeSubscribers();
            }
            catch (EventProcessingException e)
            {
                String msg = "Failed to initialize event subscribers: " + e.getMessage();
	            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
                throw new EventProcessingException(msg, e);
            }
        }

        List result = new ArrayList(mTopicSubscribers.size());

        //
        //  Iterate through all the subscribers checking to see if a subscriber
        //  has a matching subscription.
        //
        for (Iterator iter = mTopicSubscribers.iterator(); iter.hasNext();)
        {
            SubscriberInfo subInfo = (SubscriberInfo) iter.next();
            List subscriptions = subInfo.getSubscriber().getActiveSubscriptions();

            for (Iterator iter2 = subscriptions.iterator(); iter2.hasNext();)
            {
                Subscription subscription = (Subscription) iter2.next();
                boolean matches = mRouter.matchSubscription(event.getCategory(), event.getType(),
                        event.getEnvironment(), subscription);

                if (matches && (subInfo.getTopic() != null))
                {
                    //
                    //  The subscription is a match.  Add it to the result set.  Sicne we
                    //  already have a match there is no need to continue checking so break
                    //  out of the loop.
                    //
                    result.add(subInfo.getTopic());

                    break;
                }
            }
        }

        return result;
    }

    /**
     * Initialize subscribers.
     *
     * @throws EventProcessingException DOCUMENT ME!
     */
    private void initializeSubscribers() throws EventProcessingException
    {
        //
        //  Shutdown the existing queues.
        //
        try
        {
            for (Iterator iter = mSubscribers.iterator(); iter.hasNext();)
            {
                SubscriberInfo subInfo = (SubscriberInfo) iter.next();

                //Check if the subscriber has a queue associated with it
                //Shutdown the queue
                if (subInfo.getQueue() != null)
                {
                    subInfo.getQueue().shutdown();
                }
            }

            for (Iterator iter = mTopicSubscribers.iterator(); iter.hasNext();)
            {
                SubscriberInfo subInfo = (SubscriberInfo) iter.next();

                //Check if the subscriber has a topic associated with it
                //Shutdown the topic
                if (subInfo.getTopic() != null)
                {
                    subInfo.getTopic().shutdown();
                }
            }
        }
        finally
        {
            //
            //  Reset the cache version so that if something fails below the subscribers
            //  will get reinitialized on the next pass.
            //
            mCacheVersion = -1;
            mSubscribers.clear();
            mTopicSubscribers.clear();
        }

        //
        //  Get the current cache information.
        //
        int cacheVersion = SubscriberCache.getInstance().getVersionNumber();
        Collection subscribers = SubscriberCache.getInstance().getActiveSubscribers();

        //
        //  Iterate through the subscribers and initialize an output queue for each.
        //
        for (Iterator iter = subscribers.iterator(); iter.hasNext();)
        {
            //
            //  Get the queue and connection factory JNDI names.
            //
            String queueJNDIName = null;
            String connectionFactoryJNDIName = null;
            Subscriber subscriber = (Subscriber) iter.next();
            SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
            OutputQueue queue = null;

            //Instantiate TopicConnectionFactory
            String topicConnectionFactoryJNDIName = null;

            //Instantiate Topic
            String topicJNDIName = null;

            //Instantiate OutputTopic object
            OutputTopic topic = null;

            //  See if the delivery transport is to an MQSeries queue.  If it is then the
            //  MQSeries queue is used as the subscriber queue directly.
            //
            if (transport.getTransportType().equals(DeliveryTransportType.MQSERIES_QUEUE))
            {
                //
                //  Get the queue and connection factory JNDI names from the transport
                //  properties.
                //
                queueJNDIName = transport.getProperty(F90711_EventTransportParameter.MQSERIES_QUEUE_NAME);

                String propName = F90711_EventTransportParameter.MQSERIES_CON_FACTORY_NAME;
                connectionFactoryJNDIName = transport.getProperty(propName);

                //
                //  Create an XML text message queue.
                //
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initializing MQSeries queue: " + queueJNDIName, null, null, null);
                }

                queue = new XMLTextOutputQueue(subscriber.getUsername(), queueJNDIName,
                        connectionFactoryJNDIName);

                //Initialize the queue.
                //
                try
                {
                    queue.initialize();
                }
                catch (EventProcessingException e)
                {
                    String msg = "Failed to initialize output queue: " + e.getMessage();
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    throw new EventProcessingException(msg, e);
                }

                //
                //  Save the subscriber and queue information.
                //
                SubscriberInfo subInfo = new SubscriberInfo(subscriber, queue);
                mSubscribers.add(subInfo);
            }
            else if (transport.getTransportType().equals(DeliveryTransportType.JMSTOPIC))
            {
                //  See if the delivery transport is to JMSTOPIC.  If it is then the
                //  JMS Topic  is used as the subscriber topic directly.
                //
                topicJNDIName = transport.getProperty(F90711_EventTransportParameter.JMS_TOPIC_NAME);

                String propName = F90711_EventTransportParameter.JMSTOPIC_CON_FACTORY_NAME;
                topicConnectionFactoryJNDIName = transport.getProperty(propName);

                //
                //  Create an XML text message topic.
                //
				if (sE1Logger.isDebug())
                {
					sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initializing JMS Topic: " + topicJNDIName, null, null, null);
                }

                topic = new XMLTextOutputTopic(subscriber.getUsername(), topicJNDIName,
                        topicConnectionFactoryJNDIName);

                try
                {
                    topic.initialize();
                }
                catch (EventProcessingException e)
                {
                    String msg = "Failed to initialize output Topic: " + e.getMessage();
					sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null, e);
                    throw new EventProcessingException(msg, e);
                }

                //
                //  Save the subscriber and topic information.
                //
                SubscriberInfo subInfo = new SubscriberInfo(subscriber, topic);

                //Add to the Topic subscribers list
                mTopicSubscribers.add(subInfo);
            }
            else if (transport.getTransportType().equals(DeliveryTransportType.JMSQUEUE))
            {

                //
                //  Use the internal subscriber queue.
                //
                queueJNDIName = JMSNames.ESB_QUEUE ;

                //
                //  Create an event object queue.
                //
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initializing internal queue for ESB: " + queueJNDIName, null, null, null);
                }
                    queue = new EventObjectOutputQueue(subscriber.getUsername(), queueJNDIName,connectionFactoryJNDIName);
                //
                //  Initialize the queue.
                //
                try
                {
                    queue.initialize();
                }
                catch (EventProcessingException e)
                {
                    String msg = "Failed to initialize output queue: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    throw new EventProcessingException(msg, e);
                }

                //
                //  Save the subscriber and queue information.
                //
                SubscriberInfo subInfo = new SubscriberInfo(subscriber, queue);
                mSubscribers.add(subInfo);
            }
            else
            {
                //
                //  Use the internal subscriber queue.
                //
                queueJNDIName = subscriber.getQueueDescription().getQueueJNDIName();

                //
                //  Create an event object queue.
                //
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Initializing internal queue: " + queueJNDIName, null, null, null);
                }

                queue = new EventObjectOutputQueue(subscriber.getUsername(), queueJNDIName,
                        connectionFactoryJNDIName);

                //
                //  Initialize the queue.
                //
                try
                {
                    queue.initialize();
                }
                catch (EventProcessingException e)
                {
                    String msg = "Failed to initialize output queue: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    throw new EventProcessingException(msg, e);
                }

                //
                //  Save the subscriber and queue information.
                //
                SubscriberInfo subInfo = new SubscriberInfo(subscriber, queue);
                mSubscribers.add(subInfo);
            }
        }

        //
        //  Remember the cache version we initialized with.  This is not done until
        //  initialization has completed in case an expecption is thrown and the
        //  initialization does not complete.
        //
        mCacheVersion = cacheVersion;
    }

    /**
     * Private internal class to keep track of information about a subscriber.
     */
    private class SubscriberInfo
    {
        private Subscriber mSubscriber;
        private OutputQueue mQueue;
        private OutputTopic mTopic;

        /**
         * Creates a new SubscriberInfo object.
         *
         * @param sub DOCUMENT ME!
         * @param queue DOCUMENT ME!
         */
        SubscriberInfo(Subscriber sub, OutputQueue queue)
        {
            mSubscriber = sub;
            mQueue = queue;
        }

        /**
         * Creates a new SubscriberInfo object.
         *
         * @param sub DOCUMENT ME!
         * @param topic DOCUMENT ME!
         */
        SubscriberInfo(Subscriber sub, OutputTopic topic)
        {
            mSubscriber = sub;
            mTopic = topic;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        OutputQueue getQueue()
        {
            return mQueue;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        OutputTopic getTopic()
        {
            return mTopic;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        Subscriber getSubscriber()
        {
            return mSubscriber;
        }
    }
}
