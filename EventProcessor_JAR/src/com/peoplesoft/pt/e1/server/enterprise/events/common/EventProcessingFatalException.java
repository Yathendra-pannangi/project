//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

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
 *  Indicates a fatal, non-recoverable, exception occured within the event system.
 */
public class EventProcessingFatalException extends RuntimeException
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
    
    private Throwable mCause;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  {@inheritDoc}
     */
    public EventProcessingFatalException(String msg)
    {
        super(msg);
    }
        
    /**
     *  {@inheritDoc}
     */
    public EventProcessingFatalException(String msg, Throwable cause)
    {
        super(msg);
        mCause = cause;
    }
        
    /**
     *  {@inheritDoc}
     */
    public EventProcessingFatalException(Throwable cause)
    {
        super("EventProcessingFatalException: " + cause.getMessage());
        mCause = cause;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Returns the cause of this throwable or null if the cause is nonexistent or unknown.
     * 
     *  @return A <code>Throwable</code>
     */
    public Throwable getCause()
    {
        return mCause;
    }
}
