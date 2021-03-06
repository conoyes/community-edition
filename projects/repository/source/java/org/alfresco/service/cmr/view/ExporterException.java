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
package org.alfresco.service.cmr.view;


/**
 * Base Exception of Export Exceptions.
 * 
 * @author David Caruana
 */
public class ExporterException extends RuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public ExporterException(String msg)
    {
       super(msg);
    }
    
    public ExporterException(String msg, Throwable cause)
    {
       super(msg, cause);
    }

}
