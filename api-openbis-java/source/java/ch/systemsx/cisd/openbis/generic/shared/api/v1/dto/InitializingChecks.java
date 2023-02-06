/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

/**
 * Utility methods to validate input upon construction of API objects.
 * 
 * @author Kaloyan Enimanev
 */
class InitializingChecks
{
    static void checkValidString(String string, String message) throws IllegalArgumentException
    {
        if (string == null || string.length() == 0)
        {
            throw new IllegalArgumentException(message);
        }
    }

    static void checkValidLong(Long longValue, String message) throws IllegalArgumentException
    {
        if (longValue == null || longValue == 0)
        {
            throw new IllegalArgumentException(message);
        }
    }

    static void checkValidRegistrationDetails(EntityRegistrationDetails details, String message)
            throws IllegalArgumentException
    {
        if (details == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

}
