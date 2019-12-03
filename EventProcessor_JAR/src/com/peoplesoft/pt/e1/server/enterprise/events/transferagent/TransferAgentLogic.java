//============================================================================
//
// Copyright © [2004]
// PeopleSoft, Inc.
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.transferagent;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Iterator;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBErrorCode;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldAssignment;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldFunction;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBParameterMarker;
import com.jdedwards.database.base.JDBPropertyName;
import com.jdedwards.database.base.JDBSelection;
import com.jdedwards.database.base.JDBTransaction;
import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
import com.jdedwards.mgmt.agent.Server;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.EventMonitoringFactory;
import com.peoplesoft.pt.e1.server.common.events.monitoring.IEventMonitoring;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition;
import com.peoplesoft.pt.e1.server.enterprise.events.common.CommittedEventKey;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventMessageBuilder;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventSequencingType;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventState;
import com.peoplesoft.pt.e1.server.enterprise.events.common.MetricsManager;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TestCondition;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TestConditionFactory;
import com.peoplesoft.pt.e1.server.enterprise.events.common.UnknownEventTypeException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.UnknownSequencingTypeException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90708_EventSequence;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90712_FailedEvent;

import java.math.BigDecimal;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Implementation logic for the event transfer agent.
 * <br>
 * Note: this class is designed to be used as the implementation logic of the event
 * transfer agent, which there can only be a single instance of if the system is to
 * function correctly.  As a result, this class is not designed to be used by multiple
 * threads concurrently.
 */
public class TransferAgentLogic
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    /**  Max events to transfer in one session.  */
    private static final int MAX_TRANSFER_SESSION_DEFAULT = 1000;

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(TransferAgentLogic.class.getName());

    private static final String EVENT_SECTION = "EVENTS";

    private static final String SEQUENCING_PROP = "sequencingBehavior";

    private static final String MAX_TRAN_SESSION_PROP = "maxTransferSessionSize";

    private static final long UNINITIALIZED_LAST_SEQ_NUM = -100;

    //=================================================
    // Instance member fields.
    //=================================================

    //  Array of output queues to send event to.
    private EventQueue[] mEventOutputQueues;

    //  Initialization status.
    private boolean mInitialized = false;

    //  Database connection
    private JDBDatabaseAccess mDBAccess;

    //  Parameter marks for database updates
    private JDBParameterMarker mEventIDMarker = null;

    //  Field comparison for the event ID field.
    private JDBFieldComparison mEventIDFieldCompare = null;

    //  Field map used for database updates.
    private JDBFieldAssignment mFieldMap = null;

    //  Committed event factory.
    private CommittedEventKeyFactory mCommittedEventKeyFactory = new CommittedEventKeyFactory();

    private EventSequencingType mSequencingType = null;

    //  Next output queue to use when using round-robin routing.
    private int mNextOutputQueue = 0;

    //  The last event sequence number processed.
    private long mLastSeqNum = UNINITIALIZED_LAST_SEQ_NUM;
    private long mLastSessionNum = 0;
    private long mThisSessionNum = 0;

    //  Maximum number of events to transfer in one session.
    private int mMaxTransferSessionSize = MAX_TRANSFER_SESSION_DEFAULT;

    //  Metrics values.
    private MetricsManager mMetricsManager = new MetricsManager();

    private int mRTCount   = 0;
    private int mXAPICount = 0;
    private int mWFCount   = 0;
    private int mZCount    = 0;


    private long mEvtSeqBegin = 0;
    private long mEvtSeqEnd = 0;

    //  Should test conditions be created?
    private TestCondition mTestCondition = TestConditionFactory.getInstance();

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Perform initialization when started up.  Must be called before the
     * first call to <code>transferEvents()</code>.
     *
     * @throws EventProcessingException  An error occured during initialization.
     */
    public void initialize() throws EventProcessingException
    {
        if(sE1Logger.isDebug())
        {
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering TransferAgentLogic.initialize()", null, null, null);
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event Transfer Agent starting initialization", null, null, null);
        }

        //
        //  Open a database conneciton.
        //
        openDBConnection();

        initializeSeqTable();

        //
        //  Initialize the committed event factory.
        //
        mCommittedEventKeyFactory.initialize();

        //
        //  Initialize the output queues.
        //
        try
        {
            initializeOutputQueues();
        }
        catch (EventProcessingException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                "Failed to initialize event transfer agent output queues: " + e.getMessage(),
                null, null, e);

            //
            //  Try to close the database connection.
            //
            try
            {
                mDBAccess.close();
            }
            catch (JDBException e1)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database connection: " + e.getMessage(), null, null, e1);
            }
            throw e;
        }

        //
        //  Load the sequencing behavior.
        //
        mSequencingType = loadSequencing();

        //
        //  Get the maximum transfer session size.
        //
        mMaxTransferSessionSize = loadMaxSessionSize();

        //
        //  Initialize objects used for metrics reporting.
        //
        mMetricsManager.initialize();

        //
        //  Initialization is now complete.
        //
        mInitialized = true;

        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Registering rte_monitoring_operations MBean for Management Console", null, null, null);
        }
        /* Register the MonitoringService to the management framework */
        if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {

            IEventMonitoring obj = EventMonitoringFactory.getMonitoringService();
            Server.getServer().registerRuntimeMetric(obj, null, "rte_monitoring_operations");

            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_operations MBean registered.", null, null, null);
            }
        }

        if(sE1Logger.isDebug())
        {
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event Transfer Agent initialization complete", null, null, null);
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Exiting TransferAgentLogic.initialize()", null, null, null);
    	}

    }

    /**
     * Frees resources when shutting down.  This method should be called when the
     * instance is no longer going to be used to insure any resources used by it
     * are released.
     */
    public void shutdown()
    {
        if(sE1Logger.isDebug())
        {
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering TransferAgentLogic.shutdown()", null, null, null);
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event Transfer Agent Shuting Down", null, null, null);
        }

        mInitialized = false;

        //
        //  Shutdown the output queues.
        //
        shutdownOutputQueues();

        //
        //  Close the database connection.
        //
        closeDBConnection();

        //
        //  Shutdown the committed event factory.
        //
        mCommittedEventKeyFactory.shutdown();

        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Deregistering rte_monitoring_operations MBean for Management Console", null, null, null);
        }
        if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {

            Server.getServer().removeRuntimeMetric(null, "rte_monitoring_operations");
            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_operations MBean deregistered.", null, null, null);
            }

        }


        if(sE1Logger.isDebug())
        {
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event Transfer Agent Shutdown Complete", null, null, null);
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Exiting TransferAgentLogic.shutdown()", null, null, null);
        }
    }

    /**
     * Performs an event transfer cycle where all committed events ready to be
     * transfered are transfer to event processing queues.
     *
     * @throws IllegalStateException  Methods was called before <code>initialize()</code>
     *                                or after <code>shutdown()</code>
     */
    public void transferEvents() throws IllegalStateException
    {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering TransferAgentLogic.transferEvents()", null, null, null);
        }

        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(TransferAgentLogic.class.getName()
                                            + " not initialized");
        }

