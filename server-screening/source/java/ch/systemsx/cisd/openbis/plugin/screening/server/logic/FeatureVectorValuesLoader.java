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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * @author Kaloyan Enimanev
 */
public class FeatureVectorValuesLoader
{

    public static FeatureVectorValues loadFeatureVectorValues(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String datasetCode,
            String datastoreCode, WellLocation wellLocation)
    {
        IHCSFeatureVectorLoader loader =
                businessObjectFactory.createHCSFeatureVectorLoader(datastoreCode);

        List<WellFeatureVectorReference> wellReferences =
                Arrays.asList(new WellFeatureVectorReference(datasetCode, wellLocation));
        WellFeatureCollection<FeatureVectorValues> featureVectors =
                loader.fetchWellFeatureValuesIfPossible(session, wellReferences);

        List<FeatureVectorValues> features = featureVectors.getFeatures();
        if (features.size() == 0)
        {
            // Because of the way we are storing the features it can happen only if dataset contains
            // no features (NaN are stored in the plate matrix for the wells which have no value
            // specified).
            return null;
        } else
        {
            return featureVectors.getFeatures().get(0);
        }
    }

}
