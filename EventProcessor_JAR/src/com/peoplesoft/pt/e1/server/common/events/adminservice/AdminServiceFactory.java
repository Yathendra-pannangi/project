//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.adminservice;

//=================================================
// Imports from java namespace
//=================================================
import java.rmi.RemoteException;

//=================================================
// Imports from javax namespace
//=================================================
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;
import com.peoplesoft.pt.e1.server.common.events.adminservice.internal.AdminService;
import com.peoplesoft.pt.e1.server.common.events.adminservice.internal.AdminServiceHome;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Create instances of IAdminService.  User that need to use the event
 *  administration service should use this builder to create an instance
 *  of IAdminService.
 */
public final class AdminServiceFactory
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static final String HOME_JNDI_NAME =
        "ejb/com/peoplesoft/pt/e1/server/enterprise/events/ejb/AdminServiceHome";
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(AdminServiceFactory.class.getName());
    
    //=================================================
    // Public static final fields.
    //=================================================


    //=================================================
    // Instance member fields.
    //=================================================


    //=================================================
    // Constructors.
    //=================================================
    
    private AdminServiceFactory()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get an instance of IAdminService.
     * 
     *  @return  Reference to an <code>IAdminService</code>.
     * 
     *  @throws EventProcessingException  Error while creating instance.
     */
    public static IAdminService getAdminService() throws EventProcessingException
    {
        AdminService ejb = getAdminServiceRemoteEJB();
        return new AdminServiceImpl(ejb);
    }
    
    /**
     *  Get the EJB Home interface.
     *  
     *  <p>Don't cache the EJB home interface.  If the EJB server goes down and then
     *  comes back up we will need a new EJB home interface.
     */
    private static synchronized AdminServiceHome getHome() throws EventProcessingException
    {
        AdminServiceHome home = null;
        try
        {
            Object homeObj = JNDIUtil.lookup(HOME_JNDI_NAME);
            home = (AdminServiceHome)PortableRemoteObject.narrow(homeObj, AdminServiceHome.class);
        }
        catch (NamingException e)
        {
            String msg = "Failed to get AdminService home interface: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        return home;
    }
    
    /**
     *  Get an instance of the admin service's EJB remote interface.
     * 
     *  @return  instance of AdminService EJB remote interface.
     * 
     *  @throws EventProcessingException  an error occured while getting the EJB
     *                                    remote interface.
     */
    static AdminService getAdminServiceRemoteEJB() throws EventProcessingException
    {
        AdminServiceHome home = getHome();
        AdminService ejb = null;
        try
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Creating AdminService EJB remote interface", null, null, null);
            }
            ejb = home.create();
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"AdminService EJB remote interface creation successful", null, null, null);
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to get AdminService EJB: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        catch (CreateException e)
        {
            String msg = "Failed to get AdminService EJB: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
        return ejb;
    }
}
