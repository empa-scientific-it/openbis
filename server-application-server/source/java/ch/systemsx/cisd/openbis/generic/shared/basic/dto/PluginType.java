/*
 * Copyright 2013 ETH Zuerich, CISD
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
 * @author Pawel Glyzewski
 */
public enum PluginType implements Serializable
{
    JYTHON("Jython Script Plugin"), PREDEPLOYED("Predeployed Plugin");

    private final String description;

    private PluginType(final String description)
    {
        this.description = description;
    }

    public final String getDescription()
    {
        return description;
    }
}
