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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;

/**
 * {@link ModelData} for {@link ExperimentType}.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentTypeModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    public ExperimentTypeModel(final ExperimentType experimentType)
    {
        set(ModelDataPropertyNames.CODE, experimentType.getCode());
        set(ModelDataPropertyNames.OBJECT, experimentType);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(experimentType
                .getCode(), experimentType.getDescription()));
    }

    public final static List<ExperimentTypeModel> convert(
            final List<ExperimentType> experimentTypes, final boolean withAll)
    {
        final List<ExperimentTypeModel> result = new ArrayList<ExperimentTypeModel>();
        for (final ExperimentType st : experimentTypes)
        {
            result.add(new ExperimentTypeModel(st));
        }
        if (withAll && experimentTypes.size() > 0)
        {
            result.add(0, createAllTypesModel(experimentTypes));
        }
        return result;
    }

    private static ExperimentTypeModel createAllTypesModel(List<ExperimentType> basicTypes)
    {
        final ExperimentType allExperimentType = new ExperimentType();
        allExperimentType.setCode(EntityType.ALL_TYPES_CODE);

        Set<ExperimentTypePropertyType> allPropertyTypes =
                new HashSet<ExperimentTypePropertyType>();
        for (ExperimentType basicType : basicTypes)
        {
            allPropertyTypes.addAll(basicType.getAssignedPropertyTypes());
        }
        allExperimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>(
                allPropertyTypes));

        return new ExperimentTypeModel(allExperimentType);
    }

}
