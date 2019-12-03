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
import java.util.HashSet;
import java.util.Set;

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
import com.jdedwards.database.base.JDBCompositeSelection;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F907071_SubscribedEvents;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F907072_SubscribedEnvironments;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90707_EventSubscription;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Describes an event subscription.
 */
public class Subscription
{
    
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(Subscription.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    
    private String  mDescription;

    private boolean mIsActive;
    
    private String  mName;
    
    private String  mUsername;
    
    private HashSet mSubscribedToEventTypes = new HashSet();
    
    private HashSet mSubscribedToEnvironments = new HashSet();
    
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param fieldMap  Map containing database content.
     * 
     *  @param connection  Database connection to use for additional queries.
     * 
     *  @throws JDBException  Error getting database from the database. 
     * 
     *  @throws EventProcessingException  Error encountered reading data.
     */
    Subscription(JDBFieldMap fieldMap, JDBDatabaseAccess connection)
        throws JDBException, EventProcessingException
    {
        String temp = null;
        
        temp = fieldMap.getString(F90707_EventSubscription.USER);
        if (temp != null)
        {
            mUsername = temp.trim();
        }
        else
        {
            String msg = "Event subscription found in database with null username";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        temp = fieldMap.getString(F90707_EventSubscription.EVNTSBNM);
        if (temp != null)
        {
            mName = temp.trim();
        }
        else
        {
            String msg = "Event subscription found in database with null subscription name";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        temp = fieldMap.getString(F90707_EventSubscription.EVNTSBDC);
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
            String msg = "Event subscription found in database with null active status";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        //
        //  Load the subscribed to event types.
        //
        mSubscribedToEventTypes = loadSubscribedToEvents(mUsername, mName, connection);
        
        //
        //  Load the subscribed to environments.
        //
        mSubscribedToEnvironments = loadSubscribedToEnvironments(mUsername, mName, connection);
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  Subscription description.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @return  Subscription name.
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     * @return  <code>true</code> indicates the subscription is active,
     *          <code>false</code> indicates the subscription is inactive.
     */
    public boolean isActive()
    {
        return mIsActive;
    }
        
    /**
     *  Load subscribed to events.
     */
    private HashSet loadSubscribedToEvents(String username, String subscriptionName,
                                           JDBDatabaseAccess connection)
        throws JDBException
    {
        HashSet result = new HashSet();
        
        //
        //  Get the subscribed to events.
        //
        JDBResultSet resultSet =
            connection.select(
                F907071_SubscribedEvents.TABLE,
                getSubscribedEventsSelection(username, subscriptionName),
                null);
        
        //
        //  Extract all the subscribed to event types from the result set.
        //
        try
        {
            while (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                String catName  = map.getString(F907071_SubscribedEvents.EVNTTYPE).trim();
                String typeName = map.getString(F907071_SubscribedEvents.EVNTNAME).trim();
                SubscribedToEventType type = new SubscribedToEventType(catName, typeName);
                result.add(type);
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
        
        return result;
    }
    
    /**
     *  Load subscribed to environments.
     */
    private HashSet loadSubscribedToEnvironments(String username, String subscriptionName,
                                                 JDBDatabaseAccess connection)
        throws JDBException
    {
        HashSet result = new HashSet();
        
        //
        //  Get the subscribed to environments.
        //
        JDBResultSet resultSet =
            connection.select(
                F907072_SubscribedEnvironments.TABLE,
                getSubscribedEnvironmentsSelection(username, subscriptionName),
                null);
        
        //
        //  Extract all the environments from the result set.
        //
        try
        {
            while (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                String environment = map.getString(F907072_SubscribedEnvironments.ENV).trim();
                result.add(environment);
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
        
        return result;
    }
    
    private JDBSelection getSubscribedEventsSelection(String username, String subName)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison userCompare =
            new JDBFieldComparison(
                F907071_SubscribedEvents.USER,
                JDBComparisonOp.EQ,
                username,
                true);

        //
        //  Build a field comparison for the subscription name field.
        //
        JDBFieldComparison subscriptionCompare =
            new JDBFieldComparison(
                F907071_SubscribedEvents.EVNTSBNM,
                JDBComparisonOp.EQ,
                subName,
                true);

        return new JDBCompositeSelection(userCompare, subscriptionCompare);
    }
    
    private JDBSelection getSubscribedEnvironmentsSelection(String username, String subName)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison userCompare =
            new JDBFieldComparison(
                F907072_SubscribedEnvironments.USER,
                JDBComparisonOp.EQ,
                username,
                true);

        //
        //  Build a field comparison for the subscription name field.
        //
        JDBFieldComparison subscriptionCompare =
            new JDBFieldComparison(
                F907072_SubscribedEnvironments.EVNTSBNM,
                JDBComparisonOp.EQ,
                subName,
                true);

        return new JDBCompositeSelection(userCompare, subscriptionCompare);
    }
    
    /**
     *  Get the collection of subscribed to environments.
     * 
     *  @return  A <code>Set</code> of subscribed to environment names.  The objects
     *           in the collection are of type <code>String</code>.
     */
    public Set getSubscribedToEnvironments()
    {
        return Collections.unmodifiableSet(mSubscribedToEnvironments);
    }
    
    /**
     *  Get the collection of subscribed to event types.
     * 
     *  @return  A <code>Set</code> of <code>SubscribedToEventType</code> objects.
     *
     */
    public Set getSubscribedToEventTypes()
    {
        return Collections.unmodifiableSet(mSubscribedToEventTypes);
    }
}
