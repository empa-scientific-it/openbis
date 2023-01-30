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

import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * Some utility methods based on {@link UUID}.
 * <p>
 * In its canonical form, a <i>UUID</i> consists of 32 hexadecimal digits, displayed in 5 groups separated by hyphens, in the form 8-4-4-4-12 for a
 * total of 36 characters.<br>
 * For example:
 * 
 * <pre>
 * 550e8400-e29b-41d4-a716-446655440000
 * </pre>
 * 
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class UuidUtil
{

    /** Regular expression pattern for <i>UUID</i>. */
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);

    private UuidUtil()
    {
        // Can not be instantiated.
    }

    /** generates canonical string representation of the random UUID */
    public static final String generateUUID()
    {
        return CodeConverter.tryToDatabase(UUID.randomUUID().toString());
    }

    /** @return true if the parameter is a valid canonical string representation of the UUID */
    public static final boolean isValidUUID(final String uuid)
    {
        assert uuid != null : "Unspecified UUID";
        final String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(uuid, "-");
        if (split.length != 5)
        {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }
}
