//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.monitoring;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;
import com.peoplesoft.pt.e1.server.common.events.monitoring.internal.EventMonitoring;
import com.peoplesoft.pt.e1.server.common.events.monitoring.internal.EventMonitoringHome;

//=================================================
//Imports from org namespace
//=================================================
/**
 * 
 * @author BB6639454
 *
 */
public final class EventMonitoringFactory
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static final String HOME_JNDI_NAME =
        "ejb/com/peoplesoft/pt/e1/server/enterprise/events/ejb/EventMonitoringHome";
        
    private static EventMonitoringHome sHome = null;
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventMonitoringFactory.class.getName());
    
    //=================================================
    // Public static final fields.
    //=================================================


    //=================================================
    // Instance member fields.
    //=================================================


    //=================================================
    // Constructors.
    //=================================================
    private EventMonitoringFactory()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get an instance of IEventMonitoring.
     *  @return  Reference to an <code>IAdminService</code>.
     *  @throws EventMonitoringException  Error while creating instance.
     */
    public static IEventMonitoring getMonitoringService() throws EventMonitoringException
    {
        if(System.getProperty("MONITORINGDEBUG") == null
                        || System.getProperty("MONITORINGDEBUG").equalsIgnoreCase("false"))
        {
            EventMonitoringHome home = getHome();
            EventMonitoring ejb = null;
            try
            {
                ejb = home.create();
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get EventMonitoring EJB: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
                throw new EventMonitoringException(msg, e);
            }
            catch (CreateException e)
            {
                String msg = "Failed to get EventMonitoring EJB: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
                throw new EventMonitoringException(msg, e);
            } 
            return new EventMonitoringImpl(ejb);
        }
        else
        {
            return new EventMonitoringDebugImpl();
        }       
    }
    
    
    private static synchronized EventMonitoringHome getHome() throws EventMonitoringException
    {
        if (sHome == null)
        {
            try
            {
                Object homeObj = JNDIUtil.lookup(HOME_JNDI_NAME);
                sHome = (EventMonitoringHome)
                            PortableRemoteObject.narrow(homeObj, 
                                    EventMonitoringHome.class);
            }
            catch (NamingException e)
            {
                String msg = "Failed to get EventMonitoring home interface: " + e.getMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
                throw new EventMonitoringException(msg, e);
            }
        }

        return sHome;
    }
}
