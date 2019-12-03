//==========================================================================
//
//  Copyright © [2004]
//  PeopleSoft, Inc.
//  All rights reserved. PeopleSoft Proprietary and Confidential.
//  PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
//  of PeopleSoft, Inc.
//
//==========================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.transferagent;


//=================================================
//Imports from java namespace
//=================================================
import java.util.Iterator;
import java.util.TimerTask;

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import javax.naming.NamingException;

//import com.ibm.websphere.management.AdminService;
//import com.ibm.websphere.management.AdminServiceFactory;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;

import com.jdedwards.system.lib.JdeProperty;

import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;

import com.peoplesoft.pt.e1.server.enterprise.events.common.EventProcessingFatalException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.common.TriggerMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Set;


//=================================================
//Imports from org namespace
//=================================================

/**
 * Timer task for sending trigger messages on a timer.
 */
final class TriggerTimerTask extends TimerTask
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    private static E1Logger sE1Logger = JdeLog.getE1Logger(TriggerTimerTask.class.getName());
    private static TriggerTimerTask sInstance = null;

    /** DOCUMENT ME! */
    private static final String SECTION = "EVENTS";

    /** DOCUMENT ME! */
    private static final String EMBEDDED_MQ_USE = "useEmbeddedMQ";

    /** DOCUMENT ME! */
    private static final String WAS_MESSAGING_ENGINE_NAME = "WASMEName";

    private static boolean mInitialized = false;

    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Instance member fields.
    //=================================================
    private QueueSender mQueueSender;
    private QueueSession mQueueSession;
    private QueueConnection mQueueConnection;

    //=================================================
    // Constructors.
    //=================================================
    private TriggerTimerTask()
    {
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get the instance.
     *
     * @return An instance;
     */
    public static synchronized TriggerTimerTask getInstance()
    {
     //   checkIfSIBStarted();

        if (sInstance == null)
        {
            sInstance = new TriggerTimerTask();
         //   sInstance.initialize();
        }

        return sInstance;
    }

    /**
   * This methos checks if WebSphere 6 Messaging Engine has completed
   * startup. It waits until startup is complete.
   * Code should not execute for WAS5 and oracle app server.
   */
  public static void checkIfSIBStarted()
  {
    String messagingEngineName =
      JdeProperty.getProperty(SECTION, WAS_MESSAGING_ENGINE_NAME, null);
   // String embeddedMQUse =
   //   JdeProperty.getProperty(SECTION, EMBEDDED_MQ_USE, null);

    boolean isAppServerWAS6 = false;

 //   if ((embeddedMQUse != null) && embeddedMQUse.equalsIgnoreCase("true"))
 //   {
      if ((messagingEngineName != null) &&
          (!messagingEngineName.equals("")))
      {
        isAppServerWAS6 = true;
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                        "Running on WebSphere 7 application server. Will wait for messaging engine to start...",
                        null, null, null);
      }
  //  }

    if (isAppServerWAS6)
    {
      boolean meStarted = false;

      while (!meStarted)
      {     ///
          String[] engines = messagingEngineName.split(";");
          for (int i = 0; i < engines.length; i++) {
//        String filterString =
//          "WebSphere:type=SIBMessagingEngine,name=" + messagingEngineName +
//          ",*";
    String filterString =
            "WebSphere:type=SIBMessagingEngine,name=" + engines[i] +
            ",*";
        boolean foundBean = false;

       // AdminService adminService = null;
	    Object adminService = null;
        
        Object obj = null;
        ClassLoader loader = null;
        Class objectnameClass = null;

        try
        {
         // adminService = AdminServiceFactory.getAdminService();

          Class cls = Class.forName("com.ibm.websphere.management.AdminServiceFactory");
          Method method = cls.getMethod("getAdminService", new Class[0]);
          adminService = method.invoke(null, new Object[0]);
          
		  loader = adminService.getClass().getClassLoader();

          objectnameClass = 
              loader.loadClass("javax.management.ObjectName");

          Class[] argArr =
          { "String".getClass() };
          Object[] val =
          { filterString };

          Constructor ctor = objectnameClass.getConstructor(argArr);

          obj = ctor.newInstance(val);

          Class paramType[] = new Class[2];
          paramType[0] = objectnameClass;
          paramType[1] = loader.loadClass("javax.management.QueryExp");
          Method queryMeth = 
            adminService.getClass().getMethod("queryNames", paramType);
          Object argList[] =
          { obj, null };

          Set set = (Set) queryMeth.invoke(adminService, argList);

          Iterator iter = set.iterator();

          while (iter.hasNext())
          {
            obj = iter.next();
            foundBean = true;
            break;
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
          break;
        }

        if (foundBean)
        {
          // The ME MBean is initialized; Is it actually 'Started'?
          try
          {
            Class paramType[] = new Class[4];
            paramType[0] = objectnameClass;
            paramType[1] = "String".getClass();
            paramType[2] = (new Object[0]).getClass();
            paramType[3] = (new String[0]).getClass();

            Method invokeMeth = 
              adminService.getClass().getMethod("invoke", paramType);
              
            Object arglist[] = new Object[4];
            arglist[0] = obj;
            arglist[1] = "isStarted";
            arglist[2] = null;
            arglist[3] = null;
            
            Boolean retBool = (Boolean) invokeMeth.invoke(adminService, arglist);
            
            meStarted = retBool.booleanValue();

          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }

        if (!meStarted)
        {
          // Not started, better wait a bit...
          try
          {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                             "Waiting for WebSphere 7 Messaging Engine to start...",
                             null, null, null);
            Thread.sleep(5000);
          }
          catch (InterruptedException e1)
          {
            e1.printStackTrace();
          }
        }
      } ///
      }
    }
  }

    /**
     * This method is called when the time delay has passed to send a trigger message.
     */
    public void run()
    {
        sendTrigger();
    }

    /**
     * Send a trigger message.
     */
    void sendTrigger()
    {
        if(!mInitialized){
            initialize();
        }

        if(mInitialized){
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Trigger timer sending an event transfer trigger message", null, null, null);
        }

        try
        {
            ObjectMessage message = mQueueSession.createObjectMessage(new TriggerMessage());
            mQueueSender.send(message);
        }
        catch (JMSException ex)
        {
            String msg = "Failed to send event transfer trigger message: " + ex.getMessage();
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, ex);
            mInitialized = false;
        }
            }
    }

    /**
     * Initialize the timer task.
     *
     * @throws EventProcessingFatalException Error initializing.
     */
 //   private void initialize() throws EventProcessingFatalException
      private void initialize()
    {
        try
        {
            //
            //  Lookup the queue conneciton factory.
            //
            QueueConnectionFactory connectionFactory = (QueueConnectionFactory) JNDIUtil.lookup(JMSNames.Q_CON_FACTORY);

            //
            //  Lookup the queue.
            //
            Queue queue = (Queue) JNDIUtil.lookup(JMSNames.TRIGGER_Q);

            //
            //  Create the queue connection, session, and sender.
            //
            //  Note: the 'false' in createQueueSession() indicates that we're creating
            //        a non-transactional session.
            //
            mQueueConnection = connectionFactory.createQueueConnection();
            mQueueSession = mQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            mQueueSender = mQueueSession.createSender(queue);
            mInitialized = true;
        }
        catch (NamingException e)
        {
            //
            //  Clean-up.
            //
            shutdown();

            String msg = "Failed to initialize transfer agent timer task. " + e.getMessage();
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
        //    throw new EventProcessingFatalException(msg);
        }
        catch (JMSException e)
        {
            //
            //  Clean-up.
            //
            shutdown();

            String msg = "Failed to initialize transfer agent timer task. " + e.getMessage();
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
          //  throw new EventProcessingFatalException(msg);
        }
    }

    /**
     * Shutdown the timer task.
     */
    void shutdown()
    {
        try
        {
            if (mQueueSender != null)
            {
                mQueueSender.close();
            }

            if (mQueueSession != null)
            {
                mQueueSession.close();
            }

            if (mQueueConnection != null)
            {
                mQueueConnection.close();
            }
        }
        catch (JMSException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception while shutting down trigger timer task: " + e.getMessage(), null, null,e);
        }

        mQueueSender = null;
        mQueueSession = null;
        mQueueConnection = null;
        mInitialized = false;
    }
}
