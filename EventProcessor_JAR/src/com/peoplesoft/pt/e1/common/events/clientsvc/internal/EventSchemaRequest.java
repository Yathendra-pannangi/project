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
 *  This class represents a request for an event template from
 *  a remote event client.
 */
public class EventSchemaRequest extends ClientRequest
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
    
    private String  mHost;
    
    private int     mPort;
    
    private String  mCategory;
    
    private String  mType;
    
    private String  mEnvironment;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  event category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @return  environment.
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * @return  host.
     */
    public String getHost()
    {
        return mHost;
    }

    /**
     * @return  port.
     */
    public int getPort()
    {
        return mPort;
    }

    /**
     * @return  event type.
     */
    public String getType()
    {
        return mType;
    }

    /**
     * @param category  event category.
     */
    public void setCategory(String category)
    {
        mCategory = category;
    }

    /**
     * @param environment  environment.
     */
    public void setEnvironment(String environment)
    {
        mEnvironment = environment;
    }

    /**
     * @param host  host.
     */
    public void setHost(String host)
    {
        mHost = host;
    }

    /**
     * @param port   port.
     */
    public void setPort(int port)
    {
        mPort = port;
    }

    /**
     * @param type  event type.
     */
    public void setType(String type)
    {
        mType = type;
    }
}
