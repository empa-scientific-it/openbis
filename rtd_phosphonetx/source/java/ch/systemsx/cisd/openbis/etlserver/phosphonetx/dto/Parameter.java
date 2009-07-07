/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class Parameter
{
    private String name;
    private String value;
    private String type;

    @XmlAttribute(name = "name", required = true)
    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    @XmlAttribute(name = "value", required = true)
    public final String getValue()
    {
        return value;
    }

    public final void setValue(String value)
    {
        this.value = value;
    }

    @XmlAttribute(name = "type")
    public final String getType()
    {
        return type;
    }

    public final void setType(String type)
    {
        this.type = type;
    }
    
}
