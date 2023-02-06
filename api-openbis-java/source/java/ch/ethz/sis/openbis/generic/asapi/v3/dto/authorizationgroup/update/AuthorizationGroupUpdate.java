/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.authorizationgroup.update.AuthorizationGroupUpdate")
public class AuthorizationGroupUpdate implements IUpdate, IObjectUpdate<IAuthorizationGroupId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IAuthorizationGroupId groupId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private IdListUpdateValue<IPersonId> userIds = new IdListUpdateValue<IPersonId>();

    @Override
    @JsonIgnore
    public IAuthorizationGroupId getObjectId()
    {
        return getAuthorizationGroupId();
    }

    @JsonIgnore
    public IAuthorizationGroupId getAuthorizationGroupId()
    {
        return groupId;
    }

    @JsonIgnore
    public void setAuthorizationGroupId(IAuthorizationGroupId authorizationGroupId)
    {
        this.groupId = authorizationGroupId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public IdListUpdateValue<IPersonId> getUserIds()
    {
        return userIds;
    }

    @JsonIgnore
    public void setUserIdActions(List<ListUpdateAction<IPersonId>> actions)
    {
        userIds.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("groupId", groupId).toString();
    }

}
