//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldAssignment;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBParameterMarker;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.CommittedEventKey;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventMessageBuilder;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventState;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.common.MetricsManager;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TestCondition;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TestConditionFactory;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90712_FailedEvent;
import com.jdedwards.database.base.JDBPropertyName;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Implements the logic for event processor.
 */
public class EventProcessorLogic
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventProcessorLogic.class.getName());
    
    private static final String EVENT_SECTION = "EVENTS";
    
    private static final String DELETE_PROP = "deleteCompletedEvents";
    
    private static final String SOURCE_ROUTE = "SourceRoute";
    
    //  Manager of event metrics.
    private static MetricsManager sMetricsManager = null;
    
    //  Database connection
    private static JDBDatabaseAccess sMetricsDBAccess;

    //=================================================
    // Instance member fields.
    //=================================================
    
    private boolean mInitialized = false;
    
    private HashMap mEventProcessorMap = null;
    
    private SubscriberManager mEventRouter = null;
    
    //  Database connection
    private JDBDatabaseAccess mDBAccess;
    
    //  Parameter marks for database updates
    private JDBParameterMarker mEventIDMarker = null;
    
    //  Field comparison for the event ID field.
    private JDBFieldComparison mEventIDFieldCompare = null;
    
    //  Field map used for database updates.
    private JDBFieldAssignment mFieldMap = null;
    
    //  Event builder that gets event data from the database.
    private EventMessageBuilder mEventBuilder = new EventMessageBuilder();
    
    //  Flag indicating if completed events should be deleted from the database.
    private boolean mDeleteEvents = true;
    
    private EventObjectOutputQueue mSourceRouteQueue = null;
    
    //  List of source routing output queues.
    private LinkedList mSourceRouteQueueList = null;
    
    //  List of events to be deleted.
    private LinkedList mDeleteList = new LinkedList();
    
    //  Interface to determine if test conditions should be simulated.
    private TestCondition mTestCondition = TestConditionFactory.getInstance();

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Perform initialization when started up. Must be called before the
     * first call to <code>processEvent()</code>.
     * 
     * @throws EventProcessingException  An error occured during initialization.
     */
    public void initialize() throws EventProcessingException
    {
        if(sE1Logger.isDebug())
        {
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Entering EventProcessorLogic.initialize()", null, null, null); 
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event processor instance initializing", null, null, null);
        }

        try
        {
            //
            //  Construct and initialize the event router.
            //
            mEventRouter = new SubscriberManager();
            mEventRouter.initialize();
            
            //
            //  Initialize the source routed queue.
            //
            initializeSourceRouteQueue();
    
            //
            //  Open a database conneciton.
            //
            openDBConnection();
            
            //
            //  Initialize event metrics.
            //
            initializeMetrics();
        
            //
            //  Initializes the event processor driver hashmap.
            //
            mEventProcessorMap = new HashMap();
            EventProcessorDriver driver = new DataStructureEventDriver();
            mEventProcessorMap.put(F90710_EventTransfer.CATEGORY_REAL_TIME, driver);
            mEventProcessorMap.put(F90710_EventTransfer.CATEGORY_XAPI,      driver);
            driver = new XMLEventDriver();
            mEventProcessorMap.put(F90710_EventTransfer.CATEGORY_WORKFLOW,  driver);
            mEventProcessorMap.put(F90710_EventTransfer.CATEGORY_Z,         driver);
            
            //
            //  See if completed events should be deleted from the database.
            //
            mDeleteEvents = JdeProperty.getProperty(EVENT_SECTION, DELETE_PROP, true);
            
            if(sE1Logger.isDebug())
            	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Event processor instance initialization complete", null, null, null);
        }
        catch (JDBException jdbE)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to initialize EventProcessor: " + jdbE.getMessage(), null, null, jdbE);
            if (mDBAccess != null)
            {
                //
                //  Try to close the database connection.
                //
                try
                {
                    mDBAccess.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error closing database connection: " + e.getMessage(), null, null, e);
                }
            }
            if (mEventProcessorMap != null)
            {
                mEventProcessorMap.clear();
                mEventProcessorMap = null;
            }
            throw new EventProcessingException(jdbE.getMessage(), jdbE);
        }

        mInitialized = true;
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Leaving EventProcessorLogic.initialize()", null, null, null);
    }

    /**
     * Frees resources when shutting down.
     * 
     * @throws EventProcessingException  Error during shutdown processing.
     */
    public void shutdown()  throws EventProcessingException
    {
        mInitialized = false;

        //
        // Shutdown the event router.
        //
        mEventRouter.shutdown();

	//
	// Shutdown the Source Routed Queue
	//
	shutdownSourceRouteQueue();        

        //
        //  Clear the event driver hash map.
        //
        mEventProcessorMap.clear();
        mEventProcessorMap = null;

        try
        {
            //
            //  Close the database connection.
            //
            closeDBConnection();
        }
        catch (JDBException jdbE)
        {
            throw new EventProcessingException(jdbE.getMessage(), jdbE);
        }
        if(sE1Logger.isDebug())
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Event processor instance shutdown complete", null, null, null);
    }

       private void shutdownSourceRouteQueue()
            throws EventProcessingException
       {
               mSourceRouteQueue.shutdown();
       }


    /**
     * Processes incoming events.
     * 
     * @param eventKey - Committed event key.
     * 
     * @throws EventProcessingException  An error occured while processing the event.
     */
    public void processEvent(CommittedEventKey eventKey)
        throws EventProcessingException
    {
        //
        //  Make sure initialization was performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(EventProcessorLogic.class.getName()
                                            + " not initialized");
        }
        
        //
        //  Generate some debug information about the event.
        //
        if (sE1Logger.isDebug())
        {
            StringBuffer buffer = new StringBuffer(180);
            buffer.append("Received committed event:");
            appendEventKey(buffer, eventKey);
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,buffer.toString(), null, null, null);
        }
        
        //
        //  Get the event from the database.
        //
        if (mTestCondition.shouldProcessorDbGetFail())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "Event Processor: simulating database get failure, SeqNum="
                    + eventKey.getSequenceNumber(), null, null, null);
            mTestCondition.shouldProcessorDbGetFail(false);
            throw new EventProcessingException("simulated database get failure");
        }
        EventMessage event = mEventBuilder.getEventMessage(mDBAccess, eventKey.getEventID());
        if (event == null)
        {
            //
            //  The event was not found in the database.  This can happen if for some reason
            //  the row in the database was deleted but the eventKey message was still in
            //  the queue.  Generate a warning message and return.
            //
            //  Note: an exception is not thrown here because that would return the message
            //        to the queue where it would be delivered again, causing an infinite
            //        loop condition.
            //
            StringBuffer msg = new StringBuffer(180);
            msg.append("Failed to find event in database: ");
            appendEventKey(msg, eventKey);
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, msg.toString(), null, null, null);
            return;
        }
        
        //
        //  See if the event is a source routed event.
        //
        Collection outputQueues = null;
		Collection outputTopics = null;
		
        if (isSourceRouted(event))
        {
            outputQueues = getSourceRoutedQueues(event);
	    }
        else
        {
            //
            //  Event should be routed based on subscriber subscriptions.
            //
            outputQueues = getSubscriptionRoutedQueues(event);
            outputTopics = getSubscriptionRoutedTopics(event);

        }
                
        //
        //  If the event needs to be delivered to any output queues then build the event message.
        //
        if (outputQueues != null && outputQueues.size() > 0)
        {
            //
            //  Build the event message using the appropriate driver.
            //
            EventProcessorDriver driver = 
                (EventProcessorDriver)mEventProcessorMap.get(event.getCategory());
            if (driver != null)
            {
                try
                {
                    event = driver.processEvent(mDBAccess, event);
                }
                catch (Exception e)
                {
                    StringBuffer buffer = new StringBuffer(225);
                    buffer.append("Failed to process event:");
                    buffer.append(System.getProperty("line.separator"));
                    buffer.append("   exception: ").append(e.getMessage());
                    appendEventKey(buffer, eventKey);
                    String msg = buffer.toString();
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    
                    eventFailed(event, "Failed to process event: " + e.getMessage(), 0);
                    markForDelete(event);
                    return;
                }
            }
            else
            {
                StringBuffer buffer = new StringBuffer("Unknown event category:");
                appendEventKey(buffer, eventKey);
                String msg = buffer.toString();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);

                msg = "Unknown event category: " + event.getCategory();
                eventFailed(event, msg, 0);
                markForDelete(event);
                return;
            }
            
            //
            //  Send the event to the output queues.
            //
	    String evtMsg = event.getXMLPayload();
            String evtXML = new ProcessEventForESB().initialize(evtMsg);
            
            Iterator iter = outputQueues.iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
                OutputQueue queue = (OutputQueue)iter.next();
                try
                {
                    if(queue.getQueueJNDIName().equalsIgnoreCase(JMSNames.ESB_QUEUE))
                    {
                        //XML Processing starts
                       // String evtXML = new ProcessEventForESB().initialize(evtMsg);
                        event.setXMLPayload(evtXML);
                    }
                    else {
                        event.setXMLPayload(evtMsg);
                    }

                    queue.sendEventMessage(event);
		    event.setXMLPayload(null);
                    incrementMetrics(event, queue);
                }
                catch (Exception e)
                {
                    StringBuffer buffer = 
                        new StringBuffer(225);
                    buffer.append("Failed to send event message to output queue:");
                    buffer.append(System.getProperty("line.seperator"));
                    buffer.append("   exception: ").append(e.getMessage());
                    appendEventKey(buffer, eventKey);
                    String msg = buffer.toString();
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    
                    msg = 
                        "Failed to send event message to output queue:"
                            + " [subscriber=" + queue.getSubscriberUsername()
                            + "] [error=" + e.getMessage() + "]";
                    eventFailed(event, msg, i);
                }
            }
            
        }  //  End if (outputQueues.size() > 0)

		//Check to see if there are topic subscribers
		if ((outputTopics != null) && (outputTopics.size() > 0))
		{
			//
			//  Build the event message using the appropriate driver.
			//
			EventProcessorDriver driver = 
				(EventProcessorDriver)mEventProcessorMap.get(event.getCategory());
			if (driver != null)
			{
				try
				{
					event = driver.processEvent(mDBAccess, event);
				}
				catch (Exception e)
				{
					StringBuffer buffer = new StringBuffer(225);
                                        buffer.append("Failed to process event:");
					buffer.append(System.getProperty("line.separator"));
					buffer.append("   exception: ").append(e.getMessage());
					appendEventKey(buffer, eventKey);
					String msg = buffer.toString();
					sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    
					eventFailed(event, "Failed to process event: " + e.getMessage(), 0);
					markForDelete(event);
					return;
				}
			}
			else
			{
				StringBuffer buffer = new StringBuffer("Unknown event category:");
				appendEventKey(buffer, eventKey);
				String msg = buffer.toString();
				sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);

				msg = "Unknown event category: " + event.getCategory();
				eventFailed(event, msg, 0);
				markForDelete(event);
				return;
			}
            
                        String evtMsg = event.getXMLPayload();
                        String evtXML = new ProcessEventForESB().initialize(evtMsg);                        
			//  Send the event to the output topics.
			Iterator iter = outputTopics.iterator();

			for (int i = 0; iter.hasNext(); i++)
			{
				OutputTopic topic = (OutputTopic)iter.next();
				try
				{
					event.setXMLPayload(evtXML);
                                        topic.sendEventMessage(event);
                                        event.setXMLPayload(null);
					incrementMetrics(event, topic);
				}
				catch (Exception e)
				{
					
					StringBuffer buffer = 
						new StringBuffer(225);
                    buffer.append("Failed to send event message to output Topic:");
					buffer.append(System.getProperty("line.seperator"));
					buffer.append("   exception: ").append(e.getMessage());
					appendEventKey(buffer, eventKey);
					String msg = buffer.toString();
					sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
                    
					msg = 
						"Failed to send event message to output Topic:"
							+ " [subscriber=" + topic.getSubscriberUsername()
							+ "] [error=" + e.getMessage() + "]";
					eventFailed(event, msg, i);
				}
			}
		}  //  End if ((outputTopics != null) && (outputTopics.size() > 0))
        
        //
        //  Delete the event from the database.
        //
        markForDelete(event);
        
        //
        //  See if a simulated failure has been requested.
        //
        if (mTestCondition.shouldProcessorFail())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "Event Processor: simulating failure, SeqNum="
                    + eventKey.getSequenceNumber(), null, null, null);
            mTestCondition.shouldProcessorFail(false);
            throw new EventProcessingException("simulated failure");
        }
    }

    private void openDBConnection() throws JDBException
    {
        //
        //  Get a default connection to the database.
        //
        mDBAccess = EventDBUtil.getExtendedTokenDefaultDBAccess();
        
        //
        //  Build the objects needed to do efficent database updates and deletes.
        //
        mEventIDMarker = new JDBParameterMarker();
        mEventIDFieldCompare =
            new JDBFieldComparison(F90710_EventTransfer.EVNTID, JDBComparisonOp.EQ, mEventIDMarker);
        mFieldMap = new JDBFieldAssignment();
    }

    private void closeDBConnection() throws JDBException
    {
        mDBAccess.close();
    }
    
    /**
     *  This event failed to be processed.
     * 
     *  @param event  The failed event.
     * 
     *  @param msg  Description of the failure.
     * 
     *  @param index  Value to append to the original event's ID to make a new
     *                unique ID.  Needed because one event may fail multiple times
     *                and a unique ID is needed for each one.
     */
    private void eventFailed(EventMessage event, String msg, int index)
        throws EventProcessingException
    {
        if(msg.length() > 254){
            msg = msg.substring(0,254);
        }
        JDBFieldAssignment fieldMap = new JDBFieldAssignment();
        
        //
        //  Fill the field map with the event's values.
        //
        fieldMap.addField(F90712_FailedEvent.EVNTID,    event.getEventID() + "." + index);
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
        
        byte[] data = mEventBuilder.getEventData(mDBAccess, event.getEventID());
        fieldMap.addField(F90712_FailedEvent.EDATA, data);

        try
        {
            mDBAccess.insert(F90712_FailedEvent.TABLE, fieldMap);
        }
        catch (JDBException e)
        {
            String error = "Failed to insert failed event into database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, error, null, null, e);
            throw new EventProcessingException(error, e);
        }
    }
    
    /**
     *  Queue an event ID for deletion from the database.  Events are held here until
     *  the outcome of the transaction is know.
     */
    private void markForDelete(EventMessage event)
    {
        mDeleteList.add(event);
    }
    
    /**
     *  Delete the events that have been marked for deletion.
     */
    public void deleteQueuedEvents()
    {
        for (Iterator iter = mDeleteList.iterator(); iter.hasNext();)
        {
            EventMessage event = (EventMessage)iter.next();
            deleteEvent(event);
        }
        mDeleteList.clear();
    }
    
    /**
     *  Clear the events marked for deletion.
     */
    public void clearDeleteQueue()
    {
        mDeleteList.clear();
    }
    
    /**
     *  Delete an event from the database.
     * 
     *  @param  event  Key of the event to be deleted.
     */
    private void deleteEvent(EventMessage event)
    {
        try
        {
            mEventIDMarker.setValue(event.getEventID());
            if (mDeleteEvents)
            {
                //
                //  Delete the event.
                //
                mDBAccess.delete(F90710_EventTransfer.TABLE, mEventIDFieldCompare);
            }
            else
            {
                //
                //  Set the event's state to Processed.
                //
                mFieldMap.clearFields();
                mFieldMap.addField(
                    F90710_EventTransfer.EVNTST,
                    Integer.toString(EventState.PROCESSED.getValue()));
                mDBAccess.update(F90710_EventTransfer.TABLE, mEventIDFieldCompare, mFieldMap);
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to update/delete event from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
        }
    }
    
    /**
     *  Determine if an event is a source routed event.  A source routed event in indicated
     *  by a non-empty source routed field in the event message.
     * 
     *  @param event  The event in question.
     * 
     *  @return  <code>true</code> = event is source routed,<br>
     *           <code>false</code> = event is not source routed.
     */
    private boolean isSourceRouted(EventMessage event)
    {
        boolean result = false;
        if ((event.getSourceRoute() != null) && (event.getSourceRoute().trim().length() > 0))
        {
            result = true;
        }
        return result;
    }
    
    private Collection getSourceRoutedQueues(EventMessage event) throws EventProcessingException
    {
        return mSourceRouteQueueList;
    }
    
    /**
     *  Process events that are routed based on subscriptions.
     */
    private Collection getSubscriptionRoutedQueues(EventMessage event)
        throws EventProcessingException
    {
        //
        //  Route the event based on subscriber subscriptions.
        //
        Collection subscriberQueues = null;
        try
        {
            subscriberQueues = mEventRouter.getInterestedSubscriberQueues(event);
        }
        catch (Exception e)
        {
            String msg = "Failed to route event: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            eventFailed(event, msg, 0);
            subscriberQueues = new LinkedList();
        }
        
        return subscriberQueues;
    }

	/**
	 *  Process events that are routed based on subscriptions.
	 */
	private Collection getSubscriptionRoutedTopics(EventMessage event)
	    throws EventProcessingException
	{
	    //
	    //  Route the event based on subscriber subscriptions.
	    //
	    Collection subscriberTopics = null;
	    try
	    {
	        subscriberTopics = mEventRouter.getInterestedSubscriberTopic(event);
	    }
	    catch (Exception e)
	    {
	        String msg = "Failed to route event: " + e.getMessage();
	        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
	        eventFailed(event, msg, 0);
	        subscriberTopics = new LinkedList();
	    }
	    
	    return subscriberTopics;
	}
    
    private void initializeSourceRouteQueue() throws EventProcessingException
    {
        mSourceRouteQueue =
            new EventObjectOutputQueue(
                SOURCE_ROUTE,
                JMSNames.SOURCE_ROUTE_Q,
                JMSNames.Q_CON_FACTORY);
        mSourceRouteQueue.initialize();
        mSourceRouteQueueList = new LinkedList();
        mSourceRouteQueueList.add(mSourceRouteQueue);
    }
    
    private static synchronized void initializeMetrics() throws JDBException
    {
        if (sMetricsManager == null)
        {
            sMetricsManager = new MetricsManager();
            sMetricsManager.initialize();
            sMetricsDBAccess = EventDBUtil.getDefaultDBAccess();
			sMetricsDBAccess.setProperty(JDBPropertyName.AUTO_COMMIT, false);
			sMetricsDBAccess.setProperty(JDBPropertyName.RESULT_SET_UPDATABLE, true);
        }
    }

    private static synchronized void incrementMetrics(EventMessage event, OutputQueue queue)
        throws EventProcessingException
    {
        String metricName =
            queue.getSubscriberUsername() + MetricsManager.SUBSCRIBER_ROUTED_METRIC_TAG;
			try {
        sMetricsDBAccess.getTransaction().begin();
        sMetricsManager.incrementMetricCount(sMetricsDBAccess, metricName, 1);
		sMetricsDBAccess.getTransaction().commit();
		} catch (JDBException e2) {
                     sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                      "Failed to increment metric count to database: " +
                                      e2.getMessage(), null, null, e2);
                     try {
                         sMetricsDBAccess.getTransaction().rollback();
                     } catch (JDBException e3) {
                         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                          "Failed to rollback database transaction: " +
                                          e3.getMessage(), null, null, e3);
                     }
                 }
    }
