//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.util;

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
 *  Interface for JVM lifecycle listeners.
 *  <p/>
 *  To add a new JVM lifecycle listener create a class that implements
 *  this interface and then add the fully qualified classname to the
 *  jvm_lifecycle_listeners.txt file.
 *  <p/>
 *  Note: Classes implementing this interface must be public and must
 *  have a public default constructor available.
 */
public interface IJVMLifecycleListener
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Name of the resource file that lists all the JVM lifecycle listener classes. */
    String LIFECYCLE_LISTENERS_RESOURCE
        = "com/peoplesoft/pt/e1/server/enterprise/events/util/jvm_lifecycle_listeners.txt";
    
    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Called during JVM start-up.
     */
    void startup();
    
    /**
     *  Called during JVM shutdown.
     */
    void shutdown();
}
