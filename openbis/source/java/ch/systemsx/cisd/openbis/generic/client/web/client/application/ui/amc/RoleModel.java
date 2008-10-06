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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * {@link ModelData} for {@link RoleAssignment}.
 * 
 * @author Izabela Adamczyk
 */
public class RoleModel extends BaseModelData
{
    static final String GROUP = "group";

    static final String PERSON = "person";

    static final String ROLE = "role";

    static final String INSTANCE = "instance";

    static final long serialVersionUID = 1L;

    public RoleModel(RoleAssignment role)
    {
        set(GROUP, role.getGroup() != null ? role.getGroup().getCode() : "");
        set(PERSON, role.getPerson().getUserId());
        set(ROLE, role.getCode());
        set(INSTANCE, role.getInstance() != null ? role.getInstance().getCode() : "");
    }

}