//CHANGED
	private static synchronized void incrementMetrics(EventMessage event, OutputTopic topic)
		throws EventProcessingException
	{
		String metricName =
			topic.getSubscriberUsername() + MetricsManager.SUBSCRIBER_ROUTED_METRIC_TAG;
					try {
        sMetricsDBAccess.getTransaction().begin();
        sMetricsManager.incrementMetricCount(sMetricsDBAccess, metricName, 1);
		sMetricsDBAccess.getTransaction().commit();
		} catch (JDBException e2) {
                     sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                      "Failed to increment metric count to database: " +
                                      e2.getMessage(), null, null, e2);
                     try {
                         sMetricsDBAccess.getTransaction().rollback();
                     } catch (JDBException e3) {
                         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                                          "Failed to rollback database transaction: " +
                                          e3.getMessage(), null, null, e3);
                     }
                 }
	}
    
    private StringBuffer appendEventKey(StringBuffer buffer, CommittedEventKey eventKey)
    {
        buffer.append(System.getProperty("line.separator"));
        buffer.append("   eventid=").append(eventKey.getEventID());
        buffer.append(System.getProperty("line.separator"));
        buffer.append("  category=").append(eventKey.getCategory());
        buffer.append(System.getProperty("line.separator"));
        buffer.append("  type=").append(eventKey.getType());
        buffer.append(System.getProperty("line.separator"));
        buffer.append("  environment=").append(eventKey.getOriginatingEnvironment());
        buffer.append(System.getProperty("line.separator"));
        buffer.append("  userSession=").append(eventKey.getOriginatingUserSession());
        buffer.append(System.getProperty("line.separator"));
        buffer.append("  seqNum=").append(eventKey.getSequenceNumber());
        
        return buffer;
    }
}
