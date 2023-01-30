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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;

/**
 * A {@link BaseModelData} extension suitable for {@link DataType}.
 * 
 * @author Christian Ribeaud
 */
public final class DataTypeModel extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    public DataTypeModel(final DataType dataType)
    {
        assert dataType != null : "Unspecified data type.";
        set(ModelDataPropertyNames.CODE, dataType.getCode().name());
        set(ModelDataPropertyNames.OBJECT, dataType);
    }

    public DataType getDataType()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

    public final static List<DataTypeModel> convert(final List<DataType> dataTypes)
    {
        assert dataTypes != null : "Unspecified data types.";
        final List<DataTypeModel> dataTypeModels = new ArrayList<DataTypeModel>(dataTypes.size());
        for (final DataType dataType : dataTypes)
        {
            dataTypeModels.add(new DataTypeModel(dataType));
        }
        return dataTypeModels;
    }

}
