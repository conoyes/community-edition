/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of the Alfresco Web Quick Start module.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.module.org_alfresco_module_wcmquickstart.util.contextparser;

import org.alfresco.module.org_alfresco_module_wcmquickstart.model.WebSiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * Website owner parser
 * 
 * @author Roy Wetherall
 */
public class WebsiteOwnerContextParser extends ContextParser implements WebSiteModel
{	
	/** Ownable service */
	private OwnableService ownableService;
	
	/** 
	 * Sets the ownable service
	 * @param ownableService	ownable service
	 */
	public void setOwnableService(OwnableService ownableService)
    {
	    this.ownableService = ownableService;
    }
	
	/**
	 * @see org.alfresco.module.org_alfresco_module_wcmquickstart.util.contextparser.ContextParser#execute(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public String execute(NodeRef context)
	{	
		String result = null;
		NodeRef nodeRef = siteHelper.getRelevantWebSite(context);
		if (nodeRef != null)
		{
			result = ownableService.getOwner(nodeRef);
		}
		return result;
	}

}