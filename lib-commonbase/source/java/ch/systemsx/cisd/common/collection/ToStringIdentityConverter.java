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

package ch.systemsx.cisd.common.collection;

import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.collection.ToStringIdentityConverter;

/**
 * An identity {@link IToStringConverter} for {@link String}s.
 *
 * @author Bernd Rinn
 */
public class ToStringIdentityConverter implements IToStringConverter<String>
{

    private final static ToStringIdentityConverter instance = new ToStringIdentityConverter();

    private ToStringIdentityConverter()
    {
        // This is a singleton.
    }

    /**
     * @return The instance of the {@link ToStringIdentityConverter}.
     */
    public final static ToStringIdentityConverter getInstance()
    {
        return instance;
    }

    //
    // IToStringConverter
    //

    @Override
    public String toString(String value)
    {
        return value;
    }

}
