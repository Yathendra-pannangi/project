//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events;

//=================================================
// Imports from java namespace
//=================================================
import java.io.Serializable;

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
 * Represents the credentials required to validate a Connector client.
 */
public class ConnectorCredentials implements Serializable
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

    private String mUsername = null;
    private String mEnvironment = null;
    private String mSecurityToken = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Default constructor.
     */
    public ConnectorCredentials()
    {}

    /**
     * Constructs a ConnectorCredentials with all required fields.
     * 
     * @param username the username
     * @param environment the user's login environment
     * @param token the user's security token.
     */
    public ConnectorCredentials(
        String username,
        String environment,
        String token)
    {
        mUsername = username;
        mEnvironment = environment;
        mSecurityToken = token;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return the username
     */
    public String getUsername()
    {
        return mUsername;
    }

    /**
     * @return the user's login environment
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * @return the user's security token string.
     */
    public String getSecurityToken()
    {
        return mSecurityToken;
    }
    
    /**
     * @param env  The user's environment.
     */
    public void setEnvironment(String env)
    {
        mEnvironment = env;
    }

    /**
     * @param token  The user's security token string.
     */
    public void setSecurityToken(String token)
    {
        mSecurityToken = token;
    }

    /**
     * @param username  The user's user name.
     */
    public void setUsername(String username)
    {
        mUsername = username;
    }

}
