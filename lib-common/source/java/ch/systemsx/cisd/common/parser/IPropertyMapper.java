/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.parser;

import java.util.Set;

/**
 * The job of <code>PropertyMapper</code> is to return mapping informations regarding a given property code.
 * 
 * @author Christian Ribeaud
 */
public interface IPropertyMapper
{

    /**
     * Returns an <code>IPropertyModel</code> for a given property code.
     * 
     * @throws IllegalArgumentException if given <var>propertyCode</var> does not exist.
     */
    public IPropertyModel getPropertyModel(final String propertyCode)
            throws IllegalArgumentException;

    /**
     * Returns a set of all property codes.
     * <p>
     * Note that changes applied to returned <code>Set</code> are not reflected in the backed collection.
     * </p>
     */
    public Set<String> getAllPropertyCodes();

    /** Whether there is a property with given <var>propertyCode</var>. */
    public boolean containsPropertyCode(final String propertyCode);

    /** Returns default value for property with given <var>propertyCode</var> */
    public String tryGetPropertyDefault(final String propertyCode);
}