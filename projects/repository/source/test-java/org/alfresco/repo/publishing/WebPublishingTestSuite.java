/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.publishing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Nick Smith
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { ChannelServiceImplTest.class,
    PublishingEventHelperTest.class,
    ChannelServiceImplIntegratedTest.class,
    PublishingRootObjectTest.class,
//    EnvironmentImplTest.class,
    PublishingQueueImplTest.class,
    PublishingPackageSerializerTest.class,
//  PublishEventActionTest.class,
    PublishWebContentJbpmTest.class,
    PublishingIntegratedTest.class
    })
public class WebPublishingTestSuite
{
    //NOOP
}
