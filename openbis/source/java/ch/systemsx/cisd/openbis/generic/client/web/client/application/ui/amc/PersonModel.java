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

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * {@link ModelData} for {@link Person}.
 * 
 * @author Izabela Adamczyk
 */
public class PersonModel extends BaseModelData
{

    static final String ROLES = "roles";

    static final String EMAIL = "email";

    static final String LAST_NAME = "lastName";

    static final String FIRST_NAME = "firstName";

    static final String REGISTRATION_DATE = "registrationDate";

    static final String REGISTRATOR = "registrator";

    static final String USER_ID = "userId";

    static final long serialVersionUID = 1L;

    public PersonModel()
    {
    }

    public PersonModel(Person p)
    {
        set(USER_ID, p.getUserId());
        set(REGISTRATOR, p.getRegistrator());
        set(REGISTRATION_DATE, p.getRegistrationDate());
        set(FIRST_NAME, p.getFirstName());
        set(LAST_NAME, p.getLastName());
        set(EMAIL, p.getEMail());
        set(ROLES, new ArrayList<RoleAssignment>());
    }
}
