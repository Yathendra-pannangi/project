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
 * Indacates that an unknown data reload category was encountered.
 */
public class UnknownDataReloadCategoryException extends EventProcessingException
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDataReloadCategoryException(String msg)
    {
        super(msg);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDataReloadCategoryException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDataReloadCategoryException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
