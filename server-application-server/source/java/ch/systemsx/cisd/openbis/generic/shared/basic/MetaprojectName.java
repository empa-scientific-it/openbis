/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Metaproject name representation. The name cannot be null, empty or contain white spaces, commas, slashes or backslashes.
 * 
 * @author pkupczyk
 */
public class MetaprojectName
{

    private static final Pattern PATTERN = Pattern.compile(MetaprojectNameConstants.PATTERN,
            Pattern.CASE_INSENSITIVE);

    public static void validate(String name)
    {
        if (name == null)
        {
            throw new UserFailureException("Metaproject name cannot be null");
        }
        if (name.isEmpty())
        {
            throw new UserFailureException("Metaproject name cannot be empty");
        }
        if (name.length() > MetaprojectNameConstants.MAX_LENGTH)
        {
            throw new UserFailureException(MetaprojectNameConstants.MAX_LENGTH_ERROR_MESSAGE);
        }
        if (PATTERN.matcher(name).matches() == false)
        {
            throw new UserFailureException(MetaprojectNameConstants.PATTERN_ERROR_MESSAGE);
        }
    }

}
