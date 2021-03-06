/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.po.share.workflow;

import java.util.List;

import org.alfresco.webdrone.WebDrone;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * enum class that holds all elements of start workflow drop down contents.
 * 
 * @author Abhijeet Bharade
 * @since 1.6.2
 */
public enum WorkFlowType
{

    CLOUD_TASK_OR_REVIEW("Cloud Task or Review"), GROUP_REVIEW_AND_APPROVE("Group Review And Approve"), NEW_WORKFLOW("New Task"), POOLED_REVIEW_AND_APPROVE(
            "Pooled Review And Approve"), REVIEW_AND_APPROVE("Review And Approve"), SEND_DOCS_FOR_REVIEW("Send Document(s) For Review");

    private String title;

    WorkFlowType(String desc)
    {
        this.title = desc;
    }

    public String getTitle()
    {
        return title;
    }

    protected WebElement getTaskTypeElement(WebDrone drone)
    {
        By dropDown = By.cssSelector("div[id$='default-workflow-definition-menu'] li span.title");
        List<WebElement> liElements = drone.findAll(dropDown);
        for (WebElement liElement : liElements)
        {
            String elementText = liElement.getText().trim();
            if (elementText.equalsIgnoreCase(this.title.trim()))
            {
                return liElement;
            }
        }
        return null;
    }

    public static WorkFlowType getWorkflowType(String type)
    {
        for (WorkFlowType workflow : WorkFlowType.values())
        {
            if (type.contains(workflow.toString()))
            {
                return workflow;
            }
        }
        throw new IllegalArgumentException("Workflow Type not able find for given name: " + type);
    }

    /**
     * Based title return {@link WorkFlowType}.
     * 
     * @param title
     * @return {@link WorkFlowType}
     */
    public static WorkFlowType getWorkflowTypeByTitle(String title)
    {
        for (WorkFlowType workflow : WorkFlowType.values())
        {
            if (title.contains(workflow.getTitle()))
            {
                return workflow;
            }
        }
        throw new IllegalArgumentException("Workflow Type not able find for given Title: " + title);
    }
}
