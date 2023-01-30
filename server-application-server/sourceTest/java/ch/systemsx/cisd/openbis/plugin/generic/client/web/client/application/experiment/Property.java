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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import junit.framework.Assert;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Extends {@link AbstractProperty} with methods for {@link Person}
 * 
 * @author Franz-Josef Elmer
 */
public class Property extends AbstractProperty<CheckExperiment>
{
    public Property(final String key, final CheckExperiment checker)
    {
        super(key, checker);
    }

    public CheckExperiment asPerson(final String personAsString)
    {
        return by(new IValueAssertion<Person>()
            {
                @Override
                public void assertValue(final Person value)
                {
                    String actualName = value.getLastName() + ", " + value.getFirstName();
                    Assert.assertEquals(message, personAsString, actualName);
                }
            });
    }

}
