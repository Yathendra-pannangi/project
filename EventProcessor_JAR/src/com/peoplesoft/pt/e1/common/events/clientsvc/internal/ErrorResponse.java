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
 *  This class represents an error response to a remote event client.
 */
public class ErrorResponse extends ClientResponse
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Unknown error. */
    public static final int UNKNOWN = 0;
    
    /**  Indicates an unsupported request was received.  */
    public static final int NOT_SUPPORTED = 101;
    
    /**  Error from the event system.  */
    public static final int EVENT_PROCESSING_EXCEPTION = 102;
    
    /**  Invalid user credentials.  */
    public static final int INVALID_CREDENTIALS_EXCEPTION = 103;
    
    /**  An unknown subscriber.  */
    public static final int NO_SUCH_SUBSCRIBER_EXCEPTION = 104;
    
    /**  Client session is closed.  */
    public static final int CLOSED_SESSION_EXCEPTION = 105;
    
    /**  Client session is STOPPED.  */
    public static final int STOPPED_SESSION_EXCEPTION = 106;

    //=================================================
    // Instance member fields.
    //=================================================
    
    private int mErrorCode = UNKNOWN;
    
    private String mErrorMessage = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Default constructor.
     */
    public ErrorResponse()
    {}
    
    /**
     *  Constructor.
     * 
     *  @param errorCode  the error code.
     * 
     *  @param errorMsg  the error message.
     */
    public ErrorResponse(int errorCode, String errorMsg)
    {
        mErrorCode = errorCode;
        mErrorMessage = errorMsg;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  The error message.
     */
    public String getErrorMessage()
    {
        return mErrorMessage;
    }

    /**
     * @param msg  The error message.
     */
    public void setErrorMessage(String msg)
    {
        mErrorMessage = msg;
    }

    /**
     * @return  The error code.
     */
    public int getErrorCode()
    {
        return mErrorCode;
    }

    /**
     * @param code  The error code.
     */
    public void setErrorCode(int code)
    {
        mErrorCode = code;
    }

}
