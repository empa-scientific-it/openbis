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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * {@link ModelData} for {@link MaterialType}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialTypeModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    public MaterialTypeModel(final MaterialType type)
    {
        this(type.getCode(), type);
        set(ModelDataPropertyNames.TOOLTIP,
                TooltipRenderer.renderAsTooltip(type.getCode(), type.getDescription()));
    }

    private static MaterialTypeModel createNone(String label)
    {
        return new MaterialTypeModel(label, null);
    }

    private MaterialTypeModel(String code, Object object)
    {
        set(ModelDataPropertyNames.CODE, code);
        set(ModelDataPropertyNames.OBJECT, object);
    }

    /** adds an additional option to the list with the specified label and null value */
    public final static List<MaterialTypeModel> convertWithAdditionalOption(
            final List<MaterialType> types, String additionalOptionLabel, boolean withTypeInFile)
    {
        final List<MaterialTypeModel> result = convert(types, withTypeInFile);
        result.add(0, createNone(additionalOptionLabel));
        return result;
    }

    public final static List<MaterialTypeModel> convert(final List<MaterialType> types,
            boolean withTypeInFile)
    {
        final List<MaterialTypeModel> result = new ArrayList<MaterialTypeModel>();
        for (final MaterialType st : types)
        {
            result.add(new MaterialTypeModel(st));
        }

        if (withTypeInFile && result.size() > 0)
        {
            result.add(0, createTypeInFileModel());
        }

        return result;
    }

    private static MaterialTypeModel createTypeInFileModel()
    {
        final MaterialType typeInFile = new MaterialType();
        typeInFile.setCode(EntityType.DEFINED_IN_FILE);
        return new MaterialTypeModel(typeInFile);
    }
}
