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

package ch.systemsx.cisd.openbis.plugin.screening.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;

/**
 * Bean for a row in a table of feature vectors. Each row is specified by data set code, plate identifier, well position and and array of feature
 * values.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureTableRow extends FeatureVectorValues
{
    private static final long serialVersionUID = IServer.VERSION;

    private FeatureVectorDatasetWellReference reference;

    private SampleIdentifier plateIdentifier;

    public FeatureTableRow(FeatureVectorValues featureVector)
    {
        super(featureVector);
    }

    public FeatureVectorDatasetWellReference getReference()
    {
        return reference;
    }

    public void setReference(FeatureVectorDatasetWellReference reference)
    {
        this.reference = reference;
    }

    // can be null if it is not set!
    public final SampleIdentifier getPlateIdentifier()
    {
        return plateIdentifier;
    }

    public final void setPlateIdentifier(SampleIdentifier plateIdentifier)
    {
        this.plateIdentifier = plateIdentifier;
    }
}
