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
import java.util.Set;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.server.enterprise.events.cache.SubscribedToEventType;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.Subscription;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F907071_SubscribedEvents;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F907072_SubscribedEnvironments;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Implements routing logic for events based on subscriptions.
 */
public class EventRouter
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
     *  Check to see is an event matches a subscription.  Events can be either an
     *  exact match or they can match a wildcard value in the subscription for event
     *  type or environment.
     * 
     *  @param category  Category of the event.
     * 
     *  @param type  Type of the event.
     * 
     *  @param environment  Environment the event originated in.
     * 
     *  @param subscription  The subscription to test against.
     * 
     *  @return  <code>true</code> indicates the event matches the subscriptions,<br>
     *           <code>false</code> indicates the event does not match the subscriptions.
     */
    public boolean matchSubscription(
        String category,
        String type,
        String environment,
        Subscription subscription)
    {
        boolean result = false;

        //
        //  First, check to see if the environments match.  The environment can either be an
        //  exact match or a match to a wildcard.
        //
        Set subEnvs = subscription.getSubscribedToEnvironments();
        if (subEnvs.contains(F907072_SubscribedEnvironments.WILDCARD_ENVIRONMENT)
            || subEnvs.contains(environment))
        {
            //
            //  The environment matches.  Now check to see if the category and type
            //  are a match.  The category must be an exact match but the type can be
            //  either an exact match or a wildcard match.
            //
            SubscribedToEventType exact = new SubscribedToEventType(category, type);
            if (subscription.getSubscribedToEventTypes().contains(exact))
            {
                //
                //  Found an exact match.
                //
                result = true;
            }
            else
            {
                //
                //  Exact match failed, check for a wildcard match.
                //
                SubscribedToEventType wild =
                    new SubscribedToEventType(category, F907071_SubscribedEvents.WILDCARD_TYPE);
                if (subscription.getSubscribedToEventTypes().contains(wild))
                {
                    //
                    //  Found a wildcard match.
                    //
                    result = true;
                }
            }
        }

        return result;
    }
}
