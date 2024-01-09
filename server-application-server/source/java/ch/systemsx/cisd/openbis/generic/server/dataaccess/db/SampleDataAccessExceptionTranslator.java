/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniquePropertyViolationException;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueSubcodeViolationException;

/**
 * Extracts information about an actual cause of sample related DataAccessException.
 * 
 * @author pkupczyk
 */
public class SampleDataAccessExceptionTranslator
{

    private static final String CODE_CONSTRAINT_NAME = "samp_code_unique_check_uk";

    private static final String PROPERTY_CONSTRAINT_NAME = "sample_properties_unique_value";

    private static final String SUBCODE_CONSTRAINT_NAME = "samp_subcode_unique_check_uk";



    public static void translateAndThrow(DataAccessException exception)
    {
        if (isUniqueCodeViolationException(exception))
        {
            throwUniqueCodeViolationException(exception);
        } else if (isUniqueSubcodeViolationException(exception))
        {
            throwUniqueSubcodeViolationException(exception);
        } else if (isUniquePropertyViolationException(exception))
        {
            throwUniquePropertyViolationException(exception);
        } else
        {
            throw exception;
        }
    }

    public static boolean isUniqueCodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        return message != null
                && CODE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static boolean isUniqueSubcodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        return message != null
                && SUBCODE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static boolean isUniquePropertyViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        return message != null
                && PROPERTY_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static void throwUniqueCodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            throw new SampleUniqueCodeViolationException(message.getCode());
        }
    }

    public static void throwUniqueSubcodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            throw new SampleUniqueSubcodeViolationException(message.getCode());
        }
    }

    public static void throwUniquePropertyViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            throw new SampleUniquePropertyViolationException(message.getCode(1));
        }
    }


}
