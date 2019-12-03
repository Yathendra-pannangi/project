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
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Interface for the AdminService.
 */
public interface IAdminService
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    /** EventTypeDefinitions category. */
    String EVENT_TYPE_CATEGORY = "EventTypeDefinitions";

    /** SubscriberDefinitions category. */
    String SUBSCRIBER_CATEGORY = "SubscriberDefinitions";
    
    //=================================================
    // Methods.
    //=================================================

    /**
     *  Reload a specific data category.
     * 
     *  @param  category  The category to be reloaded.
     * 
     *  @throws UnknownDataReloadCategoryException  The category
     *          specified was not recognized.
     * 
     *  @throws EventProcessingException  Error during processing.
     */
    void reloadData(String category)
        throws UnknownDataReloadCategoryException, 
               EventProcessingException;
}
