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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Abstract super class of {@link SearchDomain} results based of openBIS entities.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("AbstractEntitySearchResultLocation")
public class AbstractEntitySearchResultLocation implements ISearchDomainResultLocation
{
    private static final long serialVersionUID = 1L;

    private EntityKind entityKind;

    private String entityType;

    private String code;

    private String permId;

    private int position;

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    protected String appendToString()
    {
        return "position: " + position;
    }

    protected String renderEntityKind()
    {
        String str = getEntityKind().toString().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

}
