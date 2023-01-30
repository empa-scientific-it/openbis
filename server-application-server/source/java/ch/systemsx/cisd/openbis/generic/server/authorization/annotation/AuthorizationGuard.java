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

package ch.systemsx.cisd.openbis.generic.server.authorization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * An annotation for marking method parameters that should be evaluated.
 * 
 * @author Christian Ribeaud
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
public @interface AuthorizationGuard
{
    /**
     * Class responsible for evaluating the method parameter.
     */
    Class<? extends IPredicate<?>> guardClass();

    /**
     * List of roles replacing corresponding list of @RolesAllowed annotation.
     */
    RoleWithHierarchy[] rolesAllowed() default {};

    /**
     * Name of the guard. Needed for the capabilities file to override allowed roles list.
     */
    String name() default "";
}
