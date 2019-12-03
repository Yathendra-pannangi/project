//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.adminservice.internal;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================

/**
 * Home interface for Enterprise Bean: AdminService.
 */
public interface AdminServiceHome extends javax.ejb.EJBHome
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
     * Creates a default instance of Session Bean: AdminService.
     * 
     * @return  A new <code>AdminService</code> reference.
     * 
     * @throws javax.ejb.CreateException  Error during creation.
     * 
     * @throws java.rmi.RemoteException   Error during creation.
     */
    AdminService create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
