/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.types;

/**
 * An event that occurs in the Alfresco repository
 * 
 * @author Gethin James
 * @author steveglover
 */
public interface RepositoryEvent extends Event
{
    public String getNetworkId();
    public String getTxnId();

}