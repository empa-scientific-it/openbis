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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * {@link ModelData} for {@link Person}.
 * 
 * @author Izabela Adamczyk
 */
public class PersonModel extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    public PersonModel()
    {
    }

    public PersonModel(final Person p)
    {
        set(ModelDataPropertyNames.USER_ID, p.getUserId());
        set(ModelDataPropertyNames.REGISTRATOR, p.getRegistrator());
        set(ModelDataPropertyNames.REGISTRATION_DATE, p.getRegistrationDate());
        set(ModelDataPropertyNames.FIRST_NAME, p.getFirstName());
        set(ModelDataPropertyNames.LAST_NAME, p.getLastName());
        set(ModelDataPropertyNames.EMAIL, p.getEmail());
        set(ModelDataPropertyNames.ROLES, new ArrayList<RoleAssignment>());
    }
}
