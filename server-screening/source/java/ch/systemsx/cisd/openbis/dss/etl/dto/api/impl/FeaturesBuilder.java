/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Allows to define feature vectors of one image analysis dataset.
 * 
 * @author Tomasz Pylak
 */
public class FeaturesBuilder implements IFeaturesBuilder
{
    private final List<FeatureDefinition> featureDefinitionValuesList;

    public FeaturesBuilder()
    {
        this.featureDefinitionValuesList = new ArrayList<FeatureDefinition>();
    }

    /** Defines a container to which values of the feature for each well can be added. */
    @Override
    public IFeatureDefinition defineFeature(String featureCode)
    {
        assert StringUtils.isBlank(featureCode) == false : "Feature code is blank " + featureCode;
        FeatureDefinition featureDefinitionValues =
                new FeatureDefinition(createFeatureDefinition(featureCode));
        featureDefinitionValuesList.add(featureDefinitionValues);
        return featureDefinitionValues;
    }

    private static ImgFeatureDefDTO createFeatureDefinition(String featureCode)
    {
        ImgFeatureDefDTO dto = new ImgFeatureDefDTO();
        dto.setCode(featureCode);
        dto.setLabel(featureCode);
        return dto;
    }

    public List<FeatureDefinition> getFeatureDefinitionValuesList()
    {
        return featureDefinitionValuesList;
    }
}
