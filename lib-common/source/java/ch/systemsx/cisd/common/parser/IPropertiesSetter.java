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

/**
 * Used to construct object by setting its properties
 * 
 * @author Tomasz Pylak
 */
public interface IPropertiesSetter<T>
{
    /**
     * Sets the property specified by <code>name</code> to the specified value.
     */
    void setProperty(String name, String value);

    /** Creates an object of type <code>T</code>. Will be called after setting properties values. */
    T getConstructedObject();
}