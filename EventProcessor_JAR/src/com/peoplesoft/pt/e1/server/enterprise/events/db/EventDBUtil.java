//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.db;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.impl.signon.JDBSignon;
import com.jdedwards.database.jdb.JDB;
import com.jdedwards.database.jdb.JDBSystem;
import com.jdedwards.database.services.bootstrap.JDBBootstrapSessionInfo;
import com.jdedwards.system.security.Session;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Database utilities used by the event system.
 */
public final class EventDBUtil
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static Session sSession = null;

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private EventDBUtil()
    {}

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Gets the default database access based on the configuration in the JDBJ.ini file.
     * 
     *  @return The default database access handle.
     * 
     *  @throws JDBException  An error from the database layer.
     */
    public static JDBDatabaseAccess getDefaultDBAccess() throws JDBException
    {
        JDBDatabaseAccess dbAccess = null;

        // Load database connection properties
        // Get the bootstrap sSession info to use as defaults.
        JDBBootstrapSessionInfo bootstrapSessionInfo = JDBBootstrapSessionInfo.getInstance();

        // Get the user.
        String user = bootstrapSessionInfo.getUser();

        // Get the password.
        String password = bootstrapSessionInfo.getPassword();

        // Get the environment.
        String environment = bootstrapSessionInfo.getEnvironment();

        // Get the role.
        String role = bootstrapSessionInfo.getRole();

        // Make a connection
        dbAccess = JDB.connect(user, password, environment, role);

        return dbAccess;
    }

    /**
     *  Gets the spec database access based on the configuration in the JDBJ.ini file.
     *  @return The default database access handle.
     *  @throws JDBException  An error from the database layer.
     */
    public static JDBDatabaseAccess getSpecDBAccess() throws JDBException
    {
        JDBDatabaseAccess jdb = JDBSystem.getSpecConnection(sSession, null);
        return jdb;
    }
    
    /**
     * Returns the security sSession Object.
     * @return Session
     * 
     * @throws EventProcessingException  Error getting the security session.
     */
    public static synchronized Session getSecuritySession() throws EventProcessingException
    {
        if (sSession == null)
        {
            try
            {
                JDBBootstrapSessionInfo sessionInfo = JDBBootstrapSessionInfo.getInstance();
                sSession =
                    JDBSignon.signon(
                        sessionInfo.getUser(),
                        sessionInfo.getPassword(),
                        sessionInfo.getEnvironment(),
                        sessionInfo.getRole());
            }
            catch (Exception e)
            {
                throw new EventProcessingException("Failed to get security session", e);
            }
        }

        return sSession;
    }
   


     public static JDBDatabaseAccess getExtendedTokenDefaultDBAccess() throws JDBException
     {
               JDBBootstrapSessionInfo bootstrapSessionInfo = JDBBootstrapSessionInfo.getInstance();

               return JDB.connectExtendedToken(bootstrapSessionInfo.getUser(),
                                               bootstrapSessionInfo.getPassword(),
                                               bootstrapSessionInfo.getEnvironment(),
                                               bootstrapSessionInfo.getRole());
     }
}
