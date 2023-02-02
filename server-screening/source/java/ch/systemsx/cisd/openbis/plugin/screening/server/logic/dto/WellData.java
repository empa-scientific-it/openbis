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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.Arrays;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;

/**
 * The simplest implementation of {@Link IWellData}.
 * 
 * @author Tomasz Pylak
 */
public class WellData implements IWellData
{
    private final long replicaId;

    private final float[] featureVector;

    private final WellReference wellReferenceOrNull;

    /** @param wellReferenceOrNull null if these are aggregated data */
    public WellData(long replicaId, float[] featureVector, WellReference wellReferenceOrNull)
    {
        this.featureVector = featureVector;
        this.replicaId = replicaId;
        this.wellReferenceOrNull = wellReferenceOrNull;
    }

    @Override
    public long getReplicaMaterialId()
    {
        return replicaId;
    }

    @Override
    public float[] getFeatureVector()
    {
        return featureVector;
    }

    public WellReference tryGetWellReference()
    {
        return wellReferenceOrNull;
    }

    @Override
    public String toString()
    {
        return "repl " + replicaId + ": " + Arrays.toString(featureVector);
    }
}
