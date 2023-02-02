/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;

/**
 * Fetches feature vectors of the specified wells from imaging db.
 * 
 * @author Tomasz Pylak
 */
public interface IHCSFeatureVectorLoader
{
    /**
     * Fetches all feature vectors of specified wells. Uses basic data types. If a reference to a dataset without any feature vectors is specified, it
     * is silently ignored.
     */
    WellFeatureCollection<FeatureVectorValues> fetchWellFeatureValuesIfPossible(Session session,
            List<WellFeatureVectorReference> references);

    /**
     * Fetches all features vectors from a specified dataset. Only features with given names will be fetched.<br>
     * Can be empty if a dataset contains no feature vectors.
     */
    WellFeatureCollection<FeatureVectorValues> fetchDatasetFeatureValues(Session session,
            List<String> datasetCodes, List<String> featureCodes);
}
