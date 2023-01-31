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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ValidationError
{
    public static enum ValidationErrorTarget
    {
        DATA_SET_TYPE, DATA_SET_OWNER, DATA_SET_PROPERTY, DATA_SET_FILE
    }

    public static ValidationError createDataSetTypeValidationError(String errorMessage)
    {
        return new ValidationError(ValidationErrorTarget.DATA_SET_TYPE, null, errorMessage);
    }

    public static ValidationError createOwnerValidationError(String errorMessage)
    {
        return new ValidationError(ValidationErrorTarget.DATA_SET_OWNER, null, errorMessage);
    }

    public static ValidationError createPropertyValidationError(String propertyCode,
            String errorMessage)
    {
        return new ValidationError(ValidationErrorTarget.DATA_SET_PROPERTY, propertyCode,
                errorMessage);
    }

    public static ValidationError createFileValidationError(String errorMessage)
    {
        return new ValidationError(ValidationErrorTarget.DATA_SET_FILE, null, errorMessage);
    }

    private final ValidationErrorTarget target;

    // Only non-null on property targets
    private final String propertyCodeOrNull;

    private final String errorMessage;

    // Private Constructor -- use factory methods
    private ValidationError(ValidationErrorTarget target, String propertyCodeOrNull,
            String errorMessage)
    {
        this.target = target;
        this.propertyCodeOrNull = propertyCodeOrNull;
        this.errorMessage = errorMessage;
    }

    public ValidationErrorTarget getTarget()
    {
        return target;
    }

    public String getPropertyCodeOrNull()
    {
        return propertyCodeOrNull;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(target);
        builder.append(propertyCodeOrNull);
        builder.append(errorMessage);
        return builder.toString();
    }

}
