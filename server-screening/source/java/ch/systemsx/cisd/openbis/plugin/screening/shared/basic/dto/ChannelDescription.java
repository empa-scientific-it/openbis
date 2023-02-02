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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes channel by code and label.
 * 
 * @author Izabela Adamczyk
 */
public class ChannelDescription implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    // for GWT
    @SuppressWarnings("unused")
    private ChannelDescription()
    {
    }

    public ChannelDescription(String name)
    {
        assert name != null;
        this.code = normalize(name);
        this.label = name;
    }

    // TODO 2010-08-25, Tomasz Pylak: merge with {@link CodeAndLabel}
    private String normalize(String name)
    {
        if (name == null)
        {
            return null;
        }
        return name.toUpperCase().replaceAll("[^A-Z0-9-]", "_");
    }

    public ChannelDescription(String code, String label)
    {
        assert code != null && label != null;
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return getCode();
    }
}
