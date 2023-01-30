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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * A {@link ModelData} implementation for {@link Person}.
 * 
 * @author Izabela Adamczyk
 */
public class PersonModel extends SimplifiedBaseModel
{

    private static final long serialVersionUID = 1L;

    public PersonModel()
    {
    }

    public PersonModel(final Person person)
    {
        set(ModelDataPropertyNames.CODE, person.getUserId());
        set(ModelDataPropertyNames.OBJECT, person);
    }

    public final static List<PersonModel> convert(final List<Person> groups)
    {
        final List<PersonModel> result = new ArrayList<PersonModel>();
        for (final Person g : groups)
        {
            result.add(new PersonModel(g));
        }
        return result;
    }

    public final Person getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }
}
