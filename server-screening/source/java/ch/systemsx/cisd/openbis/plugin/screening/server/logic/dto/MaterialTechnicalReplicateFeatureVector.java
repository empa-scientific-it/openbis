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

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Feature vector for one well.
 * 
 * @author Tomasz Pylak
 */
public class MaterialTechnicalReplicateFeatureVector implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int technicalReplicateSequenceNumber;

    private float[] featueVector;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialTechnicalReplicateFeatureVector()
    {
    }

    public MaterialTechnicalReplicateFeatureVector(int technicalReplicateSequenceNumber,
            float[] featueVector)
    {
        this.technicalReplicateSequenceNumber = technicalReplicateSequenceNumber;
        this.featueVector = featueVector;
    }

    public int getTechnicalReplicateSequenceNumber()
    {
        return technicalReplicateSequenceNumber;
    }

    public float[] getFeatueVector()
    {
        return featueVector;
    }

}
