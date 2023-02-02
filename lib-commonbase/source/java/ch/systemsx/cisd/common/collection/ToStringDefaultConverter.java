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

import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.collection.ToStringDefaultConverter;

/**
 * The default converter: uses {@link Object#toString()} for conversion.
 * 
 * @author Bernd Rinn
 */
public final class ToStringDefaultConverter implements IToStringConverter<Object>
{

    private final static ToStringDefaultConverter instance = new ToStringDefaultConverter();

    private ToStringDefaultConverter()
    {
        // This is a singleton.
    }

    /**
     * @return The instance of the {@link ToStringDefaultConverter}.
     */
    public final static ToStringDefaultConverter getInstance()
    {
        return instance;
    }

    //
    // IToStringConverter
    //

    @Override
    public final String toString(final Object value)
    {
        return String.valueOf(value);
    }

}
