//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.clientsvc.internal;

import com.jdedwards.base.util.BaseChainedException;

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
 *  indicates an error during the marshal/unmarshal process.
 */
public class XmlMarshalException extends BaseChainedException
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
    
    /**
     * {@inheritDoc}
     */
    public XmlMarshalException(String message)
    {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public XmlMarshalException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * {@inheritDoc}
     */
    public XmlMarshalException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