//        //
//        //  If needed, initialize the last processed sequence number.
//        //
//        if (mLastSeqNum == UNINITIALIZED_LAST_SEQ_NUM)
//        {
//            mLastSeqNum = initializeLastSeqNum();
//
//            //
//            //  If the last sequence number is still not initialized then there are no
//            //  events in the database to be transfered.
//            //
//            if (mLastSeqNum == UNINITIALIZED_LAST_SEQ_NUM)
//            {
//                return;
//            }
//        }

        initializeLastSeqNum();

        //
        //  Get the events that are ready to be transfered.
        //
        List events = null;
        try
        {
            ++mThisSessionNum;
           if (mEvtSeqBegin != -1 && mEvtSeqEnd != -1) {
          //  events =
          //      mCommittedEventKeyFactory.getCommittedEvents(mLastSeqNum + mMaxTransferSessionSize);

            events =
                    mCommittedEventKeyFactory.getCommittedEvents(mEvtSeqBegin,mEvtSeqEnd);
//            if (events.size() >= mMaxTransferSessionSize)
//            {
//                //
//                //  There are probably more events in the database so queue another
//                //  trigger message.
//                //
//                TriggerTimerTask.getInstance().sendTrigger();
//            }
        }
        }
        catch (EventProcessingException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to get committed events from the database: " + e.getMessage(),null, null, e);
            return;
        }
        if(events != null && events.size() > 0)   {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Transfering " + events.size() + " events", null, null, null);
        }
        for (Iterator iter = events.iterator(); iter.hasNext();)
        {
            //
            //  Transfer each active event.  If an exception occurs then abort this transfer
            //  cycle.  Any untransfered events will be picked up in the next cycle.
            //
            CommittedEventKey eventKey = null;
            try
            {
                eventKey = (CommittedEventKey)iter.next();
                if (isEventActive(eventKey))
                {
                    //
                    //  Transfer the event.
                    //
                    transferEvent(eventKey);
                }
                else
                {
                    //
                    //  Delete the event.
                    //
                    deleteEvent(eventKey);
                }
            }
            catch (UnknownEventTypeException e)
            {
                //
                //  Move the event to the failed events table.
                //
                try
                {
                    eventFailed(eventKey, e.getMessage());
                }
                catch (Exception e1)
                {
                    String msg = "Exception while storing failed event: " + e1.getMessage();
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e1);
                    break;
                }
            }
            catch (Exception e)
            {
                String msg = "Exception while transfering events, transfer aborted: "
                             + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                break;
            }
            catch (Throwable t)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                    "Error while transfering events, transfer aborted: " + t.getMessage(),
				    null, null, t);
                break;
            }
        }

        //
        //  Save the updated metrics to the database.
        //
        storeMetrics();

        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Exiting TransferAgentLogic.transferEvents()", null, null, null);
        }
    }
}
    /**
     * Transfer a single event.
     */
    private void transferEvent(CommittedEventKey event) throws EventProcessingException
    {
        EventQueue outputQueue = selectOutputQueue(event);
        JDBTransaction dbTransaction = null;

        //
        //  Start a new database transaction.
        //
        try
        {
            dbTransaction = mDBAccess.getTransaction();
            dbTransaction.begin();
        }
        catch (JDBException e)
        {
            String msg = "Failed to start a DB transaction: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Update the event state in the database.
        //
        try
        {
            if (mTestCondition.shouldTransferDbUpdateFail())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Event Transfer Agent: simulating DB update failure, seqNum="
                        + event.getSequenceNumber(), null, null, null);
                mTestCondition.shouldTransferDbUpdateFail(false);
                throw new JDBException(JDBErrorCode.SQL_EXCEPTION_OCCURRED);
            }
            setEventState(event, EventState.IN_PROCESS);
        }
        catch (JDBException e)
        {
            String msg = "Failed to update event state in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            try
            {
                dbTransaction.rollback();
            }
            catch (JDBException e1)
            {
                //  Ignore.
            }
            throw new EventProcessingException(msg, e);
        }

        //
        //  Send the event to the event queue.
        //
        try
        {
            if (mTestCondition.shouldTransferQueueSendFail())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Event Transfer Agent: simulating queue send failure, seqNum="
                        + event.getSequenceNumber(), null, null, null);
                mTestCondition.shouldTransferQueueSendFail(false);
                throw new EventProcessingException("simulated queue send failure");
            }
            outputQueue.sendEvent(event);
        }
        catch (EventProcessingException e)
        {
            String msg = "Failed to send committed event key to queue";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);

            //
            //  Rollback the database state update
            //
            try
            {
                dbTransaction.rollback();
            }
            catch (JDBException e1)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to rollback event state update in database: "
                              + e1.getMessage(),null, null, e1);
            }
            throw e;
        }

        //
        //  Commit the event send on the queue.
        //
        try
        {
            if (mTestCondition.shouldTransferQueueCommitFail())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Event Transfer Agent: simulating queue commit failure, seqNum="
                        + event.getSequenceNumber(), null, null, null);
                mTestCondition.shouldTransferQueueCommitFail(false);
                outputQueue.rollback();
                throw new EventProcessingException("simulated queue commit failure");
            }
            outputQueue.commit();
        }
        catch (EventProcessingException e)
        {
            String msg = "Failed to commit send of committed event key to a queue";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);

            //
            //  Roll back the event state in the database.
            //
            try
            {
                dbTransaction.rollback();
            }
            catch (JDBException e1)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                    "Failed to roll back event state in database: " + e1.getMessage(),
				    null, null,e1);
            }
            throw e;
        }

        //
        //  Commit the database state update.
        //
        try
        {
            if (mTestCondition.shouldTransferDbCommitFail())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event Transfer Agent: simulating DB commit failure, seqNum="
                    + event.getSequenceNumber(), null, null, null);
                mTestCondition.shouldTransferDbCommitFail(false);
                dbTransaction.rollback();
                throw new JDBException(JDBErrorCode.SQL_EXCEPTION_OCCURRED);
            }
            dbTransaction.commit();
        }
        catch (JDBException e)
        {
            String msg = "Failed to commit event state update in database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Check the sequence number to make sure its increasing.
        //
        long thisSeqNum = event.getSequenceNumber();
        if (thisSeqNum <= mLastSeqNum)
        {
            StringBuffer buffer = new StringBuffer(150).append("Event sequence numbers out of order:");
            buffer.append(" thisSeqNum=").append(thisSeqNum);
            buffer.append(" lastSeqNum=").append(mLastSeqNum);
            buffer.append(" thisSession=").append(mThisSessionNum);
            buffer.append(" lastSession=").append(mLastSessionNum);
         //   sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,buffer.toString(), null, null, null);
        }
        mLastSeqNum = thisSeqNum;
        mLastSessionNum = mThisSessionNum;

        //
        //  Update the event metrics.
        //
        incrementMetric(event.getCategory(), 1);
    }

    private void openDBConnection() throws EventProcessingException
    {
        //
        //  Get the database connection.
        //
        try
        {
            //
            //  Get a default connection to the database.
            //
            mDBAccess = EventDBUtil.getExtendedTokenDefaultDBAccess();
            mDBAccess.setProperty(JDBPropertyName.AUTO_COMMIT, false);
        }
        catch (JDBException e)
        {
            String msg = "Failed to connect to database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Build the objects needed to do efficent database updates and deletes.
        //
        mEventIDMarker = new JDBParameterMarker();
        mEventIDFieldCompare =
            new JDBFieldComparison(F90710_EventTransfer.EVNTID, JDBComparisonOp.EQ, mEventIDMarker);
        mFieldMap = new JDBFieldAssignment();
    }

    private void initializeOutputQueues() throws EventProcessingException
    {
        mEventOutputQueues = EventQueue.getEventQueues();
    }

    private void closeDBConnection()
    {
        //
        //  Close DB connection.
        //
        try
        {
            mDBAccess.close();
        }
        catch (JDBException e)
        {
            String msg = "Failed to close database connection: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,  e);
        }
        mDBAccess = null;
    }

    private void shutdownOutputQueues()
    {
        for (int i = 0; i < mEventOutputQueues.length; i++)
        {
            try
            {
                mEventOutputQueues[i].shutdown();
            }
            catch (EventProcessingException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to shutdown an event queue: " + e.getMessage(),null, null, e);
            }
        }
        mEventOutputQueues = null;
    }

    /**
     * Select which output queue to send this event to.
     *
     * @param eventKey  The event to be sent.
     *
     * @return  The queue to send the event to.
     */
    private EventQueue selectOutputQueue(CommittedEventKey eventKey)
    {
        EventQueue result = mEventOutputQueues[0];
        if (mSequencingType.equals(EventSequencingType.GLOBAL_FIFO))
        {
            result = mEventOutputQueues[0];
        }
        else if (mSequencingType.equals(EventSequencingType.BY_EVENT_TYPE))
        {
            int index = eventKey.getType().hashCode() % mEventOutputQueues.length;
            index = Math.abs(index);
            result = mEventOutputQueues[index];
        }
        else if (mSequencingType.equals(EventSequencingType.BY_USER_SESSION))
        {
            int index =
                eventKey.getOriginatingUserSession().hashCode() % mEventOutputQueues.length;
            index = Math.abs(index);
            result = mEventOutputQueues[index];
        }
        else if (mSequencingType.equals(EventSequencingType.ANY_SEQUENCE))
        {
            int index = mNextOutputQueue;
            mNextOutputQueue = (mNextOutputQueue + 1) % mEventOutputQueues.length;
            result = mEventOutputQueues[index];
        }
        else
        {
            result = mEventOutputQueues[0];
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Unknown event sequencing type encountered: " + mSequencingType, null, null, null);
        }
        return result;
    }

    /**
     *  Set the state of an event.  This method must be called in the context of an existing
     *  transaction and will commit with that transaction.
     */
    private void setEventState(CommittedEventKey event, EventState state)
        throws JDBException
    {
        mFieldMap.clearFields();
        mFieldMap.addField(F90710_EventTransfer.EVNTST, new Integer(state.getValue()));

        mEventIDMarker.setValue(event.getEventID());
        mDBAccess.update(F90710_EventTransfer.TABLE, mEventIDFieldCompare, mFieldMap);
    }

    /**
     * Determines if the event is currently active.
     *
     * @param event  The event to test.
     *
     * @return  <code>true</code> = Active, <code>flase</code> = Not Active.
     *
     * @throws EventProcessingException  An error occured during processing.
     */
    private boolean isEventActive(CommittedEventKey event) throws EventProcessingException
    {
        EventTypeCache cache = EventTypeCache.getInstance();
        EventTypeDefinition eventDef
            = cache.getEventTypeDefinition(event.getCategory(),
                                           event.getType(),
                                           event.getOriginatingEnvironment());
        boolean result = true;
        if (eventDef != null)
        {
            result = eventDef.isActive();
        }
        else
        {
            String msg = "Failed to locate event type definition, category="
                         + event.getCategory() + ", type=" + event.getType()
                         + ", environment=" + event.getOriginatingEnvironment();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new UnknownEventTypeException(
                event.getCategory(),
                event.getType(),
                event.getOriginatingEnvironment());
        }

        return result;
    }

    /**
     * Deletes an event from the database.
     *
     * @param event  The event to delete.
     *
     * @throws EventProcessingException  An error occured during processing.
     */
    private void deleteEvent(CommittedEventKey event) throws JDBException
    {
        mEventIDMarker.setValue(event.getEventID());
        mDBAccess.getTransaction().begin();
        mDBAccess.delete(F90710_EventTransfer.TABLE, mEventIDFieldCompare);
        mDBAccess.getTransaction().commit();
    }

    private EventSequencingType loadSequencing()
    {
        EventSequencingType result = null;
        String typeString =
            JdeProperty.getProperty(
                EVENT_SECTION,
                SEQUENCING_PROP,
                EventSequencingType.GLOBAL_FIFO.getLabel());
        try
        {
            result = EventSequencingType.getSequencingType(typeString);
        }
        catch (UnknownSequencingTypeException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Unknown event sequencing behavior: " + typeString, null, null, e);
            result = EventSequencingType.GLOBAL_FIFO;
        }

        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Event sequencing behavior = " + result.getLabel(), null, null, null);

        return result;
    }

    private int loadMaxSessionSize()
    {
        int result = MAX_TRANSFER_SESSION_DEFAULT;
        String resultString =
            JdeProperty.getProperty(
                EVENT_SECTION,
                MAX_TRAN_SESSION_PROP,
                Integer.toString(MAX_TRANSFER_SESSION_DEFAULT));

        if (resultString != null)
        {
            result = Integer.parseInt(resultString);
        }

        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Max event transfer session size = " + result, null, null, null);

        return result;
    }

    private void incrementMetric(String category, int count)
    {
        if (category.equals(F90710_EventTransfer.CATEGORY_REAL_TIME))
        {
            mRTCount += count;
        }
        else if (category.equals(F90710_EventTransfer.CATEGORY_XAPI))
        {
            mXAPICount += count;
        }
        else if (category.equals(F90710_EventTransfer.CATEGORY_WORKFLOW))
        {
            mWFCount += count;
        }
        else if (category.equals(F90710_EventTransfer.CATEGORY_Z))
        {
            mZCount += count;
        }
        else
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Unknown event category: " + category, null, null, null);
        }
    }

    private void storeMetrics()
    {
        //
        //  Update the metric counts in the database.
        //
        JDBTransaction dbTransaction = null;
        try
        {
            dbTransaction = mDBAccess.getTransaction();
            dbTransaction.begin();

            if (mRTCount != 0)
            {
                String metric =
                    mMetricsManager.mapCategoryToName(F90710_EventTransfer.CATEGORY_REAL_TIME);
                mMetricsManager.incrementMetricCount(mDBAccess, metric, mRTCount);
            }

            if (mXAPICount != 0)
            {
                String metric =
                    mMetricsManager.mapCategoryToName(F90710_EventTransfer.CATEGORY_XAPI);
                mMetricsManager.incrementMetricCount(mDBAccess, metric, mXAPICount);
            }

            if (mWFCount != 0)
            {
                String metric =
                    mMetricsManager.mapCategoryToName(F90710_EventTransfer.CATEGORY_WORKFLOW);
                mMetricsManager.incrementMetricCount(mDBAccess, metric, mWFCount);
            }

            if (mZCount != 0)
            {
                String metric =
                    mMetricsManager.mapCategoryToName(F90710_EventTransfer.CATEGORY_Z);
                mMetricsManager.incrementMetricCount(mDBAccess, metric, mZCount);
            }

            dbTransaction.commit();
            mRTCount   = 0;
            mXAPICount = 0;
            mWFCount   = 0;
            mZCount    = 0;
        }
        catch (EventProcessingException e)
        {
            String msg = "Failed to update event metrics in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            try
            {
                dbTransaction.rollback();
            }
            catch (JDBException e1)
            {
                //  Ignore.
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to update event metrics in database";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            try
            {
                if (dbTransaction != null)
                {
                    dbTransaction.rollback();
                }
            }
            catch (JDBException e1)
            {
                //  Ignore.
            }
        }
    }

    /**
     *  Place event in failed events table.
     *
     *  @param event  The failed event.
     *
     *  @param msg  Description of the failure.
     */
    private void eventFailed(CommittedEventKey eventKey, String msg)
        throws EventProcessingException
    {
        //
        //  Get the event from the database.
        //
        JDBTransaction dbTransaction = null;
        try
        {
            dbTransaction = mDBAccess.getTransaction();
            dbTransaction.begin();
        }
        catch (JDBException e)
        {
            String errorMsg = "Failed to start database transaction: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null, e);
            throw new EventProcessingException(errorMsg, e);
        }
        EventMessageBuilder builder = new EventMessageBuilder();
        EventMessage event = builder.getEventMessage(mDBAccess, eventKey.getEventID());
        if(msg.length() > 254){
            msg = msg.substring(0,254);
        }
        //
        //  Fill the field map with the event's values.
        //
        JDBFieldAssignment fieldMap = new JDBFieldAssignment();
        fieldMap.addField(F90712_FailedEvent.EVNTID,    event.getEventID());
        fieldMap.addField(F90712_FailedEvent.EVNTTIME,  event.getDateTime());
        fieldMap.addField(F90712_FailedEvent.EVNTSEQ,   new Long(event.getSequenceNumber()));
        fieldMap.addField(F90712_FailedEvent.EVNTNAME,  event.getType());
        fieldMap.addField(F90712_FailedEvent.EVNTTYPE,  event.getCategory());
        fieldMap.addField(F90712_FailedEvent.EVNTST,
                          new Integer(F90712_FailedEvent.STATE_FAILED));
        fieldMap.addField(F90712_FailedEvent.ENV,       event.getEnvironment());
        fieldMap.addField(F90712_FailedEvent.EVNTUSER,  event.getUser());
        fieldMap.addField(F90712_FailedEvent.UGRP,      event.getRole());
        fieldMap.addField(F90712_FailedEvent.OBNM,      event.getApplication());
        fieldMap.addField(F90712_FailedEvent.VER,       event.getApplicationVersion());
        fieldMap.addField(F90712_FailedEvent.EVNTSNS,   event.getSessionID());
        fieldMap.addField(F90712_FailedEvent.EVNTSCOPE, event.getScope());
        fieldMap.addField(F90712_FailedEvent.EVNTHOST,  event.getHost());
        fieldMap.addField(F90712_FailedEvent.EVNTSRT,   event.getSourceRoute());
        fieldMap.addField(F90712_FailedEvent.EVNTFPT,   msg);
        fieldMap.addField(F90712_FailedEvent.EVNTBSFN,  event.getBSFN());
        fieldMap.addField(F90712_FailedEvent.FCTNM,     event.getFunction());
        fieldMap.addField(F90712_FailedEvent.EVNTPRID,  event.getProcessID());

        byte[] data = builder.getEventData(mDBAccess, event.getEventID());
        fieldMap.addField(F90712_FailedEvent.EDATA, data);

        //
        //  Insert the event into the failed events table and delete it from the transfer table.
        //
        try
        {
            mDBAccess.insert(F90712_FailedEvent.TABLE, fieldMap);
            mEventIDMarker.setValue(event.getEventID());
            mDBAccess.delete(F90710_EventTransfer.TABLE, mEventIDFieldCompare);
            dbTransaction.commit();
        }
        catch (JDBException e)
        {
            String error = "Failed to insert failed event into database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, error, null, null,  e);
            throw new EventProcessingException(error, e);
        }
    }

    /**
     *  Get the iitial value of last sequence number.  This is one less then the smallest
     *  sequence number in the database with a state of COMMITTED.
     */
//    private long initializeLastSeqNum()
//    {
//        long result = UNINITIALIZED_LAST_SEQ_NUM;
//
//        JDBField seqNumField =
//            new JDBField(F90710_EventTransfer.EVNTSEQ.getField(), JDBFieldFunction.MIN);
//        JDBField[] fields = new JDBField[1];
//        fields[0] = seqNumField;
//        JDBSelection selection =
//            new JDBFieldComparison(
//                F90710_EventTransfer.EVNTST,
//                JDBComparisonOp.EQ,
//                F90710_EventTransfer.STATE_COMMITTED);
//
//        JDBFieldMap map =null;
//        try
//        {
//            mDBAccess.getTransaction().begin();
//            map = mDBAccess.selectSingleRow(F90710_EventTransfer.TABLE, fields, selection, null);
//            mDBAccess.getTransaction().commit();
//        }
//        catch (JDBException e)
//        {
//            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
//                "Failed to get last event sequence number from database: " + e.getMessage(),
//			    null, null, e);
//            try
//            {
//                mDBAccess.getTransaction().rollback();
//            }
//            catch (JDBException e1)
//            {
//                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to rollback database transaction: " + e.getMessage(),null, null, e1);
//            }
//        }
//        if (map != null)
//        {
//            tagMathNumeric sequence = null;
//            try
//            {
//                sequence = (tagMathNumeric)map.getValue(seqNumField);
//            }
//            catch (JDBException e1)
//            {
//                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
//                    "Failed to get last event sequence number from result set: " + e1.getMessage(),
//				   null, null, e1);
//            }
//            if (sequence != null)
//            {
//                result = sequence.asBigDecimal().longValue() - 1;
//            }
//        }
//
//        return result;
//    }


 void initializeLastSeqNum() {
     JDBDatabaseAccess mConnection = null;
     JDBFieldMap map = null;
     mEvtSeqBegin = 0;
     mEvtSeqEnd = 0;
     long seqNo = 0;

     mEvtSeqBegin = getEventSequence(JDBFieldFunction.MIN);
     if (mEvtSeqBegin == -1) {
         return;
     }
     mEvtSeqEnd = getEventSequence(JDBFieldFunction.MAX);
     if (mEvtSeqEnd == -1) {
         return;
     }

     try {
         mConnection = EventDBUtil.getExtendedTokenDefaultDBAccess();
         mConnection.setProperty(JDBPropertyName.AUTO_COMMIT, false);
         mConnection.setProperty(JDBPropertyName.RESULT_SET_UPDATABLE,
                                 true);
     } catch (JDBException e) {
         String msg =
             "Failed to get DB connection for resetting event sequence number: " +
             e.getMessage();
         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
     }

     try {
         mConnection.getTransaction().begin();
         map =
 mConnection.selectSingleRow(F90708_EventSequence.TABLE, F90708_EventSequence.FIELDS,
                         null, null);
         if (map != null) {
             tagMathNumeric sequence = null;
             try {
                 sequence =
                         (tagMathNumeric)map.getValue(F90708_EventSequence.EVNTSEQ);
             } catch (JDBException e1) {
                 sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                  "Failed to get last event sequence number from result set: " +
                                  e1.getMessage(), null, null, e1);
             }
             if (sequence != null) {
                 long result = sequence.asBigDecimal().longValue();

                 if (result < mEvtSeqBegin) {
                     if ((mEvtSeqEnd - mEvtSeqBegin) >
                         mMaxTransferSessionSize) {

                         mEvtSeqEnd =
                                 mEvtSeqBegin + mMaxTransferSessionSize;
                     } else {
                         mEvtSeqEnd = mEvtSeqEnd + 1;
                     }
                     seqNo = mEvtSeqEnd;
                     mEvtSeqBegin = mEvtSeqBegin + 1;
                 }

                 else if ((result <= mEvtSeqEnd) || (result >= mEvtSeqBegin)) {
                        if ((mEvtSeqEnd - result) > mMaxTransferSessionSize) {

                            mEvtSeqEnd = result + mMaxTransferSessionSize;
                        } else {
                            mEvtSeqEnd = mEvtSeqEnd + 1;
                        }
                        seqNo = mEvtSeqEnd;
                        if (result < mEvtSeqBegin) {
                            mEvtSeqBegin = result + 1;
                                    }
                         }

                 else {
                     seqNo = result;
                     mEvtSeqBegin = -1;
                     mEvtSeqEnd = -1;
                 }

                 try {
                     tagMathNumeric valueMN = new tagMathNumeric();
                     valueMN.setBigDecimalValue(BigDecimal.valueOf(seqNo));
                     JDBFieldAssignment fieldMap = new JDBFieldAssignment();
                     fieldMap.addField(F90708_EventSequence.EVNTSEQ,
                                       valueMN);
                     mConnection.update(F90708_EventSequence.TABLE, null,
                                        fieldMap);
                     mConnection.getTransaction().commit();
                 } catch (JDBException e2) {
                     sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                      "Failed to update last event sequence number to database: " +
                                      e2.getMessage(), null, null, e2);
                     try {
                         mConnection.getTransaction().rollback();
                     } catch (JDBException e3) {
                         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                          "Failed to rollback database transaction: " +
                                          e3.getMessage(), null, null, e3);
                     }
                 }
             }
         }
     } catch (JDBException e) {
         String msg = "Exception from database: " + e.getMessage();
         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
     }

 }


 private long getEventSequence(JDBFieldFunction value) {

    // long result = 0;
     long result = -1;
     JDBField seqNumField =
         new JDBField(F90710_EventTransfer.EVNTSEQ.getField(), value,
            F90710_EventTransfer.EVNTSEQ.isFunctionAllowed());
     JDBField[] fields = new JDBField[1];
     fields[0] = seqNumField;
     JDBSelection selection =
         new JDBFieldComparison(F90710_EventTransfer.EVNTST,
                                JDBComparisonOp.EQ,
                                F90710_EventTransfer.STATE_COMMITTED);

     JDBFieldMap map = null;
     try {
         mDBAccess.getTransaction().begin();
         map =
 mDBAccess.selectSingleRow(F90710_EventTransfer.TABLE, fields, selection, null);
         mDBAccess.getTransaction().commit();
     } catch (JDBException e) {
         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                          "Failed to get last event sequence number from database: " +
                          e.getMessage(), null, null, e);
         try {
             mDBAccess.getTransaction().rollback();
         } catch (JDBException e1) {
             sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                              "Failed to rollback database transaction: " +
                              e.getMessage(), null, null, e1);
         }
     }
     if (map != null) {
         tagMathNumeric sequence = null;
         try {
             sequence = (tagMathNumeric)map.getValue(seqNumField);
         } catch (JDBException e1) {
             sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                              "Failed to get last event sequence number from result set: " +
                              e1.getMessage(), null, null, e1);
         }
         if (sequence != null) {
             result = sequence.asBigDecimal().longValue() - 1;
         }
     }

     return result;
 }

    void initializeSeqTable() {
        JDBDatabaseAccess mConnection = null;
        JDBFieldMap map = null;

        try {
            mConnection = EventDBUtil.getExtendedTokenDefaultDBAccess();
            mConnection.setProperty(JDBPropertyName.AUTO_COMMIT, false);
            mConnection.setProperty(JDBPropertyName.RESULT_SET_UPDATABLE,
                                    true);
        } catch (JDBException e) {
            String msg =
                "Failed to get DB connection for resetting event sequence number: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
        }

        try {
            mConnection.getTransaction().begin();
            map =
            mConnection.selectSingleRow(F90708_EventSequence.TABLE, F90708_EventSequence.FIELDS,
                            null, null);
            if (map == null) {
                tagMathNumeric valueMN = new tagMathNumeric();
                valueMN.setBigDecimalValue(BigDecimal.valueOf(0));
                JDBFieldAssignment fieldMap = new JDBFieldAssignment();
                fieldMap.addField(F90708_EventSequence.EVNTSEQ, valueMN);
                mConnection.insert(F90708_EventSequence.TABLE, fieldMap);
            }
		mConnection.getTransaction().commit();
        } catch (JDBException e) {
            String msg = "Exception from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
            try {
                mDBAccess.getTransaction().rollback();
            } catch (JDBException e1) {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                 "Failed to rollback database transaction: " +
                                 e.getMessage(), null, null, e1);
            }
        }

    }
}
