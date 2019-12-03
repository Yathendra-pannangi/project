//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.adminservice;

//=================================================
//Imports from java namespace
//=================================================
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.adminservice.internal.AdminService;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Implementation of the admin service EJB client.
 */
class AdminServiceImpl implements IAdminService
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(AdminServiceImpl.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private AdminService mAdminServiceEJB = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param  ejb  Remote interface to the admin service EJB.
     */
    AdminServiceImpl(AdminService ejb)
    {
        if (ejb == null)
        {
            throw new NullPointerException();
        }
        mAdminServiceEJB = ejb;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  {@inheritDoc}
     */
    public void reloadData(String category)
        throws UnknownDataReloadCategoryException, 
               EventProcessingException
    {
        //
        //  Make sure we have an EJB to call.  Its possible for the EJB to be null if
        //  a previous call failed because the EJB server is down.
        //
        if (mAdminServiceEJB == null)
        {
            mAdminServiceEJB = AdminServiceFactory.getAdminServiceRemoteEJB();
        }
        
        //
        //  Try calling the EJB.  Try twice since on the first call we migth fail if
        //  the EJB server has gone down since the EJB reference was obtained.  If we
        //  get a NoSuchObjectException then try to get a new EJB and try again in case
        //  the server has come back up.
        //
        for (int i = 0; i < 2; i++)
        {
            try
            {
                if (sE1Logger.isDebug())
                {
			sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Calling EJB to reload of data category: " +category, null, null, null);
                }
                mAdminServiceEJB.reloadData(category);
                if (sE1Logger.isDebug())
                {
			sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "EJB call successful", null, null, null);
                }
                break;
            }
            catch (UnknownDataReloadCategoryException e)
            {
		sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Invalid data reload category: " + e.getMessage(), null, null, e);
                throw e;
            }
            catch (NoSuchObjectException e)
            {
		sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "AdminService EJB service is stale, getting new connection", null, null, e);
                mAdminServiceEJB = null;
                mAdminServiceEJB = AdminServiceFactory.getAdminServiceRemoteEJB();
            }
            catch (RemoteException e)
            {
                mAdminServiceEJB = null;
                String msg = "Failed to reload data category " + category + ": " + e.getMessage();
				sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
    }
}
