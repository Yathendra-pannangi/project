//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.connectorsvc.internal;

//=================================================
// Imports from java namespace
//=================================================
import java.rmi.RemoteException;

//=================================================
// Imports from javax namespace
//=================================================
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================

/**
 * Home interface for Enterprise Bean: ConnectorService.
 */
public interface ConnectorServiceHome extends EJBHome
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Creates a default instance of Session Bean: ConnectorService.
     * 
     * @throws CreateException if the container can't create the bean
     * @throws RemoteException if the remote bean can't be created?
     * @return the ConnectorService instance
     */
    ConnectorService create() throws CreateException, RemoteException;
}
