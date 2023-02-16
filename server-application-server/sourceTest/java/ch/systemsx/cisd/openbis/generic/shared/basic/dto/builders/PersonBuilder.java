/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Builder of a {@link Person} instance.
 * 
 * @author Franz-Josef Elmer
 */
public class PersonBuilder
{
    private final Person person = new Person();
    {
        person.setActive(true);
    }

    public PersonBuilder userID(String userID)
    {
        person.setUserId(userID);
        return this;
    }

    public PersonBuilder name(String firstName, String lastName)
    {
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return this;
    }

    public PersonBuilder email(String email)
    {
        person.setEmail(email);
        return this;
    }

    public PersonBuilder registrator(Person registrator)
    {
        person.setRegistrator(registrator);
        return this;
    }

    public PersonBuilder registrationDate(Date registrationDate)
    {
        person.setRegistrationDate(registrationDate);
        return this;
    }

    public final Person getPerson()
    {
        return person;
    }

}
