/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.preference.script;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeObject;

/**
 * @author Roy Wetherall
 */
public class ScriptPreferenceService extends BaseScopableProcessorExtension
{
    private ServiceRegistry services;
    
    /** Preference Service */
    private PreferenceService preferenceService;
    
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }
    
    public NativeObject getPreferences(String userName)
    {
        return getPreferences(userName, null);
    }
    
    public NativeObject getPreferences(String userName, String preferenceFilter)
    {
        Map<String, Serializable> prefs = this.preferenceService.getPreferences(userName, preferenceFilter);        
        NativeObject result = new NativeObject();
        
        for (Map.Entry<String, Serializable> entry : prefs.entrySet())
        {
            String[] keys = entry.getKey().replace(".", "-").split("-");            
            setPrefValue(keys, entry.getValue(), result);
        }        
        
        return result;
    }
    
    private void setPrefValue(String[] keys, Serializable value, NativeObject object)
    {
        NativeObject currentObject = object;
        int index = 0;
        for (String key : keys)
        {
            if (index == keys.length-1)
            {
                currentObject.put(key, currentObject, value);
            }
            else
            {
                NativeObject newObject = null;
                Object temp = currentObject.get(key, currentObject);
                if (temp == null || temp instanceof NativeObject == false)
                {
                    newObject = new NativeObject();
                    currentObject.put(key, currentObject, newObject);
                }
                else
                {
                    newObject = (NativeObject)temp;
                }
                currentObject = newObject;
            }
         
            index ++;
        }
    }
    
    public void setPreferences(String userName, NativeObject preferences)
    {
        Map<String, Serializable> values = new HashMap<String, Serializable>(10);
        getPrefValues(preferences, null, values);
        
        this.preferenceService.setPreferences(userName, values);
    }
    
    private void getPrefValues(NativeObject currentObject, String currentKey, Map<String, Serializable> values)
    {
        Object[] ids = currentObject.getIds();
        for (Object id : ids)
        {
            String key = getAppendedKey(currentKey, id.toString());
            Object value = currentObject.get(id.toString(), currentObject);
            if (value instanceof NativeObject)
            {
                getPrefValues((NativeObject)value, key, values);
            }
            else
            {
                values.put(key, (Serializable)value);
            }
        }
    }
    
    private String getAppendedKey(String currentKey, String key)
    {
        StringBuffer buffer = new StringBuffer(64);
        if (currentKey != null && currentKey.length() != 0)
        {
            buffer.append(currentKey).append(".").append(key);
        }
        else
        {    
            buffer.append(key);
        }
        return buffer.toString();
    }
}
