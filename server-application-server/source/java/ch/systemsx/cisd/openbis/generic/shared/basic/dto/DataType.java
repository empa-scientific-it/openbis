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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * A class that represents a data type.
 * 
 * @author Izabela Adamczyk
 */
public final class DataType implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DataTypeCode code;

    private String description;

    public DataType()
    {
    }

    public DataType(final DataTypeCode code)
    {
        this.code = code;
    }

    public DataTypeCode getCode()
    {
        return code;
    }

    public void setCode(final DataTypeCode code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

}
