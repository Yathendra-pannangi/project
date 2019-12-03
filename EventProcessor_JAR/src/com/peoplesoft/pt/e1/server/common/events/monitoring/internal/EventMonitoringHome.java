//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.monitoring.internal;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================

//=================================================
//Imports from org namespace
//=================================================
/**
 * Home interface for Enterprise Bean: EventMonitoring.
 */
public interface EventMonitoringHome extends javax.ejb.EJBHome
{
    //=================================================
    // Non-public static class fields.
    //=================================================

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
     * Creates a default instance of Session Bean: EventMonitoring.
     * @throws javax.ejb.CreateException ex
     * @throws java.rmi.RemoteException ex
     * @return EventMonitoring 
     */
    com.peoplesoft.pt.e1.server.common.events.monitoring.internal.EventMonitoring create()
        throws javax.ejb.CreateException, java.rmi.RemoteException;
}
