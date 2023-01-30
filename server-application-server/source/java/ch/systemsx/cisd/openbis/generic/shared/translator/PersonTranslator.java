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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PersonAdapter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link Person} &lt;---&gt; {@link PersonPE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public class PersonTranslator
{
    private PersonTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Person> translate(final Collection<PersonPE> persons)
    {
        final List<Person> result = new ArrayList<Person>();
        for (final PersonPE person : persons)
        {
            result.add(PersonTranslator.translate(person, true));
        }
        return result;
    }

    public final static Person translate(final PersonPE person)
    {
        return translate(person, false);
    }

    private final static Person translate(final PersonPE person, final boolean recursively)
    {
        if (person == null)
        {
            return null;
        }
        final Person result = new Person();
        result.setFirstName(person.getFirstName());
        result.setLastName(person.getLastName());
        result.setEmail(person.getEmail());
        result.setUserId(person.getUserId());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setRegistrationDate(person.getRegistrationDate());
        result.setActive(person.isActive());
        if (recursively)
        {
            result.setRegistrator(PersonTranslator.translate(person.getRegistrator(), false));
        }

        return result;
    }

    public final static IPerson translateToIPerson(final PersonPE personPE)
    {
        if (personPE == null)
        {
            return null;
        }

        return new PersonAdapter(personPE.getUserId(), personPE.getFirstName(),
                personPE.getLastName());
    }

}
