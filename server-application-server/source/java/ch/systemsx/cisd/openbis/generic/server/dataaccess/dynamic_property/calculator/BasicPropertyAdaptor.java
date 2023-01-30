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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;

/**
 * Simple {@link IEntityPropertyAdaptor} implementation
 * 
 * @author Piotr Buczek
 */
public class BasicPropertyAdaptor implements IEntityPropertyAdaptor
{

    private final String code;

    private final String value;

    private final EntityPropertyPE propertyPE;

    public BasicPropertyAdaptor(String code, String value, EntityPropertyPE propertyPE)
    {
        this.code = code;
        this.value = value;
        this.propertyPE = propertyPE;
    }

    public BasicPropertyAdaptor(String code, String value)
    {
        this(code, value, null);
    }

    @Override
    public String propertyTypeCode()
    {
        return code;
    }

    @Override
    public String valueAsString()
    {
        return value;
    }

    @Override
    public String renderedValue()
    {
        return valueAsString();
    }

    public EntityPropertyPE getPropertyPE()
    {
        return propertyPE;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "code: " + propertyTypeCode() + ", value: " + valueAsString();
    }

}
