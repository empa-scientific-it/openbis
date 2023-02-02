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

package ch.systemsx.cisd.common.collection;

/**
 * This converter represents a role that can convert a <var>String</var> to some other <var>Object</var>.
 * 
 * @author Bernd Rinn
 */
public interface IToStringConverter<T>
{

    /** Returns the converted value of the <var>T</var> <var>value</var> to {@link String}. */
    public String toString(T value);

}
