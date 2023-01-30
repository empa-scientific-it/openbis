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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * {@link ModelData} for {@link SampleType}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleTypeModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    public SampleTypeModel(final SampleType sampleType)
    {
        set(ModelDataPropertyNames.CODE, sampleType.getCode());
        set(ModelDataPropertyNames.OBJECT, sampleType);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(sampleType.getCode(),
                sampleType.getDescription()));
    }

    public final static List<SampleTypeModel> convert(final List<SampleType> sampleTypes,
            final boolean onlyListable, final boolean withAll, final boolean withTypeInFile)
    {
        final List<SampleTypeModel> result = new ArrayList<SampleTypeModel>();
        final List<SampleType> filteredTypes = filter(sampleTypes, onlyListable);

        for (final SampleType sampleType : filteredTypes)
        {
            result.add(new SampleTypeModel(sampleType));
        }
        if (withAll && filteredTypes.size() > 0)
        {
            result.add(0, createAllTypesModel(filteredTypes));
        }
        if (withTypeInFile && filteredTypes.size() > 0)
        {
            result.add(0, createTypeInFileModel());
        }
        return result;
    }

    private static List<SampleType> filter(final List<SampleType> sampleTypes,
            final boolean onlyListable)
    {
        final List<SampleType> result = new ArrayList<SampleType>();

        for (final SampleType sampleType : sampleTypes)
        {
            if (onlyListable && sampleType.isListable() == false)
            {
                continue;
            }
            result.add(sampleType);
        }
        return result;
    }

    private static SampleTypeModel createAllTypesModel(List<SampleType> basicTypes)
    {
        final SampleType allSampleType = SampleType.createAllSampleType(basicTypes, true);
        return new SampleTypeModel(allSampleType);
    }

    private static SampleTypeModel createTypeInFileModel()
    {
        final SampleType typeInFile = new SampleType();
        typeInFile.setCode(EntityType.DEFINED_IN_FILE);
        typeInFile.setListable(false);
        return new SampleTypeModel(typeInFile);
    }

}
