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
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for the client
 * 
 * @author Kevin Roast
 */
public class ClientConfigElement extends ConfigElementAdapter
{
   // defaults for any config values not supplied
   private int listPageSize = 10;
   private int detailsPageSize = 10;
   private int iconsPageSize = 9;
   private String defaultView = "list";
   private int recentSpacesItems = 6;
   
   
   /**
    * Default Constructor
    */
   public ClientConfigElement()
   {
      super("client");
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public ClientConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      return null;
   }
   
   /**
    * @return Returns the defaultView.
    */
   public String getDefaultView()
   {
      return this.defaultView;
   }

   /**
    * @param defaultView The defaultView to set.
    */
   public void setDefaultView(String defaultView)
   {
      this.defaultView = defaultView;
   }

   /**
    * @return Returns the detailsPageSize.
    */
   public int getDetailsPageSize()
   {
      return this.detailsPageSize;
   }

   /**
    * @param detailsPageSize The detailsPageSize to set.
    */
   public void setDetailsPageSize(int detailsPageSize)
   {
      this.detailsPageSize = detailsPageSize;
   }

   /**
    * @return Returns the iconsPageSize.
    */
   public int getIconsPageSize()
   {
      return this.iconsPageSize;
   }

   /**
    * @param iconsPageSize The iconsPageSize to set.
    */
   public void setIconsPageSize(int iconsPageSize)
   {
      this.iconsPageSize = iconsPageSize;
   }

   /**
    * @return Returns the listPageSize.
    */
   public int getListPageSize()
   {
      return this.listPageSize;
   }

   /**
    * @param listPageSize The listPageSize to set.
    */
   public void setListPageSize(int listPageSize)
   {
      this.listPageSize = listPageSize;
   }

   /**
    * @return Returns the recentSpacesItems.
    */
   public int getRecentSpacesItems()
   {
      return this.recentSpacesItems;
   }

   /**
    * @param recentSpacesItems The recentSpacesItems to set.
    */
   public void setRecentSpacesItems(int recentSpacesItems)
   {
      this.recentSpacesItems = recentSpacesItems;
   }
}
