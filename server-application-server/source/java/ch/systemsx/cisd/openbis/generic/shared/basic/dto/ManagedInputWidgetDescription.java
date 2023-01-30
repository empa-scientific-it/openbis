/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class ManagedInputWidgetDescription implements IManagedInputWidgetDescription
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String value;

    private String description; // for field info

    private boolean mandatory;

    //
    // IManagedInputWidgetDescription
    //

    @Override
    public String getCode()
    {
        return code;
    }

    public IManagedInputWidgetDescription setCode(String code)
    {
        if (isBlank(code))
        {
            throw new IllegalArgumentException("Code is null or a blank string.");
        }
        this.code = code.toUpperCase();
        return this;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        if (isBlank(label))
        {
            throw new IllegalArgumentException("Label is null or a blank string.");
        }
        this.label = label;
        if (code == null)
        {
            code = label.toUpperCase();
        }
    }

    private boolean isBlank(String string)
    {
        return string == null || string.trim().length() == 0;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    @Override
    public IManagedInputWidgetDescription setValue(String value)
    {
        this.value = value;
        return this;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public IManagedInputWidgetDescription setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public boolean isMandatory()
    {
        return mandatory;
    }

    @Override
    public IManagedInputWidgetDescription setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
        return this;
    }

}
