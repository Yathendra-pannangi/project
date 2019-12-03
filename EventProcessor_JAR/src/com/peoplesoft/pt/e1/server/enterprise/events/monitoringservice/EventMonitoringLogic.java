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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
//=================================================
// Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.management.ObjectName;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
import com.jdedwards.mgmt.agent.Server;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.EventMetrics;
import com.peoplesoft.pt.e1.server.common.events.monitoring.SubscriberMonitoringInfo;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberDeliveryTransport;
import com.peoplesoft.pt.e1.server.enterprise.events.common.DeliveryTransportType;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSQueue;
import com.peoplesoft.pt.e1.server.enterprise.events.common.MetricsManager;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This class implements the logic to return generated events information.
 */
public class EventMonitoringLogic
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static String sSECTION = "EVENTS";
    private static String sQueueStorage = "queueMessageStorageFile";
    private static String sQueueProperties = "queueMessageStorageProperties";
    private static String sQueuePropertiesFile = JdeProperty.getProperty(
                                                 sSECTION, sQueueProperties, null);
    private static String sQueueStorageFile = JdeProperty.getProperty(
                                              sSECTION, sQueueStorage, null);
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventMonitoringLogic.class.getName());
    private SubscriberCache mCache = SubscriberCache.getInstance();

    //=================================================
    // Instance member fields.
    //=================================================

    private JDBDatabaseAccess mDatabaseAccess = null;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * This method does the initialization.
     */
    public void initialize()
    {

    }

    /**
     * This method returns the active subcribers list.
     * @return List
     * @throws EventProcessingException ex
     */
    public LinkedList getAllSubscribers() throws EventProcessingException
    {
        LinkedList subInfoList = new LinkedList();
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing getAllSubscribers", null, null, null);
        }
        //
        //  Iterate through the subscribers and initialize an output queue for each.
        //
        try
        {
            /**
            *  Iterate through the Management Console subscriber MBeans and unregister so they can be refreshed from the subscriber cache.
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
            
            Collection subscribers = mCache.getAllSubscribers();
            for(Iterator iter = subscribers.iterator(); iter.hasNext();)
            {
                SubscriberMonitoringInfo subInfo = new SubscriberMonitoringInfo();
                Subscriber sub = (Subscriber)iter.next();
                subInfo.setDescription(sub.getDescription());
                subInfo.setUsername(sub.getUsername());
                subInfo.setIsActive(sub.isActive());
                subInfoList.add(subInfo);
                
                /**After creating the SubscriberMonitoringInfo object register it with the Manangement Console.  Leaving
                 * this function's original return of LinkedList for other implementations, but the Management Console
                 * knows nothing of the SubscriberMonitoringInfo class and therefore can't 
                 * receive it as a return type in LinkedList
                 **/
                subInfo.registerObject(sub.getUsername());
            }
        }
        catch (EventProcessingException ex)
        {
            String msg = "Exception getting list of suscribers: " + ex.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
            throw new EventProcessingException(msg, ex);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"getAllSubscribers returned " + subInfoList.size()+ " subscribers.", null, null, null);
        }
        return subInfoList;
    }

    /**
     * This returns the number of messages in each queue.
     * @return Number of messages.
     * @param subscriberID String
     * @throws EventProcessingException ex
     */
    public long getNumberOfMessagesInQueue(String subscriberID) throws EventProcessingException
    {
        return getMessageCount(mCache.getSubscriber(subscriberID));
    }

    /**
     * This method returns the number of messages routed to a subscriber.
     * @param  subscriberID string
     * @return Number of messages routed to subscriber
     * @throws EventProcessingException ex
     */
    public long getNumberOfRoutedMessages(String subscriberID) throws EventProcessingException
    {
        MetricsManager manager = new MetricsManager();
        manager.initialize();
        try
        {
            mDatabaseAccess = EventDBUtil.getDefaultDBAccess();
        }
        catch(JDBException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"getNumberOfRoutedMessages Failed. " + ex,  null, null,ex);
        }
        long count = manager.getCurrentMetricCount(mDatabaseAccess,
                                                       subscriberID+".routed"+".events");
        return count;
    }

    /**
     * This method removes all messages from a subscriber's queue.
     * @param  subscriberID string
     * @throws EventProcessingException ex
     */
    public void purgeSubscriberQueue(String subscriberID) throws EventProcessingException
    {
        String queueJNDIName = null;
        String connectionFactoryJNDIName = null;
        Subscriber subscriber = mCache.getSubscriber(subscriberID);
        SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
        JMSQueue jmsQueue = null;
        

        if (transport.getTransportType().equals(DeliveryTransportType.MQSERIES_QUEUE))
        {
            //
            //  Get connection factory JNDI names from the transport
            //  properties.
            //
             String propName =
                F90711_EventTransportParameter.MQSERIES_CON_FACTORY_NAME;
            connectionFactoryJNDIName = transport.getProperty(propName);
        }

        // Get queue JNDI Name
            queueJNDIName = subscriber.getQueueDescription().getQueueJNDIName();

        jmsQueue = new JMSQueue(queueJNDIName, connectionFactoryJNDIName, false);
        try
        {
            jmsQueue.initialize();
            QueueReceiver queueReceiver = jmsQueue.getQueueReceiver();
            while(true)
            {
                Message message = queueReceiver.receiveNoWait();
                if(message == null)
                {
                    break;
                }
            }
            jmsQueue.shutdown();
        }
        catch(EventProcessingException e)
        {
            String msg = "Error in getMessageCount. " +  e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null,null);
            throw new EventProcessingException(msg, e);
        }
        catch(JMSException e)
        {
            String msg = "Error in getMessageCount. " +  e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null,e);
            throw new EventProcessingException(msg, e);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Finished getMessageCount.", null, null, null);
        }
    }

    /**
     * Returns the number of messages in a subcriber queue.
     * @return HashMap
     * @throws EventProcessingException ex
     */
    private long getMessageCount(Subscriber subscriber) throws EventProcessingException
    {
        long numMsgs = 0;
        String queueJNDIName = null;
        String connectionFactoryJNDIName = null;
        SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
        JMSQueue jmsQueue = null;
        if (transport.getTransportType().equals(DeliveryTransportType.MQSERIES_QUEUE))
        {
            //
            //  Get the factory JNDI names from the transport
            //  properties.
        
            String propName =
                F90711_EventTransportParameter.MQSERIES_CON_FACTORY_NAME;
            connectionFactoryJNDIName = transport.getProperty(propName);
        }
        
        // Get queue JNDI Name
        queueJNDIName = subscriber.getQueueDescription().getQueueJNDIName();

        jmsQueue = new JMSQueue(queueJNDIName, connectionFactoryJNDIName, false);
        try
        {
            jmsQueue.initialize();
            QueueBrowser queueBrowser = jmsQueue.getQueueBrowser();
            // count number of messages
            for (Enumeration e = queueBrowser.getEnumeration() ; e.hasMoreElements() ;)
            {
                e.nextElement();
                numMsgs++;
            }
            jmsQueue.shutdown();
        }
        catch(EventProcessingException e)
        {
            String msg = "Error in getMessageCount. " +  e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null,e);
            throw new EventProcessingException(msg, e);
        }
        catch(JMSException e)
        {
            String msg = "Error in getMessageCount. " +  e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventProcessingException(msg, e);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Finished getMessageCount.", null, null, null);
        }
        return numMsgs;
    }

    /**
     * This returns the remaining queue storage space on a file system in KB.
     * @return long Storage Space
     * @throws EventProcessingException ex
     */
    public long getStorageSpaceRemaining() throws EventProcessingException
    {
        long storageSpaceRemaining = 0;
        if(sQueuePropertiesFile == null)
        {
            String msg = "Setting " + sQueuePropertiesFile + " not found in JAS.INI";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
            throw new EventProcessingException(msg);
        }
        if(sQueueStorageFile == null)
        {
            String msg = "Check setting " + sQueueStorageFile + " in JAS.INI";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
            throw new EventProcessingException(msg);
        }
        Properties props = getProperties(sQueuePropertiesFile);
        if(props != null)
        {
            String maxSize = props.getProperty("LogFileSize");
            long size = Integer.parseInt(maxSize);
            size = (size * 1024);//convert bytes to KB
            File file = new File(sQueueStorageFile);
            long currentSize = file.length();
            currentSize = (currentSize/1024);//convert bytes to KB
            storageSpaceRemaining = size - currentSize;
        }
        else
        {
            String msg = "QueuePropertiesFile " + sQueuePropertiesFile
                         + " does not have LogFileSize defined.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null,null);
        }
        return storageSpaceRemaining;
    }

   /**
    * This returns the current queue storage space on a file system in KB.
    * @return long Storage Space
    * @throws EventProcessingException ex
    */
    public long getCurrentStorageSpace() throws EventProcessingException
    {
        long currentSize = 0;
        if(sQueueStorageFile == null)
        {
            String msg = "Check setting " + sQueueStorageFile + " in JAS.INI";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
             throw new EventProcessingException(msg);
        }
        File file = new File(sQueueStorageFile);
        if(file != null)
        {
            currentSize = file.length();
            currentSize = (currentSize/1024);//convert bytes to KB
        }
        else
        {
            String msg = "File " + sQueueStorageFile + " not found.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,null);
        }
        return currentSize;
    }

    /** This function loads all the properties from the file and creates a
     *  valid properties object.
     *  @param String file to be loaded.
     *  @return Properties object with all properties loaded.
     */
    private static Properties getProperties(String queueFile)
    {
        Properties props = new Properties();
        try
        {
            File file = new File(queueFile);
            InputStream inputStream = new FileInputStream(file);
            if (inputStream != null)
            {
                props.load(inputStream);
            }
            else
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR," Properties file: " + queueFile + " not found in the classpath.", null, null,null);
            }
        }
        catch (IOException ioex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Properties file " + queueFile + " loading failed. Loading error."
                                 + ioex.getMessage(), null, null,ioex);
        }
        return props;
    }

    /**
     * This method returns the failed events information.
     * @return EventMessage
     * @throws EventProcessingException ex
     */
    public ArrayList getFailedEventsInfo() throws EventProcessingException
    {
        ArrayList list = null;
        try
        {
            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing getFailedEventsInfo", null, null, null);
            }
            mDatabaseAccess = EventDBUtil.getDefaultDBAccess();
            FailedEventDBLookup lookup = new FailedEventDBLookup();
            try
            {
                list = lookup.getFailedEvents(mDatabaseAccess);
            }
            catch (EventProcessingException ex)
            {
                String msg = "Exception retrieving failed events data. " + ex.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
                throw new EventProcessingException(msg, ex);
            }
        }
        catch(JDBException ex)
        {
            String msg = "Exception connecting to Failed Events Table F90710. "  + ex.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
            throw new EventProcessingException(ex);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Returning from getFailedEventsInfo", null, null, null);
        }
        return list;
    }

    /**
     * This deletes all of the failed events.
     * @throws EventProcessingException ex
     */
    public void deleteAllFailedEvents() throws EventProcessingException
    {
        long numOfDeletedRecords = 0;
        try
        {
            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing deleteAllFailedEvents", null, null, null);
            }
            mDatabaseAccess = EventDBUtil.getDefaultDBAccess();
            FailedEventDBLookup lookup = new FailedEventDBLookup();
            try
            {
                numOfDeletedRecords =  lookup.deleteFailedEvents(mDatabaseAccess);
            }
            catch (EventProcessingException ex)
            {
                String msg = "Exception deleteing failed events data. " + ex.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
                throw new EventProcessingException(msg, ex);
            }
        }
        catch(JDBException ex)
        {
            String msg = "Exception connecting to Failed Events Table F90710. " + ex.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null, ex);
            throw new EventProcessingException(ex);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Finish deleteAllFailedEvents. Deleted "
                            + numOfDeletedRecords + " records.", null, null, null);
        }
        return;
    }

    /**
     * This method deletes a specific failed event.
     * @param eventID  The event to delete.
     * @throws EventProcessingException ex
     */
    public void deleteFailedEvent(String eventID) throws EventProcessingException
    {
        try
        {
            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing deleteFailedEvents" ,null, null,null);
            }
            mDatabaseAccess = EventDBUtil.getDefaultDBAccess();
            FailedEventDBLookup lookup = new FailedEventDBLookup();
            try
            {
                lookup.deleteFailedEvents(mDatabaseAccess, eventID, false);
            }
            catch (EventProcessingException ex)
            {
                String msg = "Exception deleteing failed events data. " + ex.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
                throw new EventProcessingException(msg, ex);
            }
        }
        catch(JDBException ex)
        {
            String msg = "Exception connecting to Failed Events Table F90710. " + ex.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
            throw new EventProcessingException(ex);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Finish deleteFailedEvents.", null, null, null);
        }
        return;
    }

    /**
     * This method returns the events metrics information.  
     * @return EventMessage
     * @throws EventProcessingException ex
     */
    public HashMap getEventMetrics() throws EventProcessingException
    {
        HashMap map = new HashMap();
        MetricsManager manager = new MetricsManager();
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing getEventMetrics", null, null, null);
        }
        try
        {
            /**After creating the EventMetrics object register it with the Manangement Console.  Leaving
             * this function's original return of HashMap for other implementations, but the Management Console
             * knows nothing of the EventMetrics class and therefore can't receive it as a return type in HashMap
             **/
             
            mDatabaseAccess = EventDBUtil.getDefaultDBAccess();
            manager.initialize();
            long countRT = manager.getCurrentMetricCount(mDatabaseAccess,
                                                    EventMetrics.NUM_COMMITTED_RT_EVENTS);
            EventMetrics metricsRT = new EventMetrics();
            metricsRT.setMatricName(EventMetrics.NUM_COMMITTED_RT_EVENTS);
            metricsRT.setMatricCount(countRT);
            map.put(EventMetrics.NUM_COMMITTED_RT_EVENTS,metricsRT);
            metricsRT.registerObject(EventMetrics.NUM_COMMITTED_RT_EVENTS);

            long countWF = manager.getCurrentMetricCount(mDatabaseAccess,
                                                    EventMetrics.NUM_COMMITTED_WORKFLOW_EVENTS);
            EventMetrics metricsWF = new EventMetrics();
            metricsWF.setMatricName(EventMetrics.NUM_COMMITTED_WORKFLOW_EVENTS);
            metricsWF.setMatricCount(countWF);
            map.put(EventMetrics.NUM_COMMITTED_WORKFLOW_EVENTS,metricsWF);
            metricsWF.registerObject(EventMetrics.NUM_COMMITTED_WORKFLOW_EVENTS);

            long countXAPI = manager.getCurrentMetricCount(mDatabaseAccess,
                                                    EventMetrics.NUM_COMMITTED_XAPI_EVENTS);
            EventMetrics metricsXAPI = new EventMetrics();
            metricsXAPI.setMatricName(EventMetrics.NUM_COMMITTED_XAPI_EVENTS);
            metricsXAPI.setMatricCount(countXAPI);
            map.put(EventMetrics.NUM_COMMITTED_XAPI_EVENTS,metricsXAPI);
            metricsXAPI.registerObject(EventMetrics.NUM_COMMITTED_XAPI_EVENTS);

            long countZ = manager.getCurrentMetricCount(mDatabaseAccess,
                                                    EventMetrics.NUM_COMMITTED_Z_EVENTS);
            EventMetrics metricsZ = new EventMetrics();
            metricsZ.setMatricName(EventMetrics.NUM_COMMITTED_Z_EVENTS);
            metricsZ.setMatricCount(countZ);
            map.put(EventMetrics.NUM_COMMITTED_Z_EVENTS,metricsZ);
            metricsZ.registerObject(EventMetrics.NUM_COMMITTED_Z_EVENTS);
        }
        catch(JDBException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error in getEventMetrics. " + ex, null, null, ex);
        }
        if(sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Returning from getEventMetrics", null, null, null);
        }
        return map;
    }

    /**
     * This does the clean up of environment.
     */
    public void shutDown()
    {

    }
}
