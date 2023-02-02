/*
 * Copyright 2011 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ANALYSIS_PROCEDURE;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeatureDefinition;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Extends {@link DataSetInformation} with information about images analysis on the well level (relevant for HCS).
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorDataSetInformation extends DataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private List<FeatureDefinition> features;

    public FeatureVectorDataSetInformation()
    {
        this.features = new ArrayList<FeatureDefinition>();
    }

    public void setAnalysisProcedure(String analysisProcedure)
    {
        getDataSetProperties().add(new NewProperty(ANALYSIS_PROCEDURE, analysisProcedure));
    }

    public List<FeatureDefinition> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<FeatureDefinition> features)
    {
        this.features = features;
    }

    /** are all necessary fields filled? */
    public boolean isNotEmpty()
    {
        return features != null && features.size() > 0;
    }

    /** Defines a container to which values of the feature for each well can be added. */
    public IFeatureDefinition defineFeature(String featureCode)
    {
        assert StringUtils.isBlank(featureCode) == false : "Feature code is blank " + featureCode;
        FeatureDefinition featureDefinitionValues =
                new FeatureDefinition(createFeatureDefinition(featureCode));
        features.add(featureDefinitionValues);
        return featureDefinitionValues;
    }

    private static ImgFeatureDefDTO createFeatureDefinition(String featureCode)
    {
        ImgFeatureDefDTO dto = new ImgFeatureDefDTO();
        dto.setCode(featureCode);
        dto.setLabel(featureCode);
        return dto;
    }

}
