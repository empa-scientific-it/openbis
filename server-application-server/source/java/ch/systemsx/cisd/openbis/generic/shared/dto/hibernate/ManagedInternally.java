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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * <code>boolean</code> value has to be <code>false</code> for entities that must not be in the internal namespace.
 * 
 * @author Christian Ribeaud
 */
@Target(
{ METHOD, FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ManagedInternallyValidator.class)
public @interface ManagedInternally
{

    /**
     * Whether given entity must be located in the internal namespace or not.
     * <p>
     * Default is <code>false</code>.
     * </p>
     */
    boolean value() default false;

    String message() default "{validator.internalnamespace}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
