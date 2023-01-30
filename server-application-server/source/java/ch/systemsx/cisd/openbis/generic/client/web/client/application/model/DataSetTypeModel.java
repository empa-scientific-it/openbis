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

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * {@link ModelData} for {@link DataSetType}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetTypeModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    public DataSetTypeModel(final DataSetType dataSetType)
    {
        this(dataSetType.getCode(), dataSetType);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(dataSetType.getCode(),
                dataSetType.getDescription()));
    }

    private DataSetTypeModel(String code, Object object)
    {
        set(ModelDataPropertyNames.CODE, code);
        set(ModelDataPropertyNames.OBJECT, object);
    }

    public final static List<DataSetTypeModel> convert(final List<DataSetType> dataSetTypes,
            boolean withAll, boolean withTypesInFile)
    {
        final List<DataSetTypeModel> result = new ArrayList<DataSetTypeModel>();
        for (final DataSetType st : dataSetTypes)
        {
            result.add(new DataSetTypeModel(st));
        }
        if (withTypesInFile && dataSetTypes.size() > 0)
        {
            result.add(0, createTypeInFileModel());
        }
        if (withAll)
        {
            result.add(0, createAllTypesModel());
        }
        return result;
    }

    private static DataSetTypeModel createTypeInFileModel()
    {
        final DataSetType typeInFile = new DataSetType();
        typeInFile.setCode(EntityType.DEFINED_IN_FILE);
        return new DataSetTypeModel(typeInFile);
    }

    private static DataSetTypeModel createAllTypesModel()
    {
        return new DataSetTypeModel(EntityType.ALL_TYPES_CODE, null);
    }

}
