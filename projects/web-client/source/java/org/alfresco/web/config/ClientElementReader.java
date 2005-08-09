/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.config;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for client config values
 * 
 * @author Kevin Roast
 */
public class ClientElementReader implements ConfigElementReader
{
   public static final String ELEMENT_CLIENT = "client";
   public static final String ELEMENT_PAGESIZE = "page-size";
   public static final String ELEMENT_LIST = "list";
   public static final String ELEMENT_DETAILS ="details";
   public static final String ELEMENT_ICONS = "icons";
   public static final String ELEMENT_DEFAULTVIEW = "default-view";
   public static final String ELEMENT_RECENTSPACESITEMS = "recent-spaces-items";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      ClientConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ELEMENT_CLIENT) == false)
         {
            throw new ConfigException("ClientElementReader can only parse " +
                  ELEMENT_CLIENT + "elements, the element passed was '" + name + "'");
         }
         
         configElement = new ClientConfigElement();
         
         // get the page size sub-element
         Element pageSize = element.element(ELEMENT_PAGESIZE);
         if (pageSize != null)
         {
            {
               Element viewPageSize = pageSize.element(ELEMENT_LIST);
               if (viewPageSize != null)
               {
                  configElement.setListPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
            {
               Element viewPageSize = pageSize.element(ELEMENT_DETAILS);
               if (viewPageSize != null)
               {
                  configElement.setDetailsPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
            {
               Element viewPageSize = pageSize.element(ELEMENT_ICONS);
               if (viewPageSize != null)
               {
                  configElement.setIconsPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
         }
         
         // get the default view mode
         Element defaultView = element.element(ELEMENT_DEFAULTVIEW);
         if (defaultView != null)
         {
            configElement.setDefaultView(defaultView.getTextTrim());
         }
         
         // get the recent space max items
         Element recentSpaces = element.element(ELEMENT_RECENTSPACESITEMS);
         if (recentSpaces != null)
         {
            configElement.setRecentSpacesItems(Integer.parseInt(recentSpaces.getTextTrim()));
         }
      }
      
      return configElement;
   }
}
