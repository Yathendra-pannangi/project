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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//=================================================
//Imports from javax namespace
//=================================================
 import javax.management.ObjectName;
//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
import com.jdedwards.mgmt.agent.Server;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90706_EventSubscriber;
import com.peoplesoft.pt.e1.server.common.events.monitoring.SubscriberMonitoringInfo;


//=================================================
//Imports from org namespace
//=================================================

/**
 *  Caches event type information from the database for faster run-time access.
 */
public final class SubscriberCache
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static SubscriberCache sInstance = new SubscriberCache();
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(SubscriberCache.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    
    private List mActiveSubscriberCache = new LinkedList();
    
    private List mAllSubscriberCache = new LinkedList();
    
    private boolean mCacheLoaded = false;
    
    /** 
     * Version number indicates to cache users if the cache has been reloaded.
     * Each time the cache is reloaded the version number is incremented so that
     * users of the cache can detect that it has changed.
     */
    private int mVersionNumber = 0;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor since this class is a singleton.
     */
    private SubscriberCache()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get a reference to the cache instance.
     * 
     *  @return  Reference to the cache instance.
     */
    public static SubscriberCache getInstance()
    {
        return sInstance;
    }
    
    /**
     *  Load the cache if it's not already loaded.  Used to force loading during some
     *  type of initialization rather then at first use.
     * 
     *  @throws EventProcessingException  Error occured.
     */
    public synchronized void preload() throws EventProcessingException
    {
        if (!mCacheLoaded)
        {
            reload();
        }
    }
    
    /**
     *  Reload the cache from the database.
     * 
     *  @throws EventProcessingException  An error occured while reloading the cache.
     */
    void reload() throws EventProcessingException
    {
        //
        //  Build new cache content.
        //
        SubscriberCacheLists newCache = loadSubscribers();
        
        //
        //  Replace the existing cache content with the new stuff.
        //
        replaceCache(newCache);
    }
    
    /**
     *  Get a collection of active subscriber definitions.
     * 
     *  @return  Collection of <code>Subscriber</code> objects.  All of the subscribers
     *           in the collection will be in the active state.  Inactive subscribers
     *           are not included.
     * 
     *  @throws EventProcessingException  An error occured getting subscribers
     *                                    from the cache.
     */
    public synchronized Collection getActiveSubscribers() throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
                   
        return Collections.unmodifiableList(mActiveSubscriberCache);
    }
    
    /**
     *  Get a collection of subscriber definitions.
     * 
     *  @return  Collection of <code>Subscriber</code> objects.
     * 
     *  @throws EventProcessingException  An error occured getting subscribers
     *                                    from the cache.
     */
    public synchronized Collection getAllSubscribers() throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
                   
        return Collections.unmodifiableList(mAllSubscriberCache);
    }
    /**
     *  Get a collection of subscriber definitions.
     * 
     *  @param subscriberID String
     *  @return <code>Subscriber</code> object associated with the subscriber ID.
     * 
     *  @throws EventProcessingException  An error occured getting subscribers
     *                                    from the cache.
     */
    public synchronized Subscriber getSubscriber(String subscriberID)
        throws EventProcessingException
    {
        if (subscriberID == null)
        {
            throw new NullPointerException("subscriberID is null");
        }
        
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
        
        //
        //  Iterate through the subscribers looking for the requested subscriber ID.
        //
        Subscriber result = null;
        for(Iterator iter = mAllSubscriberCache.iterator(); iter.hasNext();)
        {
            Subscriber subscriber = (Subscriber)iter.next();       
            if(subscriberID.equals(subscriber.getUsername()))
            {
                result = subscriber;
                break;
            }
        }       
        return result;
    }   
    /**
     *  Get the version number of the cache.  If the cache version number has changed
     *  since the last call to this method then the cache contects have changed.
     * 
     *  @return  Cache contents version number.
     * 
     *  @throws EventProcessingException  An error occured getting the cache version.
     */
    public synchronized int getVersionNumber() throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
        
        return mVersionNumber;
    }
    
    /**
     *  Replaces the current cache with a new one.
     */
    private synchronized void replaceCache(SubscriberCacheLists newCache)
    {
        mActiveSubscriberCache = newCache.getActiveSubscribers();
        mAllSubscriberCache = newCache.getAllSubscribers();
        ++mVersionNumber;
        mCacheLoaded = true;
    }
    
    /**
     *  Loads subscribers from the database.
     * 
     *  @return  A list of <code>Subscriber</code> objects.
     */
    private SubscriberCacheLists loadSubscribers() throws EventProcessingException
    {
        SubscriberCacheLists subscribers = new SubscriberCacheLists();
        JDBDatabaseAccess connection = null;
        try
        {
            connection = EventDBUtil.getDefaultDBAccess();
            
            //
            //  Select all of the subscribers from the database.
            //
            JDBResultSet resultSet = 
                connection.select(F90706_EventSubscriber.TABLE, F90706_EventSubscriber.FIELDS);
            try
            {
                /**
                *  Iterate through the Management Console subscriber MBeans and unregister so they can be refreshed when added to the subscriber cache.
                */
                    if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {
                                    
                    try{
                        Set set = Server.getServer().getMBeanServer().queryNames(new ObjectName("jde:targetType=rteserver" + ",instanceName=" + E1Agent.getAgent().getInstanceName() + ",metricName=rte_monitoring_subscriber_info" + ",*"), null);
                        for (Iterator i = set.iterator(); i.hasNext(); ) {
                                            
                            ObjectName on = (ObjectName)i.next();
                            Server.getServer().unregisterComponent(on);
                            if(sE1Logger.isDebug())
                            {
                                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_subscriber_info MBean deregistered: " + on.toString(), null, null, null);
                            }
                                            
                            }
                        } catch(Exception e){
                                    
                            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Failed to query the MbeanServer for rte_monitoring_subscriber_info mbeans: " + e.getMessage(), null, null, e);
                                        
                        }               
                }
                
                while (resultSet.hasMoreRows())
                {
                    JDBFieldMap fieldMap = resultSet.fetchNext();
                    
					/**
					 * SAR 7871301
					 * In case of "EventProcessingException" thrown when creating a subscriber,
					 * e.g. "UnknownDeliveryTransportException" due to invaild transport type,
					 * do not exception out of this function, instead, handle the exception
					 * and contiune to load next subscriber. 
					 */                    
					Subscriber subscriber = null;
                    try {
						subscriber = new Subscriber(fieldMap, connection);
                    } catch (EventProcessingException epe)
                    {
						String temp = fieldMap.getString(F90706_EventSubscriber.USER);
						String userName = null;						
						if (temp != null)
						{
							userName = temp.trim();
						}                    	
						String msg = "Failed to load subscriber " + userName + " into event cache: " + epe.getMessage();
						sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null,null, epe);                    	
                    }
                    
                    if (subscriber != null)
                    {
						//
						//  Only cache the active subscribers in the active subscriber cache.
						//
						if (subscriber.isActive())
						{
							subscribers.getActiveSubscribers().add(subscriber);
						}
						subscribers.getAllSubscribers().add(subscriber); 
                                                /**
                                                 *Create the SubscriberMonitoringInfo object and register it with the Manangement Console so each subscriber in cache
                                                 *has an mbean.
                                                 */
                                                SubscriberMonitoringInfo subInfo = new SubscriberMonitoringInfo();
                                                
                                                subInfo.setDescription(subscriber.getDescription());
                                                subInfo.setUsername(subscriber.getUsername());
                                                subInfo.setIsActive(subscriber.isActive());
                                                subInfo.registerObject(subscriber.getUsername());
                    }
                }
            }
            catch (JDBException e)
            {
                String msg = "Failed to load subscribers into event cache: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
                throw new EventProcessingException(msg, e);
            }
            finally
            {
                try
                {
                    resultSet.close();
                }
                catch (Exception e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database result set: " + e.getMessage(),null,null,e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to load subscribers into event cache: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
            throw new EventProcessingException(msg, e);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database connection: " + e.getMessage(),null,null, e);
                }
            }
        }
        
        return subscribers;
    }
    
    private class SubscriberCacheLists
    {
        private List mActiveSubscribers = new LinkedList();
        private List mAllSubscribers = new LinkedList();
        
        
        /**
         * @return List of active subscribers.
         */
        public List getActiveSubscribers()
        {
            return mActiveSubscribers;
        }

        /**
         * @return List of all subscribers.
         */
        public List getAllSubscribers()
        {
            return mAllSubscribers;
        }

    }
}
