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

import ch.systemsx.cisd.common.collection.FromStringIdentityConverter;
import ch.systemsx.cisd.common.collection.IFromStringConverter;

/**
 * The identity {@link IFromStringConverter} for {@link String}s, which returns the value itself as the converted value. This class is a singleton.
 * 
 * @author Bernd Rinn
 */
public final class FromStringIdentityConverter implements IFromStringConverter<String>
{

    private static final FromStringIdentityConverter instance = new FromStringIdentityConverter();

    private FromStringIdentityConverter()
    {
        // This is a singleton.
    }

    @Override
    public String fromString(String value)
    {
        return value;
    }

    /**
     * @return The instance of the {@link FromStringIdentityConverter}.
     */
    public static FromStringIdentityConverter getInstance()
    {
        return instance;
    }

}
