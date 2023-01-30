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

import java.io.Serializable;

/**
 * Configuration parameters of a portlet.
 * <p>
 * Instances of this class are serialized in the personal {@link DisplaySettings} of a user. Thus, CHANGES IN THIS CLASS MIGHT LEAD TO A LOST OF
 * PERSONAL SETTINGS
 * 
 * @author Franz-Josef Elmer
 */
public class PortletConfiguration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    // Needed by GWT for deserialization
    @SuppressWarnings("unused")
    private PortletConfiguration()
    {
    }

    /**
     * Creates a new instance for specified unique portlet name.
     */
    public PortletConfiguration(String name)
    {
        if (name == null || name.trim().length() == 0)
        {
            throw new IllegalArgumentException("Undefined portlet name.");
        }
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
