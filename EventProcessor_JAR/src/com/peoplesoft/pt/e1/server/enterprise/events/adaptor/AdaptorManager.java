 //============================================================================
 //
 // Copyright © [2004] 
 // PeopleSoft, Inc.  
 // All rights reserved. PeopleSoft Proprietary and Confidential.
 // PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
 //
 //============================================================================

 package com.peoplesoft.pt.e1.server.enterprise.events.adaptor;

 //=================================================
 //Imports from java namespace
 //=================================================
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;

 //=================================================
 //Imports from javax namespace
 //=================================================

 //=================================================
 //Imports from com namespace
 //=================================================
 import com.jdedwards.base.logging.E1Logger;
 import com.jdedwards.base.logging.JdeLog;
 import com.jdedwards.base.logging.log4j.LogUtils;
 import com.jdedwards.system.lib.JdeProperty;
 import com.peoplesoft.pt.e1.common.events.EventProcessingException;
 import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscriber;
 import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberCache;
 import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscriberDeliveryTransport;
 import com.peoplesoft.pt.e1.server.enterprise.events.common.DeliveryTransportType;
 import com.peoplesoft.pt.e1.server.enterprise.events.util.IJVMLifecycleListener;

 //=================================================
 //Imports from org namespace
 //=================================================

 //=================================================
 //Imports from junit namespace
 //=================================================

 /**
  * This class manages the JDENetAdaptor and SourceRouterAdaptor
  * resources and connections.
  */
 public class AdaptorManager extends Thread implements IJVMLifecycleListener
 {
     //=================================================
     // Non-public static class fields.
     //=================================================
     
     private static E1Logger sE1Logger = 
            JdeLog.getE1Logger(AdaptorManager.class.getName());
     
     private static Object sReloadLock = new Object();
     
     /**  Indicates if the thread should keep running.  */
     private static boolean sRun = true; 
     
     /**  Indicates that a reload check needs to be done.  */
     private static boolean sReload = false;
     
     private static JDENetSourceRouter sRouter = new JDENetSourceRouter();
     
     /**  How long to wait on a thread join before timeing out (milliseconds). */
     private static final long JOIN_TIMEOUT = 500;
     
     /**  Interval for checking the status of the source routing thread (milliseconds). */
     private static final long MONITOR_INTERVAL = 300000;
     
     /**  Interval for retrying to load the subscriber cache (milliseconds). */
     private static final int CACHE_RETRY_INTERVAL = JdeProperty.getProperty("EVENTS",
                                                     "cacheRetryInterval", 60000);
                                                   
     /** 
      * Indicates if there are any subscribers with the transport type JDENET.
      */
     private static boolean subscriberJDENET = false;                                                    
     
     //=================================================
     // Public static final fields.
     //=================================================
     
     //=================================================
     // Instance member fields.
     //=================================================
     
     //=================================================
     // Constructors.
     //=================================================

     //=================================================
     // Methods.
     //=================================================
     
     /**
      *  Called during JVM start-up.
      */
     public void startup()
     { 
         if(sE1Logger.isDebug())
         {
             sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Starting AdaptorManager", null, null, null);
         }
         //start the source router queue.
         sRouter.setDaemon(true);
         sRouter.start();
         this.start(); 
     }
     
     /**
      *  This method starts the JDENetTransportAdaptor. Creates 
      *  a seperate new adaptor for each active subcriber having
      *  JDENET as a transport type.
      */
     public void run()
     {   
         if(sE1Logger.isDebug())
         {
             sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "AdaptorManager Started.",null, null, null);
         }
         
         //
         //  Container to keep track of all the JDENET adaptor threads that are running.
         //
         HashMap threadMap = new HashMap();
         
         //
         //  Go into the main loop where JDENET threads are started and then modified when
         //  a change occurs.
         //
         while (getRun())
         {
             Collection subscribers = null;
             
             if (!isSubscriberJDENET())
             {
                 try
                 {
                     //
                     //  Get the currect collection of active subscribers.
                     //
                     subscribers = SubscriberCache.getInstance().getAllSubscribers();
                 }
                 catch (EventProcessingException e1)
                 {
                     sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error loading subscriber cache with all"
                                   + " subscribers inside AdaptorManager."
                                   + "  Waiting for " + (CACHE_RETRY_INTERVAL/1000) + " seconds "
                                   + " before trying again.", null, null, e1);
                     try
                     {
                         Thread.sleep(CACHE_RETRY_INTERVAL);
                     }
                     catch (InterruptedException e)
                     {
                         // Retry the outer while loop again, as the rest of the logic in this loop
                         // will not work until the subscriber cache is loaded.
                         continue;
                     }
                 }
       
                 try
                 {
                     //
                     //  Loop through all subscribers and check if jdenet transport is
                     //  used and then log the XAPI Phase II message with any jdenet subscribers.
                     // 
                     for (Iterator iter = subscribers.iterator(); iter.hasNext();)
                     {
                         Subscriber subscriber = (Subscriber)iter.next();
                         SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
                         if ((transport != null)
                             && (transport.getTransportType().equals(DeliveryTransportType.JDENET)))
                         {
                             sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"TOOLS Release 8.97 and later, Transaction " +
                                 "Server no longer supports XAPI Phase II.  Please use Business " +
                                 "Service Server for this functionality.", null, null, null);
                             setSubscriberJDENET(true);
                             
                             break;
                         }
                     }
                 }
                 catch (Exception ex)
                 {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error introspecting on" +
                        " subscriber transport type in JDENetTransport Adaptor " + ex.getMessage(),null, null, ex);
                 }
             }
             
             // Load the subscriber cache.  This code is in its own section, so that if the cache
             // cannot be loaded a specified wait interval should elapse before attempting to load
             // the cache again.  It was found that when this code was in a try/catch block with
             // all the other code and if the Security Server went down, that the AdaptorManager
             // thread looped quickly and constantly, filling up log files.
             try
             {
                 //
                 //  Get the currect collection of active subscribers.
                 //
                 subscribers = SubscriberCache.getInstance().getActiveSubscribers();
             }
             catch (EventProcessingException e1)
             {
                 sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error loading subscriber cache inside AdaptorManager."
                               + "  Waiting for " + (CACHE_RETRY_INTERVAL/1000) + " seconds "
                               + " before trying again.", null, null, e1);
                 try
                 {
                     Thread.sleep(CACHE_RETRY_INTERVAL);
                 }
                 catch (InterruptedException e)
                 {
                     // Retry the outer while loop again, as the rest of the logic in this loop
                     // will not work until the subscriber cache is loaded.
                     continue;
                 }
             }

             try
             {
                 //
                 //  Loop through the active subscribers and check if jdenet transport is
                 //  already started or not.  If started, store the thread reference in a temp 
                 //  collection.  If not started, start the new one and add it temp collection.
                 //  When we're done the temp collection will contain the remaining an new
                 //  active threads.
                 // 
                 HashMap tempMap = new HashMap();
                 for (Iterator iter = subscribers.iterator(); iter.hasNext();)
                 {
                     Subscriber subscriber = (Subscriber)iter.next();
                     String userName = subscriber.getUsername();
                     SubscriberDeliveryTransport transport = subscriber.getDeliveryTransport();
                     JDENetTransport netTransport = (JDENetTransport)threadMap.remove(userName);
                     if (netTransport != null)
                     {
                         //
                         //  Subscriber currently has an active thread.  Make sure the
                         //  subscriber is still configured to use JDENET as its transport.
                         //
                         if ((transport != null) 
                             && (transport.getTransportType().equals(DeliveryTransportType.JDENET)))
                         {
                             //
                             //  The subscriber currently has a thread.  Make sure the
                             //  thread is still running.
                             //
                             if (netTransport.isAlive())
                             {
                                 //
                                 //  The thread is still running, keep using it.
                                 //
                                 tempMap.put(userName, netTransport);
                             }
                             else
                             {
                                 //
                                 //  The thread has stoped for some reason, restart it.
                                 //
                                 netTransport = new JDENetTransport(subscriber);
                                 tempMap.put(userName, netTransport);
                                 netTransport.setDaemon(true);
                                 netTransport.start();
                                 if(sE1Logger.isDebug())
                                 sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Restarted JDENET adaptor thread for subscriber: "
                                              + userName, null, null, null);
                             }
                         }
                         else
                         {
                             //
                             //  Subscriber is still active but the transport has changed.
                             //  Stop the subscriber's JDENET thread.
                             //
                             stopTreansportThread(netTransport);
                         }
                     }
                     else
                     {
                         //
                         //  The subscriber does not currently have an active thread.  See if
                         //  the subscriber is using JDENET as the transport and start a
                         //  thread if it is.
                         //
                         if ((transport != null)
                             && (transport.getTransportType().equals(DeliveryTransportType.JDENET)))
                         {
                             JDENetTransport addNetTransport = new JDENetTransport(subscriber);
                             tempMap.put(userName, addNetTransport);
                             addNetTransport.setDaemon(true);
                             addNetTransport.start();
                         }
                     }
                 }
                 
                 //
                 //  Stop all the remaining threads, these subcribers are no longer active.
                 //
                 for (Iterator iter = threadMap.values().iterator();iter.hasNext();)
                 {
                     stopTreansportThread((JDENetTransport)iter.next());
                 }
                 
                 //
                 //  Clear the collection with the inactive thread references.
                 //
                 threadMap.clear();
                 
                 //
                 //  Copy all active thread references back to the thread collection.
                 //
                 threadMap.putAll(tempMap);
                 
                 //
                 //  Remove all the data from the temp collection.
                 //
                 tempMap.clear();
                 
                 //
                 //  Wait for the next subscriber cache reload.
                 //
                 waitForReload();
             }
             catch (Exception ex)
             {
                 sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error starting JDENetTransport Adaptor " + ex.getMessage(),null, null, ex);
             }
         }
         
         //
         //  Shutdown all the threads.
         //
         for (Iterator iter = threadMap.values().iterator(); iter.hasNext();)
         {
             stopTreansportThread((JDENetTransport)iter.next());
         }
         
         //
         //  Stop the source router adapter.
         //
         sRouter.stopJdenetSourceRouter();
     }

     /**
      * Waits untill notified by Subscriber cache reload.
      */
     private void waitForReload()
     {
         //
         //  Loop waiting for a reload.
         //
         while (!getReload())
         {
             //
             //  Wait until notified or the monitor interval has expired.
             //
             try
             {
                 synchronized (sReloadLock)
                 {
                     sReloadLock.wait(MONITOR_INTERVAL);
                 }
             }
             catch (InterruptedException ex)
             {
                 sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Adaptor manager thread interrupted." + ex,null, null, ex);
             }
             
             //
             //  Make sure the source routing thread is still running.
             //
             if (!sRouter.isAlive())
             {
                 sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, "SourceRouter not running, restarting", null, null, null);
                 sRouter = new JDENetSourceRouter();
                 sRouter.setDaemon(true);
                 sRouter.start();
             }
         }
         setReload(false);
     }
     
     /**
      * Notifies the Adaopter manager to reload the cache.
      */      
     public static void notifyReload()
     {
         synchronized(sReloadLock)
         {
             setReload(true);
             sReloadLock.notify();
         } 
     }
     
     /**
      *  Called during JVM shutdown.
      */
     public void shutdown()
     {
         setRun(false);
         notifyReload();
     }
     
     private void stopTreansportThread(JDENetTransport transport)
     {
         transport.stopJdenetTransport();
         try
         {
             transport.join(JOIN_TIMEOUT);
         }
         catch (InterruptedException e)
         {
             //  Ignore.
         }
         if (transport.isAlive())
         {
             sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, "JDENET transport thread did not shutdown normally for subscriber: "
                 + transport.getSubscriber().getUsername(), null, null, null);
         }
     }
     
     /**
      *  These two methods for getting and setting run must be synchronized since
      *  the value of run is used for communication between threads.
      */
     private static synchronized boolean getRun()
     {
         return sRun;
     }
     
     private static synchronized void setRun(boolean run)
     {
         sRun = run;
     }
     
     private static synchronized boolean getReload()
     {
         return sReload;
     }
     
     private static synchronized void setReload(boolean reload)
     {
         sReload = reload;
     }

     /**
      * Indicate whether there are any subscribers with the transport type JDENET
      * @param subscriberJDENET true or false
      */
     private void setSubscriberJDENET(boolean subscriberJDENET)
     {
         this.subscriberJDENET = subscriberJDENET;
     }
   
     /**
      * Indicates whether there are any subscribers with the transport type JDENET
      * @return boolean type 
      */
     public boolean isSubscriberJDENET()
     {
         return subscriberJDENET;
     }
 }
