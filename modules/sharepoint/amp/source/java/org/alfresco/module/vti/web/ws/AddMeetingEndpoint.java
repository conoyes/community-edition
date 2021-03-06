/*
* Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.module.vti.web.ws;

import org.alfresco.module.vti.handler.MeetingServiceHandler;
import org.alfresco.module.vti.handler.SiteTypeException;
import org.alfresco.module.vti.metadata.model.MeetingBean;
import org.dom4j.Element;

/**
 * Class for handling AddMeeting soap method
 * 
 * @author Nick Burch
 */
public class AddMeetingEndpoint extends AbstractMeetingEndpoint
{
    public AddMeetingEndpoint(MeetingServiceHandler handler)
    {
        super(handler);
    }
    
    @Override
    protected void executeMeetingAction(VtiSoapRequest soapRequest, VtiSoapResponse soapResponse, String siteName,
            MeetingBean meetingBean, int sequence, int recurrenceId, boolean ignoreAttendees, boolean cancelMeeting) throws Exception
    {
        // Perform the addition of the meeting
        try
        {
            handler.addMeeting(siteName, meetingBean);
        }
        catch (SiteTypeException ste)
        {
            throw new VtiSoapException(ste.getMsgId(), 6l);
        }
        catch (Exception e)
        {
            throw new VtiSoapException(e.getMessage(), 7l, e);
        }
        
        // Build the response
        Element e = buildMeetingResponse(soapResponse);
        Element result = e.addElement("AddMeetingResult");
        Element meeting = result.addElement("AddMeeting");
        meeting.addAttribute("Url", getHost(soapRequest) + soapRequest.getAlfrescoContextName() + "/" + siteName + "?calendar=calendar")
               .addAttribute("HostTitle", meetingBean.getSubject()).addAttribute("UniquePermissions", "true")
               .addAttribute("MeetingCount", "1").addAttribute("AnonymousAccess", "false")
               .addAttribute("AllowAuthenticatedUsers", "false");
    }
}