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

package ch.systemsx.cisd.openbis.common.conversation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All methods that support service conversations communication should be marked with this annotation. If a method without this annotation is called
 * via service conversations then {@link java.lang.NoSuchMethodException NoSuchMethodException} is thrown.
 * 
 * @author pkupczyk
 */

@Target(
{ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Conversational
{

    /**
     * Type of the progress reporting that should be used for the annotated method.
     */
    Progress progress();

}
