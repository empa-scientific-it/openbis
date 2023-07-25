/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;

/**
 * A {@link IEntityProperty} class that only stores the generic value, but not a vocabulary term value or a material value.
 * 
 * @author Bernd Rinn
 */
public class GenericEntityProperty extends AbstractEntityProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Serializable originalValue;

    private Serializable value;

    @Override
    public String getStringValue()
    {
        if(value != null && value.getClass().isArray()) {
            return getArrayAsString();
        }
        return (String) value;
    }

    @Override
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public void setValue(Serializable value)
    {
        this.value = value;
    }

    @Override
    public Serializable getValue()
    {
        return value;
    }

    @Override
    public String tryGetOriginalValue()
    {
        if (getOriginalValue() != null)
        {
            return getOriginalValue();
        }
        return super.tryGetAsString();
    }

    public void setOriginalValue(String originalValue)
    {
        this.originalValue = originalValue;
    }

    public String getOriginalValue()
    {
        return (String) originalValue;
    }

}
