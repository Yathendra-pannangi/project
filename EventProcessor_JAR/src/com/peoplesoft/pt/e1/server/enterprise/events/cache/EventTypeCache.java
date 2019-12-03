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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.services.base.JDBServiceCache;
import com.jdedwards.database.services.spec.SerializedSpecMap;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90705_EventActivationStatus;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Caches event type information from the database for faster run-time access.
 */
public final class EventTypeCache
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(EventTypeCache.class.getName());
    
    private static EventTypeCache sInstance = new EventTypeCache();

    //=================================================
    // Instance member fields.
    //=================================================
    
    private HashMap mCache = new HashMap();
    
    private boolean mCacheLoaded = false;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor since this class is a singleton.
     */
    private EventTypeCache()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get a reference to the cache instance.
     * 
     *  @return  Reference to the cache instance.
     */
    public static EventTypeCache getInstance()
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
     *  @throws EventProcessingException  Error reloading the cache.
     */
    void reload() throws EventProcessingException
    {
        //
        //  Build the new class outside of a synchronization block so that the old cache
        //  can still be accessed.  Then replace the cache quickly within a short
        //  synchronized block.
        //
        HashMap cache = buildNewCache();
        replaceCache(cache);
    }
    
    private synchronized void replaceCache(HashMap cache)
    {
        mCache = cache;
        mCacheLoaded = true;
    }
    
    /**
     *  Get an event type definition.
     * 
     *  @param category      The event category.
     * 
     *  @param type          The event type.
     * 
     *  @param environment   The environment.
     * 
     *  @return  The event definition or <code>null</code> if no definition is found.
     * 
     *  @throws EventProcessingException  An error occured locating the requested cache entry.
     */
    public synchronized EventTypeDefinition getEventTypeDefinition(String category, 
                                                                   String type, 
                                                                   String environment)
        throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
        
        EventTypeCacheKey key = new EventTypeCacheKey(category, type, environment);
        EventTypeDefinition eventDef = (EventTypeDefinition)mCache.get(key);

        return eventDef;
    }
    
    /**
     *  Get event definitions for a specific environment.
     * 
     *  @param environment  The requested environment.
     * 
     *  @return  A list of <code>EventTypeDefinition</code> objects for the
     *           requested environment.
     * 
     *  @throws  EventProcessingException  An error occured.
     */
    public synchronized List getEventsForEnvironment(String environment) 
        throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
        
        List result = new LinkedList();
        for (Iterator iter = mCache.values().iterator(); iter.hasNext();)
        {
            EventTypeDefinition eventDef = (EventTypeDefinition)iter.next();
            if (eventDef.getEnvironment().equals(environment))
            {
                result.add(eventDef);
            }
        }
        
        return result;
    }
    
    /**
     * Get a list of all events types, regardless of environment.
     * 
     * @return  A <code>List</code> of <code>String</code> objects containing the event
     *          type of all events defined within the system.
     * 
     * @throws EventProcessingException  Error occured.
     */
    public synchronized List getAllEventTypes() throws EventProcessingException
    {
        //
        //  Verify the cache is loaded.
        //
        if (!mCacheLoaded)
        {
            reload();
        }
        
        HashSet values = new HashSet();
        for (Iterator iter = mCache.values().iterator(); iter.hasNext();)
        {
            EventTypeDefinition eventDef = (EventTypeDefinition)iter.next();
            values.add(eventDef.getType());
        }
        
        List result = new LinkedList(values);
        Collections.sort(result);
        
        return result;
    }
    
    private HashMap buildNewCache() throws EventProcessingException
    {
        HashMap result = new HashMap();
        
        JDBDatabaseAccess connection = null;
        try
        {
            connection = EventDBUtil.getDefaultDBAccess();
            
            SerializedSpecMap specMap =
                new SerializedSpecMap(
                    EventDBUtil.getSecuritySession(),
                    EventDBUtil.getSpecDBAccess(),
                    new JDBServiceCache());
            
            //
            //  Select all of the event records from the database.
            //
            JDBResultSet resultSet = 
                connection.select(F90705_EventActivationStatus.TABLE, 
                                  F90705_EventActivationStatus.FIELDS);
            try
            {
                HashMap events = new HashMap();
                while (resultSet.hasMoreRows())
                {
                    JDBFieldMap fieldMap = resultSet.fetchNext();
                    
                    //
                    //  The data structures that go into an event do not vary by environment.
                    //  So, if this same event type has already been loaded for another
                    //  environment then make a copy of the one that has already been loaded
                    //  and set the environment and active status accordingly.  This prevents
                    //  additional trips to the database and speeds up the loading.
                    //
                    String name = fieldMap.getString(F90705_EventActivationStatus.EVNTNAME);
                    String type = fieldMap.getString(F90705_EventActivationStatus.EVNTTYPE);
                    
                    EventTypeDefinition existingEvent = 
                        (EventTypeDefinition)events.get(name + type);
                    
                    EventTypeDefinition eventDef = null;
                    
                    /**
                     * SAR 7884671 - handle "EventProcessingException" to skip loading only this event.
                     * In case of invalid event definition exists into F90701 (for example, 
                     * lower case event aggreate type is entered by hand), we should skip loading only the event, but,
                     * continue to load the rest of event definitions.
                     */
                    try
                    {
	                    if (existingEvent != null)
	                    {
	                        //
	                        //  This type has already been loaded so make a copy.
	                        //
	                        eventDef = new EventTypeDefinition(fieldMap, existingEvent);
	                    }
	                    else
	                    {
	                        //
	                        //  This type has not been loaded so load it from the database.
	                        //
	                        eventDef = new EventTypeDefinition(fieldMap, connection, specMap);
	                        events.put(name + type, eventDef);
	                    }
	                    EventTypeCacheKey key =
	                        new EventTypeCacheKey(
	                            eventDef.getCategory(),
	                            eventDef.getType(),
	                            eventDef.getEnvironment());
	                    result.put(key, eventDef);

					} catch (EventProcessingException epe)
					{
						String msg = "Failed to load event definition " + name + " type " + type;
						sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, epe);                    	
					}
                }
            }
            catch (JDBException e)
            {
                String msg = "Failed to load event definitions into event cache: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
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
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database result set: " + e.getMessage(),null, null, e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to load event definitions into event cache: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
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
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database connection: " + e.getMessage(),null, null, e);
                }
            }
        }
        
        return result;
    }
}
