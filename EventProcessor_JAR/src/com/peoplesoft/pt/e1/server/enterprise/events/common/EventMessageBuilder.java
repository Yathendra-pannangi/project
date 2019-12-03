//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.JDECalendar;
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBParameterMarker;
import com.jdedwards.database.base.JDBResultSet;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Build <code>EventMessage</code> objects from rows in the database.
 */
public class EventMessageBuilder
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventMessageBuilder.class.getName());
    
    private static final int MAX_DB_TRIES = 3;

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    /**  Parameter marks for database updates. */
    private JDBParameterMarker mEventIDMarker = null;
    
    /**  Field comparison for the event ID field. */
    private JDBFieldComparison mEventIDFieldCompare = null;
    
    /**  Fields to select.  */
    private static final JDBField[] SELECT_FIELDS = new JDBField[] 
    {
        F90710_EventTransfer.EVNTID,      // Event ID
        F90710_EventTransfer.EVNTTIME,    // Event date/time
        F90710_EventTransfer.EVNTSEQ,     // Sequence number
        F90710_EventTransfer.EVNTNAME,    // Name (a.k.a. event type)
        F90710_EventTransfer.EVNTTYPE,    // Type (a.k.a. category)
        F90710_EventTransfer.EVNTST,      // State
        F90710_EventTransfer.ENV,         // Environment
        F90710_EventTransfer.EVNTUSER,    // User
        F90710_EventTransfer.UGRP,        // Group
        F90710_EventTransfer.OBNM,        // Application
        F90710_EventTransfer.VER,         // Application version
        F90710_EventTransfer.EVNTSNS,     // User session
        F90710_EventTransfer.EVNTSCOPE,   // Event scope
        F90710_EventTransfer.EVNTHOST,    // Originating host
        F90710_EventTransfer.EVNTSRT,     // Source routing field
        F90710_EventTransfer.EVNTBSFN,    // Originating BSFN name
        F90710_EventTransfer.FCTNM,       // Function name
        F90710_EventTransfer.EVNTPRID,    // Originating process ID
    };
    
    /**  Data fields to select.  */
    private static final JDBField[] DATA_SELECT_FIELDS = new JDBField[] 
    {
        F90710_EventTransfer.EDATA,       // Event data field
    };

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     */
    public EventMessageBuilder()
    {
        //
        //  Build the objects needed to do efficent database updates and deletes.
        //
        mEventIDMarker = new JDBParameterMarker();
        mEventIDFieldCompare =
            new JDBFieldComparison(F90710_EventTransfer.EVNTID, JDBComparisonOp.EQ, mEventIDMarker);

    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get an event from the database.
     * 
     *  @param  connection  The database connection to use.
     * 
     *  @param  eventID  The ID of the event to retreive.
     * 
     *  @return The requested event.
     * 
     *  @throws EventProcessingException  An error occured.
     */
    public EventMessage getEventMessage(JDBDatabaseAccess connection, String eventID)
        throws EventProcessingException
    {
        //
        //  Try getting the event from the database.  Several tries are made because
        //  there are some rare situations with some database servers where this operation
        //  may fail due to a preceived deadlock in the database.
        //
        EventMessage result = null;
        int tries = 0;
        boolean success = false;
        do
        {
            try
            {
                result = getEventMessageImpl(connection, eventID);
                success = true;
            }
            catch (EventProcessingException e)
            {
                if (++tries < MAX_DB_TRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, "Failed to get event from database, retrying", null, null, null);
                }
                else
                {
                    throw e;
                }
            }
        } while (!success);
        
        return result;
    }
        
    private EventMessage getEventMessageImpl(JDBDatabaseAccess connection, String eventID)
        throws EventProcessingException
    {
        EventMessage result = null;
        try
        {
            mEventIDMarker.setValue(eventID);
            JDBResultSet resultSet =
                connection.select(
                    F90710_EventTransfer.TABLE,
                    SELECT_FIELDS,
                    mEventIDFieldCompare,
                    null);
            try
            {
                if (resultSet.hasMoreRows())
                {
                    //
                    //  Build an event message object.
                    //
                    JDBFieldMap map = resultSet.fetchNext();
                    result = buildEventMessage(map);
                }
            }
            finally
            {
                try
                {
                    resultSet.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(),null, null, e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to get event from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        return result;
    }

    /**
     *  Get's an event's data BLOB from the database.
     * 
     *  @param connection  Database connection to use.
     * 
     *  @param eventID  The ID of the event to retreive.
     * 
     *  @return  The requested event's data BLOB.
     * 
     *  @throws EventProcessingException  An error occured.
     */
    public byte[] getEventData(JDBDatabaseAccess connection, String eventID)
        throws EventProcessingException
    {
        //
        //  Try getting the data from the database.  Several tries are made because
        //  there are some rare situations with some database servers where this operation
        //  may fail due to a preceived deadlock in the database.
        //
        byte[] result = null;
        int tries = 0;
        boolean success = false;
        do
        {
            try
            {
                result = getEventDataImpl(connection, eventID);
                success = true;
            }
            catch (EventProcessingException e)
            {
                if (++tries < MAX_DB_TRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Failed to get event data from database, retrying", null, null,null);
                }
                else
                {
                    throw e;
                }
            }
        } while (!success);
        
        return result;
    }
        
    private byte[] getEventDataImpl(JDBDatabaseAccess connection, String eventID)
        throws EventProcessingException
    {
        byte[] result = null;
        try
        {
            mEventIDMarker.setValue(eventID);
            JDBResultSet resultSet =
                connection.select(
                    F90710_EventTransfer.TABLE,
                    DATA_SELECT_FIELDS,
                    mEventIDFieldCompare,
                    null);
            try
            {
                if (resultSet.hasMoreRows())
                {
                    //
                    //  Build an event message object.
                    //
                    JDBFieldMap map = resultSet.fetchNext();
                    result = (byte[])map.getValue(F90710_EventTransfer.EDATA);
                }
                else
                {
                    String msg = 
                        "Failed to find event in database, eventID=" + eventID;
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
                    throw new EventProcessingException(msg);
                }
            }
            finally
            {
                try
                {
                    resultSet.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(),null, null, e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to get event from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        return result;
    }

    /**
     *  Build an event message from a result set.
     */
    private EventMessage buildEventMessage(JDBFieldMap map)
        throws JDBException, EventProcessingException
    {
        EventMessage event = new EventMessage();
        String tmp = null;
        
        tmp = map.getString(F90710_EventTransfer.EVNTID);
        if (tmp != null)
        {
            event.setEventID(tmp.trim());
        }
        else
        {
            String msg = "null event ID found in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTNAME);
        if (tmp != null)
        {
            event.setType(tmp.trim());
        }
        else
        {
            String msg = "null event type found in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTTYPE);
        if (tmp != null)
        {
            event.setCategory(tmp.trim());
        }
        else
        {
            String msg = "null event category found in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        
        tmp = map.getString(F90710_EventTransfer.ENV);
        if (tmp != null)
        {
            event.setEnvironment(tmp.trim());
        }
        else
        {
            String msg = "null event environment found in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTHOST);
        if (tmp != null)
        {
            event.setHost(tmp.trim());
        }
        else
        {
            String msg = "null event host found in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,null);
            throw new EventProcessingException(msg);
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTUSER).trim();
        if (tmp != null)
        {
            event.setUser(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.UGRP);
        if (tmp != null)
        {
            event.setRole(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.OBNM);
        if (tmp != null)
        {
            event.setApplication(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.VER);
        if (tmp != null)
        {
            event.setApplicationVersion(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTSNS);
        if (tmp != null)
        {
            event.setSessionID(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTSCOPE);
        if (tmp != null)
        {
            event.setScope(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTSRT);
        if (tmp != null)
        {
            event.setSourceRoute(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTBSFN);
        if (tmp != null)
        {
            event.setBSFN(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.FCTNM);
        if (tmp != null)
        {
            event.setFunction(tmp.trim());
        }
        
        tmp = map.getString(F90710_EventTransfer.EVNTPRID);
        if (tmp != null)
        {
            event.setProcessID(tmp.trim());
        }
        
        //
        //  Sequence number.
        //
        tagMathNumeric sequence = (tagMathNumeric)map.getValue(F90710_EventTransfer.EVNTSEQ);
        event.setSequenceNumber(sequence.asBigDecimal().longValue());
        
        //
        //  Originating date/time.
        //
        JDECalendar dateTime = (JDECalendar)map.getValue(F90710_EventTransfer.EVNTTIME);
        if (dateTime == null)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"null event date/time stamp found in database",null, null,null);
        }
        event.setDateTime(dateTime);
        
        return event;
    }
}
