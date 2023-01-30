/*
 * Copyright 2008 ETH Zuerich, CISD
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
package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Criteria for listing persons.
 * 
 * @author Izabela Adamczyk
 */
public final class ListPersonsCriteria extends DefaultResultSetConfig<String, TableModelRowWithObject<Person>> implements
        IsSerializable
{
    // If not null, persons form the authorization group will be listed.
    // Otherwise all persons are listed.
    private TechId authorizationGroupId;

    public ListPersonsCriteria()
    {
    }

    public ListPersonsCriteria(AuthorizationGroup group)
    {
        setAuthorizationGroupId(TechId.create(group));
    }

    public TechId getAuthorizationGroupId()
    {
        return authorizationGroupId;
    }

    public void setAuthorizationGroupId(TechId authorizationGroupId)
    {
        this.authorizationGroupId = authorizationGroupId;
    }

}
