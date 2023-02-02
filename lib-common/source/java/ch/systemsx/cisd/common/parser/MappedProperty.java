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

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A simple <code>IPropertyModel</code> implementation that acts like a bean.
 * <p>
 * <code>column</code> and <code>name</code> are mandatory and are set in the constructor.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class MappedProperty implements IPropertyModel
{

    private final int column;

    private final String name;

    private String format;

    MappedProperty(final int column, final String name)
    {
        this.column = column;
        this.name = name;
    }

    public final void setFormat(String format)
    {
        this.format = format;
    }

    //
    // IPropertyModel
    //

    @Override
    public final String getFormat()
    {
        return format;
    }

    @Override
    public final int getColumn()
    {
        return column;
    }

    @Override
    public final String getCode()
    {
        return name;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}