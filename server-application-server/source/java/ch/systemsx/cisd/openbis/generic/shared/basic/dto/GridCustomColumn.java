/*
 * Copyright 2009 ETH Zuerich, CISD
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

/**
 * Stores information describing the grid custom column.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumn extends AbstractExpression
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private DataTypeCode dataType;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setDataType(DataTypeCode dataType)
    {
        this.dataType = dataType;
    }

    public DataTypeCode getDataType()
    {
        return dataType;
    }

    @Override
    public String toString()
    {
        return code + "[" + dataType + "]";
    }

}
