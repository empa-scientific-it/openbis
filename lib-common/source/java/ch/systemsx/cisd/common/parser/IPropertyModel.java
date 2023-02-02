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

/**
 * This returns the mapping informations needed in {@link IParserObjectFactory} via the {@link IPropertyMapper}.
 * <p>
 * This model is specified by the parsed file.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IPropertyModel
{
    /** Returns the column number where the information regarding this property could be found. */
    public int getColumn();

    /** Returns the property code as it has been found in the parsed file. */
    public String getCode();

    /**
     * Returns the format of this property.
     * <p>
     * This is useful when the type of this property is, for instance, <code>java.util.Date</code>.
     * </p>
     */
    public String getFormat();
}