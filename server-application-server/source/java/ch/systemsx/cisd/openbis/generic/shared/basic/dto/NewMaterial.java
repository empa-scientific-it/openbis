/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A material to register.
 * 
 * @author Izabela Adamczyk
 */
public final class NewMaterial extends Code<NewMaterial> implements IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    public NewMaterial()
    {
    }

    public NewMaterial(final String code)
    {
        setCode(code);
    }

    @Override
    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    @Override
    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getCode();
    }
}
