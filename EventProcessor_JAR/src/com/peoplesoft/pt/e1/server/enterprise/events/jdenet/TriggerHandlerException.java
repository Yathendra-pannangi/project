//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.jdenet;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.system.net.ProcessKernelException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Indicates an exception within the Trigger Listener system.
 */
class TriggerHandlerException extends ProcessKernelException
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

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * This method wraps the exception with TriggerListenerExceprion.
     * @param msg Description of the error
     */
    public TriggerHandlerException(String msg)
    {
        super(msg);
    }
}
