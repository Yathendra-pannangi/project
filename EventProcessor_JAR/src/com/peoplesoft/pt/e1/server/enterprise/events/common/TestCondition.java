//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

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
 *  This interface is used to determine if the code should simulate specific
 *  error conditions for test purposes.
 */
public interface TestCondition
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

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Indicates that the transfer agent's update of the database to indicate an event
     *  is IN_PROCESS should fail.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldTransferDbUpdateFail();
    
    /**
     *  Indicates that the transfer agent's update of the database to indicate an event
     *  is IN_PROCESS should fail.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldTransferDbUpdateFail(boolean fail);
    
    /**
     *  Indicates that the transfer agent's commit the database to indicate an event
     *  is IN_PROCESS should fail.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldTransferDbCommitFail();
    
    /**
     *  Indicates that the transfer agent's commit the database to indicate an event
     *  is IN_PROCESS should fail.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldTransferDbCommitFail(boolean fail);
    
    /**
     *  Indicates that the transfer agent's sending to of an event to an event
     *  queue should fail.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldTransferQueueSendFail();
    
    /**
     *  Indicates that the transfer agent's sending to of an event to an event
     *  queue should fail.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldTransferQueueSendFail(boolean fail);
    
    /**
     *  Indicates that the transfer agent's commit of a send to an event queue
     *  should fail.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldTransferQueueCommitFail();
    
    /**
     *  Indicates that the transfer agent's commit of a send to an event queue
     *  should fail.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldTransferQueueCommitFail(boolean fail);
    
    /**
     *  Indicates that the event processor's get of an event from the database
     *  should fail.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldProcessorDbGetFail();
    
    /**
     *  Indicates that the event processor's get of an event from the database
     *  should fail.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldProcessorDbGetFail(boolean fail);
    
    /**
     *  Indicates that the event processor should fail at the end of the processing
     *  logic for an event.
     * 
     *  @return   <code>true</code> = simulate failure,<br>
     *            <code>false</code> = don't simulate failure.
     */
    boolean shouldProcessorFail();
    
    /**
     *  Indicates that the event processor should fail at the end of the processing
     *  logic for an event.
     * 
     *  @param fail   <code>true</code> = simulate failure,<br>
     *                <code>false</code> = don't simulate failure.
     */
    void shouldProcessorFail(boolean fail);

}
