/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.DataSetUniquePropertyViolationException;
import org.springframework.dao.DataAccessException;

public final class DataSetDataAccessExceptionTranslator
{
    private static final String PROPERTY_VALUE_CONSTRAINT_NAME = "data_set_properties_unique_value";

    private static final String PROPERTY_SAMPLE_VALUE_CONSTRAINT_NAME =
            "data_set_properties_unique_samp";

    private static final String PROPERTY_VOCAB_VALUE_CONSTRAINT_NAME =
            "data_set_properties_unique_cvte";

    public static void translateAndThrow(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            if (isUniquePropertyViolationException(message))
            {
                throw new DataSetUniquePropertyViolationException(message.getCode(1));
            } else if (isUniqueSamplePropertyViolationException(message))
            {
                throw new DataSetUniquePropertyViolationException(message.getCode(1), "sample");
            } else if (isUniqueVocabPropertyViolationException(message))
            {
                throw new DataSetUniquePropertyViolationException(message.getCode(1),
                        "controlled vocabulary");
            } else
            {
                throw exception;
            }
        } else
        {
            throw exception;
        }

    }

    public static boolean isUniquePropertyViolationException(UniqueViolationMessage message)
    {
        return PROPERTY_VALUE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static boolean isUniqueSamplePropertyViolationException(UniqueViolationMessage message)
    {
        return PROPERTY_SAMPLE_VALUE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static boolean isUniqueVocabPropertyViolationException(UniqueViolationMessage message)
    {
        return PROPERTY_VOCAB_VALUE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

}
