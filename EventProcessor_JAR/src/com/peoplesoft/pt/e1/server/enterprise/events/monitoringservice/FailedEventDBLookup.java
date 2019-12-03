//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.monitoringservice;

//=================================================
// Imports from java namespace
//=================================================
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

//=================================================
// Imports from javax namespace
//=================================================
 import javax.management.ObjectName;
 
//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.JDECalendar;
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBCompositeSelection;
import com.jdedwards.database.base.JDBConjunctionOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.jdedwards.database.base.JDBSortDirection;
import com.jdedwards.database.base.JDBSortField;
import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
import com.jdedwards.mgmt.agent.Server;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.FailedEventMessage;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90712_FailedEvent;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Description of the class.
 */
public class FailedEventDBLookup
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(FailedEventDBLookup.class.getName());

    /**  Fields to select.  */
    private static final JDBField[] SELECT_FIELDS =
        new JDBField[] 
        { 
            F90712_FailedEvent.EVNTID, // Event ID
            F90712_FailedEvent.EVNTSEQ, // Sequence number
            F90712_FailedEvent.EVNTNAME, // Name (a.k.a. event type)
            F90712_FailedEvent.EVNTTYPE, // Type (a.k.a. category)
            F90712_FailedEvent.EVNTST, // State
            F90712_FailedEvent.ENV, // Environment
            F90712_FailedEvent.EVNTSNS, // User session
            F90712_FailedEvent.EVNTHOST, // Event Host
            F90712_FailedEvent.EVNTTIME, //Event Time
            F90712_FailedEvent.EVNTUSER, //USer Name
            F90712_FailedEvent.EVNTBSFN, //Business function
            F90712_FailedEvent.EVNTFPT //Failed Message
        };

    /** Sort by ascending sequence number. */
    private static final JDBSortField SORT =
        new JDBSortField(F90712_FailedEvent.EVNTSEQ, JDBSortDirection.ASCENDING);

    //=================================================
    // Instance member fields.
    //=================================================

    private JDBSelection mSelection = null;
    
    private JDBCompositeSelection mCompositeSelection = null;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get failed events that are ready to be deleted.
     * 
     * @param  connection  Database connection to use for retreiving committed events.
     * @return  A hashmap of <code>CommittedEventKey</code> objects sorted by their
     *          sequence numbers.
     * @throws EventProcessingException  Error getting committed events from the database.
     */
    ArrayList getFailedEvents(JDBDatabaseAccess connection) throws EventProcessingException
    {
        ArrayList result = new ArrayList();
        try
        {
            //
            //  Select failed events from the database.
            //
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Select failed events from database begin.", null, null, null);
            }
            JDBResultSet resultSet =
                connection.select(F90712_FailedEvent.TABLE, SELECT_FIELDS, getSelection(), SORT);
            try
            {
                if(sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Deregistering rte_monitoring_failed_event_info MBeans for Management Console", null, null, null);
                }
                if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {
                    
                    try{
                        Set set = Server.getServer().getMBeanServer().queryNames(new ObjectName("jde:targetType=rteserver" + ",instanceName=" + E1Agent.getAgent().getInstanceName() + ",metricName=rte_monitoring_failed_event_info" + ",*"), null);
                        for (Iterator i = set.iterator(); i.hasNext(); ) {
                            
                            ObjectName on = (ObjectName)i.next();
                            Server.getServer().unregisterComponent(on);
                            if(sE1Logger.isDebug())
                            {
                                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_failed_event_info MBean deregistered: " + on.toString(), null, null, null);
                            }
                            
                        }
                        
                    } catch(Exception e){
                    
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Failed to query the MbeanServer for rte_monitoring_failed_event_info mbeans: " + e.getMessage(), null, null, e);
                        
                    }
                
                }
                
                while (resultSet.hasMoreRows())
                {
                    JDBFieldMap map = resultSet.fetchNext();
                    FailedEventMessage failedEventMsg = buildFailedEvent(map);
                    result.add(failedEventMsg);
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
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(), null, null, e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Exception from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Select failed events from database complete.",  null, null, null);
        }
        return result;
    }

    private FailedEventMessage buildFailedEvent(JDBFieldMap map) throws JDBException
    {
        FailedEventMessage event = new FailedEventMessage();
        try
        {
            String eventID = map.getString(F90712_FailedEvent.EVNTID).trim();
            String type = map.getString(F90712_FailedEvent.EVNTNAME).trim();
            String category = map.getString(F90712_FailedEvent.EVNTTYPE).trim();
            String environment = map.getString(F90712_FailedEvent.ENV).trim();
            String host = map.getString(F90712_FailedEvent.EVNTHOST).trim();
            String bsfn = map.getString(F90712_FailedEvent.EVNTBSFN).trim();
            String failedMsg = map.getString(F90712_FailedEvent.EVNTFPT).trim();
            JDECalendar dateTime = (JDECalendar) map.getValue(F90712_FailedEvent.EVNTTIME);
            tagMathNumeric sequence = (tagMathNumeric) map.getValue(F90712_FailedEvent.EVNTSEQ);
            event.setEventID(eventID);
            event.setType(type);
            event.setCategory(category);
            event.setEnvironment(environment);
            event.setSequenceNumber(sequence.asBigDecimal().longValue());
            event.setDateTime(dateTime);
            event.setHost(host);
            event.setBSFN(bsfn);
            event.setFailedMessage(failedMsg);
            
            /**After creating the FailedEventMessage object register it with the Manangement Console.  Leaving
             * this function's original return of ArrayList for other implementations, but the Management Console
             * knows nothing of the FailedEventMessage class (or EventMessage for that matter) and therefore can't 
             * receive it as a return type in ArrayList
             **/
            event.registerObject(eventID);
        }
        catch(JDBException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception creating FailedEventMessage. "+ ex, null, null,ex);
        }
        return event;
    }
    
    /**
     * Deletes all the failed events from the database.
     * @param connection JDBDatabaseAccess 
     * @return number of records deleted
     * @throws EventProcessingException ex
     */
    public long deleteFailedEvents(JDBDatabaseAccess connection) throws EventProcessingException
    {
        return deleteFailedEvents(connection, null, true);
    }
    
    /**
     * Deletes all the failed events from the database.
     * @param connection JDBDatabaseAccess 
     * @param eventID String
     * @param deleteAll true we need to delete all.
     * @return number of records deleted
     * @throws EventProcessingException ex
     */
    public long deleteFailedEvents(JDBDatabaseAccess connection, 
                        String eventID, boolean deleteAll) throws EventProcessingException
    {
        long numOfRecords = 0;
        try
        {
            //
            //  delete failed events from the database.
            //
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Delete failed events from database begin.", null, null, null);
            }
            if(deleteAll)
            {
                numOfRecords =
                    connection.delete(F90712_FailedEvent.TABLE, getSelection());
            }
            else
            {
                numOfRecords =
                    connection.delete(F90712_FailedEvent.TABLE, getSelectionWithEventID(eventID));
            }
        }
        catch (JDBException e)
        {
            String msg = "Exception from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            e.printStackTrace();
            throw new EventProcessingException(msg, e);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Deleted " + numOfRecords +" events from database.", null, null, null);
        }
        return numOfRecords;
    }

    private JDBSelection getSelection() throws JDBException
    {
        if (mSelection == null)
        {
            mSelection =
                new JDBFieldComparison(
                    F90712_FailedEvent.EVNTST,
                    JDBComparisonOp.EQ,
                    F90712_FailedEvent.STATE_FAILED);
        }
        return mSelection;
    }
    
    private JDBSelection getSelectionWithEventID(String eventID) throws JDBException
    {
        if (mCompositeSelection == null)
        {
            mCompositeSelection = new JDBCompositeSelection();
            mCompositeSelection.addSelection(new JDBFieldComparison
                                                (F90712_FailedEvent.EVNTST, JDBComparisonOp.EQ, 
                                                    F90712_FailedEvent.STATE_FAILED));
            mCompositeSelection.addSelection(JDBConjunctionOp.AND, 
                                                new JDBFieldComparison(F90712_FailedEvent.EVNTID, 
                                                    JDBComparisonOp.EQ, eventID));               
        }
        return mCompositeSelection;
    }
}
