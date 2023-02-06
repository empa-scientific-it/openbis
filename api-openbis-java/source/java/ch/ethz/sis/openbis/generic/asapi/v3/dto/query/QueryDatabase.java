/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.query;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.query.QueryDatabase")
public class QueryDatabase implements Serializable, IPermIdHolder, ISpaceHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private QueryDatabaseFetchOptions fetchOptions;

    @JsonProperty
    private QueryDatabaseName permId;

    @JsonProperty
    private String name;

    @JsonProperty
    private String label;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Role creatorMinimalRole;

    @JsonProperty
    private RoleLevel creatorMinimalRoleLevel;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public QueryDatabaseFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(QueryDatabaseFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public QueryDatabaseName getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(QueryDatabaseName permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getName()
    {
        return name;
    }

    // Method automatically generated with DtoGenerator
    public void setName(String name)
    {
        this.name = name;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    // Method automatically generated with DtoGenerator
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Space getSpace()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpace())
        {
            return space;
        }
        else
        {
            throw new NotFetchedException("Space has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSpace(Space space)
    {
        this.space = space;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Role getCreatorMinimalRole()
    {
        return creatorMinimalRole;
    }

    // Method automatically generated with DtoGenerator
    public void setCreatorMinimalRole(Role creatorMinimalRole)
    {
        this.creatorMinimalRole = creatorMinimalRole;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public RoleLevel getCreatorMinimalRoleLevel()
    {
        return creatorMinimalRoleLevel;
    }

    // Method automatically generated with DtoGenerator
    public void setCreatorMinimalRoleLevel(RoleLevel creatorMinimalRoleLevel)
    {
        this.creatorMinimalRoleLevel = creatorMinimalRoleLevel;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "QueryDatabase " + name;
    }

}
