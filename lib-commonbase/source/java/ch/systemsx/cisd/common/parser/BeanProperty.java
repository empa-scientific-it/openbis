/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a setter method in a <i>Java</i> bean class that should be considered by the parser.
 * <p>
 * By default, each property annotated with <code>BeanProperty</code> is mandatory.
 * </p>
 * 
 * @see ch.systemsx.cisd.common.parser.IParser
 * @author Christian Ribeaud
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeanProperty
{
    /**
     * Whether this bean property is optional or not.
     * <p>
     * Default is <code>false</code> meaning that any field set by annotated method is mandatory.
     * </p>
     */
    public boolean optional() default false;

    /**
     * Static label (or alias) for this annotated field.
     * <p>
     * This <b>must</b> not be empty (as the parser works with labelled methods).
     * </p>
     */
    public String label() default "";
}
