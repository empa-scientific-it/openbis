/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation all JSON transfered classes should be marked with. It was originally created to
 * replace @JsonTypeName annotation which was not visible in the documentation. With @JsonTypeName
 * annotation it would be impossible for our users to find a logical type name of a class to be sent
 * to JSON-RPC services.
 * 
 * @author pkupczyk
 */
@Target(
    { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonObject
{
    /**
     * Logical type name for annotated type.
     */
    public String value();
}
