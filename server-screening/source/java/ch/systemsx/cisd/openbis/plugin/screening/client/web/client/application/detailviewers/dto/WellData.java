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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores information about well metadata, may be extended in future.
 * 
 * @author Tomasz Pylak
 */
public class WellData
{
    private final WellLocation wellLocation;

    private final Experiment experiment;

    private WellMetadata wellMetadataOrNull;

    private boolean fullyLoaded = false;

    // ordered map from feature labels to feature values
    // NOTE: it contains a subset of all feature values of a well (only the ones that were loaded)
    private Map<String /* feature label */, FeatureValue> featureValuesMap =
            new LinkedHashMap<String, FeatureValue>();

    public WellData(WellLocation wellLocation, Experiment experiment)
    {
        this.wellLocation = wellLocation;
        this.experiment = experiment;
    }

    public void addFeatureValue(String featureName, FeatureValue value)
    {
        featureValuesMap.put(featureName, value);
    }

    public void setAllFeatureValues(Map<String, FeatureValue> values)
    {
        resetFeatureValues();
        for (Entry<String, FeatureValue> entry : values.entrySet())
        {
            addFeatureValue(entry.getKey(), entry.getValue());
        }
        fullyLoaded = true;
    }

    public void resetFeatureValues()
    {
        featureValuesMap.clear();
        fullyLoaded = false;
    }

    public FeatureValue tryGetFeatureValue(String featureLabel)
    {
        return featureValuesMap.get(featureLabel);
    }

    // ordered set of feature labels for which we have loaded feature values
    public Set<String> getFeatureLabels()
    {
        return featureValuesMap.keySet();
    }

    public void setMetadata(WellMetadata well)
    {
        this.wellMetadataOrNull = well;
    }

    public WellMetadata tryGetMetadata()
    {
        return wellMetadataOrNull;
    }

    public WellLocation getWellLocation()
    {
        return wellLocation;
    }

    public Experiment getExperiment()
    {
        return experiment;
    }

    public boolean isFullyLoaded()
    {
        return fullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded)
    {
        this.fullyLoaded = fullyLoaded;
    }
}