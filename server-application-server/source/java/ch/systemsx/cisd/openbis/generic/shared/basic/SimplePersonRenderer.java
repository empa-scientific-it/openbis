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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Tomasz Pylak
 */
public class SimplePersonRenderer
{
    public final static StringBuilder createPersonName(final Person person)
    {
        final StringBuilder builder = new StringBuilder();
        if (person != null)
        {
            final String lastName = person.getLastName();
            final String firstName = person.getFirstName();
            final String userId = person.getUserId();
            if (isBlank(lastName) == false)
            {
                builder.append(lastName);
            }
            if (isBlank(firstName) == false)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(firstName);
            }
            if(isBlank(lastName) && isBlank(firstName)) {
            	builder.append(userId);
            }
        }
        return builder;
    }

    private static boolean isBlank(String text)
    {
        return text == null || text.length() == 0;
    }
}
