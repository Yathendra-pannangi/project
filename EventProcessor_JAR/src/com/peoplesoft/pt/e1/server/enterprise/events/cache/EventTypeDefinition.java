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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.base.spec.SpecException;
import com.jdedwards.base.spec.SpecReference;
import com.jdedwards.base.spec.SpecType;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.jdedwards.database.services.spec.SerializedSpecMap;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F907011_EventDefinitionSubTypes;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90701_EventDefinition;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90705_EventActivationStatus;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Describes the definition of a particular event type in a given environment.
 */
public class EventTypeDefinition
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(EventTypeDefinition.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    
    private String    mCategory;
    
    private String    mType;
    
    private String    mEnvironment;
    
    private boolean   mIsActive;
    
    private HashMap   mDataStructuresMap = new HashMap();

    //=================================================
    // Constructors.
    //=================================================

    
    /**
     *  Copy constructor, except environment and active status.
     * 
     *  @param fieldMap  The field map of values.
     * 
     *  @param eventType  The event type to copy.
     * 
     *  @throws EventProcessingException  An error occured.
     * 
     *  @throws JDBException  An error occured.
     */
    EventTypeDefinition(JDBFieldMap fieldMap, EventTypeDefinition eventType)
        throws EventProcessingException, JDBException
    {
        mCategory    = eventType.mCategory;
        mType        = eventType.mType;
        mDataStructuresMap = eventType.mDataStructuresMap;

        String tmp = fieldMap.getString(F90705_EventActivationStatus.ENV);
        if (tmp != null)
        {
            mEnvironment = tmp.trim();
        }
        else
        {
            String msg = "null environment in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        tmp = fieldMap.getString(F90705_EventActivationStatus.EVNTACT);
        if (tmp != null)
        {
            mIsActive = tmp.trim().equalsIgnoreCase(F90705_EventActivationStatus.ACTIVE_STRING);
        }
        else
        {
            String msg = "null status in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
    }
    
    /**
     *  Constructor.
     * 
     *  @param fieldMap  The field map of values.
     * 
     *  @param connection Database connection to use.
     * 
     *  @param specMap  SpecMap to use for looking up data structure specs.
     * 
     *  @throws JDBException  An error occured.
     * 
     *  @throws EventProcessingException  An error occured.
     */
    EventTypeDefinition(
        JDBFieldMap fieldMap,
        JDBDatabaseAccess connection,
        SerializedSpecMap specMap)
        throws JDBException, EventProcessingException
    {
        String tmp = fieldMap.getString(F90705_EventActivationStatus.ENV);
        if (tmp != null)
        {
            mEnvironment = tmp.trim();
        }
        else
        {
            String msg = "null environment in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        tmp = fieldMap.getString(F90705_EventActivationStatus.EVNTNAME);
        if (tmp != null)
        {
            mType = tmp.trim();
        }
        else
        {
            String msg = "null name(type) in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        tmp = fieldMap.getString(F90705_EventActivationStatus.EVNTTYPE);
        if (tmp != null)
        {
            mCategory = tmp.trim();
        }
        else
        {
            String msg = "null type(category) in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        tmp = fieldMap.getString(F90705_EventActivationStatus.EVNTACT);
        if (tmp != null)
        {
            mIsActive = tmp.trim().equalsIgnoreCase(F90705_EventActivationStatus.ACTIVE_STRING);
        }
        else
        {
            String msg = "null status in database for event definition";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        //
        //  If this is a data structure type of event then load the data structures.
        //
        if ((mCategory.equals(F90710_EventTransfer.CATEGORY_REAL_TIME))
            || (mCategory.equals(F90710_EventTransfer.CATEGORY_XAPI)))
        {
            mDataStructuresMap = loadDataStructures(connection, mType, specMap, true);
        }
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Get the event's environment.
     * 
     * @return  The environment name.
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * Get the event's category.
     * 
     * @return  The category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * Get the event's type.
     * 
     * @return  The type.
     */
    public String getType()
    {
        return mType;
    }

    /**
     * Is the event active?
     * 
     * @return  <code>true</code> = Active, <code>false</code> = Inactive.
     */
    public boolean isActive()
    {
        return mIsActive;
    }
    
    /**
     *  Get the list of data structures defined for the event's data payload.
     * 
     *  @return  A list of <code>EventTypeDataStructure</code> objects.
     */
    public Map getDataStructures()
    {
        return Collections.unmodifiableMap(mDataStructuresMap);
    }
    
    /**
     *  Get the data structure spec using DSName.
     * 
     *  @param dsName String data structure name.
     * 
     *  @return  Object spec object for the DS or <code>null</code> if the named data
     *           structure is not defined for the event type.
     */
    public Object getDataStructureSpec(String dsName)
    {
        return mDataStructuresMap.get(dsName);
    }
    
    private HashMap loadDataStructures(
        JDBDatabaseAccess connection,
        String eventType,
        SerializedSpecMap specMap,
        boolean allowRecurse)
        throws JDBException, EventProcessingException
    {
        HashMap result = new HashMap();
        
        JDBResultSet resultSet =
            connection.select(F90701_EventDefinition.TABLE, getF90701Selection(eventType), null);
            
        //
        //  Extract event information.
        //
        try
        {
            if (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                String category = map.getString(F90701_EventDefinition.EVNTTYPE).trim();
                String type = map.getString(F90701_EventDefinition.EVNTNAME).trim();
                String aggregate = map.getString(F90701_EventDefinition.EVNTAGG).trim();
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                        "Loading data structures, Event: "
                            + category
                            + ":"
                            + type
                            + " aggregate="
                            + aggregate, null, null, null);
                }
                
                if (aggregate.equals(F90701_EventDefinition.SINGLE_STRING))
                {
                    //
                    //  A single event type with the data structure defined in the the same row.
                    //
                    String dsName = map.getString(F90701_EventDefinition.EVNTDS).trim();
                    if (sE1Logger.isDebug())
                    {
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Caching spec for data structure: " + dsName, null, null, null);
                    }
                    
                    //
                    //  Get the spec for the data structure.
                    //
                    SpecReference specReference = new SpecReference(dsName, SpecType.BSFNUBE_DSTR);
                    Object spec = null;
                    try
                    {
                        if (specMap.doesSpecExist(specReference))
                        {
                            spec = specMap.getSpec(specReference);
                            result.put(dsName,new EventTypeDataStructure(dsName, eventType, spec));
                        }
                        else
                        {
                            String msg = "Specs not found for data structure: " + dsName;
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                        }
                    }
                    catch (SpecException e)
                    {
                        String msg = "Specs not found for data structure: " + dsName;
                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                    }
                }
                else if (aggregate.equals(F90701_EventDefinition.CONTAINER_STRING))
                {
                    if (allowRecurse)
                    {
                        //
                        //  A container event type.  Find the sub-event types and get
                        //  their data structures.
                        //
                        List subEvents = getSubEvents(connection, eventType);
                        if (subEvents.size() <= 0)
                        {
                            String msg =
                                "Failed to find any sub-event types for event: " + eventType;
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                        }
                        for (Iterator iter = subEvents.iterator(); iter.hasNext();)
                        {
                            String subEvent = (String)iter.next();
                            result.putAll(loadDataStructures(connection, subEvent, specMap, false));
                        }
                    }
                    else
                    {
                        //
                        //  We should not find a container event at a level where we are not
                        //  allowed to recurse.
                        //
                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Invalid container event found in database, eventType="
                                      + eventType, null, null, null);
                    }
                }
                else
                {
                    String msg = "Unknown event aggregate type found in database: " + aggregate;
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                    throw new EventProcessingException(msg);
                }
            }
            else
            {
                //
                //  See if the event type is actually a data structure name.
                //
                SpecReference specReference = new SpecReference(eventType, SpecType.BSFNUBE_DSTR);
                Object spec = null;
                try
                {
                    if (specMap.doesSpecExist(specReference))
                    {
                        spec = specMap.getSpec(specReference);
                        result.put(
                            eventType,
                            new EventTypeDataStructure(eventType, eventType, spec));
                    }
                    else
                    {
                        String msg =
                            "Event type / data structure not found in database: " + eventType;
                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                    }
                }
                catch (SpecException e)
                {
                    String msg = "Event type / data structure not found in database: " + eventType;
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                }                
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
    
    private JDBSelection getF90701Selection(String eventType)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison selection =
            new JDBFieldComparison(
                F90701_EventDefinition.EVNTNAME,
                JDBComparisonOp.EQ,
                eventType,
                true);

        return selection;
    }
    
    private JDBSelection getF907011Selection(String eventType)
    {
        //
        //  Build a field comparison for the user field.
        //
        JDBFieldComparison selection =
            new JDBFieldComparison(
                F907011_EventDefinitionSubTypes.EVNTNAME,
                JDBComparisonOp.EQ,
                eventType,
                true);

        return selection;
    }
    
    private List getSubEvents(JDBDatabaseAccess connection, String eventType)
        throws EventProcessingException, JDBException
    {
        List result = new LinkedList();
        
        JDBResultSet resultSet =
            connection.select(
                F907011_EventDefinitionSubTypes.TABLE,
                getF907011Selection(eventType),
                null);
            
        //
        //  Extract the sub-events from the result set.
        //
        try
        {
            while (resultSet.hasMoreRows())
            {
                JDBFieldMap map = resultSet.fetchNext();
                String subEvent = map.getString(F907011_EventDefinitionSubTypes.EVNTSNAME).trim();
                result.add(subEvent);
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
}
