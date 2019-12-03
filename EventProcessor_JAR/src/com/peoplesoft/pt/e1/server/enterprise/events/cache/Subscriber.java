//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.cache;

//=================================================
//Imports from java namespace
//=================================================
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90706_EventSubscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90707_EventSubscription;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90715_EventQueueAssignment;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Contains a description of an event subscriber.
 */
public class Subscriber
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(Subscriber.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    
    private String mUsername;
    
    private String mDescription;
    
    private boolean mIsActive;
    
    private List mActiveSubscriptions = new LinkedList();
    
    private List mAllSubscriptions = new LinkedList();
    
    private SubscriberQueueDescription mQueueDescription;
    
    private SubscriberDeliveryTransport mDeliveryTransport;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Construct a subscriber from a database field map.
     * 
     *  @param fieldMap  Database field map to build the subscriber from.
     * 
     *  @param connection  Connection to use for loading any other data.
     * 
     *  @throws JDBException  Error during construction.
     * 
     *  @throws EventProcessingException  Error during construction.
     */
    Subscriber(JDBFieldMap fieldMap, JDBDatabaseAccess connection)
        throws JDBException, EventProcessingException
    {
        String temp = null;
        
        temp = fieldMap.getString(F90706_EventSubscriber.USER);
        if (temp != null)
        {
            mUsername = temp.trim();
        }
        else
        {
            String msg = "Event subscriber found in database with null username";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        temp = fieldMap.getString(F90706_EventSubscriber.EVNTSBDC);
        if (temp != null)
        {
            mDescription = temp.trim();
        }
        
        temp = fieldMap.getString(F90707_EventSubscription.EVNTACT);
        if (temp != null)
        {
            String activeString = temp.trim();
            mIsActive = activeString.equalsIgnoreCase(F90707_EventSubscription.ACTIVE_STRING);
        }
        else
        {
            String msg = "Event subscriber found in database with null active status";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        //
        //  Load the delivery transport.
        //
        temp = fieldMap.getString(F90706_EventSubscriber.EVNTTDRIVE);
		String transportType=null;
        if (temp != null)
        {
            transportType = temp.trim();
            mDeliveryTransport = 
                new SubscriberDeliveryTransport(transportType, mUsername, connection);
        }
        else
        {
            String msg = "Event subscriber found in database with null delivery transport";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        //
        //  Load the queue description.
        //
        
        
        if(temp!= null){

        	if(!transportType.equals(F90706_EventSubscriber.TRANSPORT_JMS_TOPIC))
         	{
				mQueueDescription = loadQueueDescription(connection, mUsername,transportType);
        	}
				
        }
        
        
        //
        //  Load the subscriptions.
        //
        SubscriptionLists subscriptions = loadSubscriptions(connection, mUsername);
        mActiveSubscriptions = subscriptions.getActiveSubscriptions();
        mAllSubscriptions = subscriptions.getAllSubscriptions();
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  Description of the subscriber.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @return  <code>true</code> indicates subscriber is active, <code>false</code>
     *          indicates subscriber is inactive.
     */
    public boolean isActive()
    {
        return mIsActive;
    }

    /**
     * @return  Subscriber username used for sign-on / authentication.
     */
    public String getUsername()
    {
        return mUsername;
    }

    /**
     * @return  Delivery transport description.
     */
    public SubscriberDeliveryTransport getDeliveryTransport()
    {
        return mDeliveryTransport;
    }

    /**
     * @return  Queue description
     */
    public SubscriberQueueDescription getQueueDescription()
    {
        return mQueueDescription;
    }
    
    /**
     * Active subscriptions.
     * 
     * @return  A list of active <code>Subscription</code> objects.
     */
    public List getActiveSubscriptions()
    {
        return Collections.unmodifiableList(mActiveSubscriptions);
    }
    
    /**
     * All subscriptions.
     * 
     * @return  A list of all <code>Subscription</code> objects.
     */
    public List getAllSubscriptions()
    {
        return Collections.unmodifiableList(mAllSubscriptions);
    }
    
    /**
     *  Load all the active subscriptions for the subscriber.
     */
    private SubscriptionLists loadSubscriptions(JDBDatabaseAccess connection, String username)
        throws JDBException, EventProcessingException
    {
        SubscriptionLists result = new SubscriptionLists();
        
        //
        //  Get the subscriptions.
        //
        JDBResultSet resultSet =
            connection.select(
                F90707_EventSubscription.TABLE,
                F90707_EventSubscription.FIELDS,
                getSubscriptionSelection(username),
                null);
        
        //
        //  Extract all the parameters from the result set.
        //
        try
        {
            while (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                Subscription subscription = new Subscription(map, connection);
                
                if (subscription.isActive())
                {
                    result.getActiveSubscriptions().add(subscription);
                }
                result.getAllSubscriptions().add(subscription);
            }
        }
        finally
        {
            try
            {
                //
                //  Make sure to try closing the result set.
                //
                resultSet.close();
            }
            catch (Exception e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database result set: " + e.getMessage(), null, null, e);
            }
        }
        
        return result;
    }
    
    /**
     *  Load the subscriber's queue description
     */
    private SubscriberQueueDescription loadQueueDescription(JDBDatabaseAccess connection, 
                                                            String username,
                                                            String transportType)
        throws JDBException, EventProcessingException
    {
        SubscriberQueueDescription result = null;
        
        //
        //  Get the queue description.
        //
        
		// Return  queue name based on transport type
		if (transportType.equals(F90706_EventSubscriber.TRANSPORT_MQSERIES_QUEUE))
		{
		
			String queueName = this.mDeliveryTransport.getProperty(F90711_EventTransportParameter.MQSERIES_QUEUE_NAME);
			result = new SubscriberQueueDescription(queueName);		
		}
		else if (transportType.equals(F90706_EventSubscriber.TRANSPORT_JDENET))
		{
			String queueName = JMSNames.SOURCE_ROUTE_Q;
			result = new SubscriberQueueDescription(queueName);
		}
                else if (transportType.equals(F90706_EventSubscriber.TRANSPORT_JMS_QUEUE))
                {
                        String queueName = JMSNames.ESB_QUEUE;
                        result = new SubscriberQueueDescription(queueName);
                }

		else
		{
		
		

        JDBResultSet resultSet =
            connection.select(
                F90715_EventQueueAssignment.TABLE,
                F90715_EventQueueAssignment.FIELDS,
                getQueueSelection(username),
                null);
        
        try
        {
            if (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                SubscriberQueueDescription queueDescription = new SubscriberQueueDescription(map);
                result = queueDescription;
            }
            else
            {
                String msg =
                    "Failed to locate event subscription queue for subscriber: " + username;
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                throw new EventProcessingException(msg);
            }
        }
        finally
        {
            //
            //  Make sure to try closing the result set.
            //
            try
            {
                resultSet.close();
            }
            catch (Exception e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database result set: " + e.getMessage(), null, null, e);
            }
        }
		} 
        
        return result;
    }
    
    private JDBSelection getSubscriptionSelection(String username)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison userCompare =
            new JDBFieldComparison(
                F90707_EventSubscription.USER,
                JDBComparisonOp.EQ,
                username,
                true);
                
        return userCompare;
    }
    
    private JDBSelection getQueueSelection(String username)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison userCompare =
            new JDBFieldComparison(
                F90715_EventQueueAssignment.USER,
                JDBComparisonOp.EQ,
                username,
                true);

        return userCompare;
    }
    
    private class SubscriptionLists
    {
        private List mActiveSubscriptions = new LinkedList();
        private List mAllSubscriptions = new LinkedList();
        
        /**
         * @return  Active subscriptions.
         */
        public List getActiveSubscriptions()
        {
            return mActiveSubscriptions;
        }

        /**
         * @return  All subscriptions.
         */
        public List getAllSubscriptions()
        {
            return mAllSubscriptions;
        }

    }
}
