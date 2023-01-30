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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.common.reflection.MethodUtils;

/**
 * Utility class containing methods useful in defining equals and hashCode methods.
 * 
 * @author Izabela Adamczyk
 */
public final class EqualsHashUtils
{
    private EqualsHashUtils()
    {
        // Can not be instantiated.
    }

    public static void assertDefined(final Object o, final String name)
    {
        assert name != null : "Unspecified name.";
        if (o == null)
        {
            final String className =
                    MethodUtils.getMethodOnStack(2).getDeclaringClass().getSimpleName();
            throw new IllegalStateException(String.format(
                    "Field name '%s' in class '%s' should be defined but is null."
                            + " Equals operation cannot be performed.", name, className));
        }
    }
}
