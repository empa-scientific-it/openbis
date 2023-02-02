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

package ch.systemsx.cisd.common.reflection;

/**
 * A filter for {@link Class}.
 * 
 * @author Christian Ribeaud
 */
public interface IClassFilter
{
    /**
     * Tests whether or not the specified <var>clazz</var> should be included.
     */
    public boolean accept(final Class<?> clazz);

    /**
     * Returns <code>true</code> if the specified class is accepted. This method is call before {@link #accept(Class)}. It should return
     * <code>false</code> if the class can not be loaded. If <code>true</code> is returned also {@link #accept(Class)} will be invoked which will give
     * the definite answer.
     */
    public boolean accept(String fullyQualifiedClassName);
}
