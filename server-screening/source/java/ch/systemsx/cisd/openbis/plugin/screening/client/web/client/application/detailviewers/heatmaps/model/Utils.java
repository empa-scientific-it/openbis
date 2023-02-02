/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;

/**
 * @author Franz-Josef Elmer
 */
public class Utils
{
    public static Float tryAsFloatFeature(WellData well, final String featureLabel)
    {
        FeatureValue value = well.tryGetFeatureValue(featureLabel);
        return value != null ? value.asFloat() : null;
    }

    public static String tryAsVocabularyFeature(WellData well, final String featureLabel)
    {
        FeatureValue value = well.tryGetFeatureValue(featureLabel);
        return value != null ? value.tryAsVocabularyTerm() : null;
    }

}
