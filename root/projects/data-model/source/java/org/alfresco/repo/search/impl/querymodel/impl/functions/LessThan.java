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
package org.alfresco.repo.search.impl.querymodel.impl.functions;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.impl.BaseComparison;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * @author andyh
 */
public class LessThan extends BaseComparison
{
    public final static String NAME = "LessThan";

    /**
     * @param name
     * @param returnType
     * @param argumentDefinitions
     */
    public LessThan()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, ARGS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getValue(java.util.Set)
     */
    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

}