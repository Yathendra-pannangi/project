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
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.adminservice.UnknownDataReloadCategoryException;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Remote interface for Enterprise Bean: AdminService.
 */
public interface AdminService extends javax.ejb.EJBObject
{
    /**
     *  Reload a specific data category.
     * 
     *  @param  category  The category to be reloaded.
     * 
     *  @throws UnknownDataReloadCategoryException  The category
     *          specified was not recognized.
     * 
     *  @throws EventProcessingException  Error during processing.
     * 
     *  @throws java.rmi.RemoteException  Error during method call.
     */
    void reloadData(String category)
        throws UnknownDataReloadCategoryException, 
               EventProcessingException, 
               java.rmi.RemoteException;
}
