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
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Factory class to create an instance of the <code>TestCondition</code> interface.
 * 
 *  <p>The factory checks the <code>com.peoplesoft.pt.e1.server.tran.events.TestMode</code>
 *  system property to determine if the system is running in test mode.  If this property is
 *  present and set to <code>true</code> then the <code>TestCondition</code> interface
 *  instance created may indicate specific test conditions should be created, otherwise the
 *  interface will always indicate test conditions should not be created.
 */
public final class TestConditionFactory
{
    //=================================================
    // Static fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(TestConditionFactory.class.getName());
    
    private static TestCondition sInstance;

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private to prevent instances from being created.
     */
    private TestConditionFactory()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Create a <code>TestCondition</code> instance.
     * 
     *  @return  instance.
     */
    public static synchronized TestCondition getInstance()
    {
        if (sInstance == null)
        {
            boolean inTestMode =
                Boolean.getBoolean("com.peoplesoft.pt.e1.server.tran.events.TestMode");
            if (inTestMode)
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event system in test mode", null, null, null);
                sInstance = new TestConditionTestMode();
            }
            else
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event system in production mode", null, null, null);
                sInstance = new TestConditionProductionMode();
            }
        }
                 
        return sInstance;
    }

    /**
     *  TestCondition class to use in production.
     */
    private static class TestConditionProductionMode implements TestCondition
    {        
        public boolean shouldTransferDbUpdateFail()
        {
            return false;
        }
        
        public void shouldTransferDbUpdateFail(boolean fail)
        {}
        
        public boolean shouldTransferDbCommitFail()
        {
            return false;
        }
        
        public void shouldTransferDbCommitFail(boolean fail)
        {}
        
        public boolean shouldTransferQueueSendFail()
        {
            return false;
        }
        
        public void shouldTransferQueueSendFail(boolean fail)
        {}
        
        public boolean shouldTransferQueueCommitFail()
        {
            return false;
        }
        
        public void shouldTransferQueueCommitFail(boolean fail)
        {}
        
        public boolean shouldProcessorDbGetFail()
        {
            return false;
        }
        
        public void shouldProcessorDbGetFail(boolean fail)
        {}
        
        public boolean shouldProcessorFail()
        {
            return false;
        }
        
        public void shouldProcessorFail(boolean fail)
        {}
    }
    
    /**
     *  TestCondition class to use in test.
     */
    private static class TestConditionTestMode implements TestCondition
    {
        private boolean mShouldTransferDbUpdateFail;
        private boolean mShouldTransferDbCommitFail;
        private boolean mShouldTransferQueueSendFail;
        private boolean mShouldTransferQueueCommitFail;
        private boolean mShouldProcessorDbGetFail;
        private boolean mShouldProcessorFail;
        
        public synchronized boolean shouldTransferDbCommitFail()
        {
            return mShouldTransferDbCommitFail;
        }

        public synchronized boolean shouldTransferDbUpdateFail()
        {
            return mShouldTransferDbUpdateFail;
        }

        public synchronized boolean shouldTransferQueueCommitFail()
        {
            return mShouldTransferQueueCommitFail;
        }

        public synchronized boolean shouldTransferQueueSendFail()
        {
            return mShouldTransferQueueSendFail;
        }

        public synchronized void shouldTransferDbCommitFail(boolean b)
        {
            mShouldTransferDbCommitFail = b;
        }

        public synchronized void shouldTransferDbUpdateFail(boolean b)
        {
            mShouldTransferDbUpdateFail = b;
        }

        public synchronized void shouldTransferQueueCommitFail(boolean b)
        {
            mShouldTransferQueueCommitFail = b;
        }

        public synchronized void shouldTransferQueueSendFail(boolean b)
        {
            mShouldTransferQueueSendFail = b;
        }

        public boolean shouldProcessorDbGetFail()
        {
            return mShouldProcessorDbGetFail;
        }

        public boolean shouldProcessorFail()
        {
            return mShouldProcessorFail;
        }

        public void shouldProcessorDbGetFail(boolean b)
        {
            mShouldProcessorDbGetFail = b;
        }

        public void shouldProcessorFail(boolean b)
        {
            mShouldProcessorFail = b;
        }

    }
}
