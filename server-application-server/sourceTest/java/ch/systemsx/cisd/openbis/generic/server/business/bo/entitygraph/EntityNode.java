/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

public class EntityNode implements IIdHolder, Comparable<EntityNode>
{
    private final long id;

    private final String code;

    private String type;

    EntityNode(String codePrefix, long id)
    {
        this.id = id;
        code = codePrefix + id;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

    public String getIdentifier()
    {
        return getCode();
    }

    public String getType()
    {
        return type;
    }

    public String getIdentifierAndType()
    {
        return getCodeAndType();
    }

    public String getCodeAndType()
    {
        StringBuilder builder = new StringBuilder(code);
        if (type != null)
        {
            builder.append("[").append(type).append("]");
        }
        return builder.toString();
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public int compareTo(EntityNode that)
    {
        return this.getCode().compareTo(that.getCode());
    }

    @Override
    public String toString()
    {
        return getIdentifierAndType();
    }

}