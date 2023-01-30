/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api;

/**
 * Interface implemented by all entity properties
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Dynamic Properties API.
public interface IEntityPropertyAdaptor
{
    /** Returns the code of property type. */
    public String propertyTypeCode();

    /** Returns the property value as string. */
    public String valueAsString();

    /** Returns the property value as string that should be rendered e.g. in a table. */
    public String renderedValue();

}
