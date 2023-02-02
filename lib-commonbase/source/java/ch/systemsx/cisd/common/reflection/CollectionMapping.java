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

package ch.systemsx.cisd.common.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * An annotation that defines the type of collection and the type of elements to use when creating the collection in a bean context.
 * 
 * @author Bernd Rinn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface CollectionMapping
{
    /**
     * The concrete class to use as a collection.
     */
    @SuppressWarnings({ "rawtypes" })
    // No way to avoid the warning since the compiler doesn't accept something like
    // ArrayList<String>.class
    Class<? extends Collection> collectionClass();

    /**
     * The class to use as the elements of the collection (since the generics type isn't known at run time).
     */
    Class<?> elementClass();
}
