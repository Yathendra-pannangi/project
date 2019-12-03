//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.connectorsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.connectorsvc.internal.ConnectorService;
import com.peoplesoft.pt.e1.common.events.connectorsvc.internal.ConnectorServiceHome;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Create instances of IConnectorService.  Users that need to use the event
 *  Connector Service should use this builder to create an instance
 *  of IConnectorService.
 */
public final class ConnectorServiceFactory
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String HOME_JNDI_NAME =
        "ejb/com/peoplesoft/pt/e1/server/enterprise/events/ejb/ConnectorServiceHome";
        
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(ConnectorServiceFactory.class.getName());
    
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     * Private constructor to prevent users from creating their
     * own instance.
     */
    private ConnectorServiceFactory()
    {
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Get an instance of IConnectorService.
     * 
     *  @return  Reference to an <code>IConnectorService</code>.
     * 
     *  @throws EventProcessingException  Error while creating instance.
     */
    public static IConnectorService getConnectorService() throws EventProcessingException
    {
        // lookup home interface
        ConnectorServiceHome home = null;
        try
        {
            Object homeObj = JNDIUtil.lookup(HOME_JNDI_NAME);
            home = (ConnectorServiceHome)PortableRemoteObject.narrow(
                   homeObj, ConnectorServiceHome.class);
        }
        catch (NamingException e)
        {
            String msg = "Failed to get ConnectorService home interface: " + e.getMessage();
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        // create EJB instance
        ConnectorService ejb = null;
        try
        {
            ejb = home.create();
        }
        catch (Exception e)
        {
            String msg = "Failed to get ConnectorService EJB: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        if (sE1Logger.isDebug())
        {
		sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"ConnectorService instance created.", null, null, null);
        }
        
        return new ConnectorServiceImpl(ejb);
    }
}
