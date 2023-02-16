/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Contains well description, its image dataset (single) and biological and technical replicate labels.
 * 
 * @author Tomasz Pylak
 */
public class WellReplicaImage implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private WellContent wellImage;

    private int technicalReplicateSequenceNumber;

    // null if there are no biological replicates
    private String biologicalReplicateLabelOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private WellReplicaImage()
    {
    }

    public WellReplicaImage(WellContent wellImage, int technicalReplicaSequenceNumber,
            String subgroupLabelOrNull)
    {
        this.wellImage = wellImage;
        this.technicalReplicateSequenceNumber = technicalReplicaSequenceNumber;
        this.biologicalReplicateLabelOrNull = subgroupLabelOrNull;
    }

    public WellContent getWellImage()
    {
        return wellImage;
    }

    public int getTechnicalReplicateSequenceNumber()
    {
        return technicalReplicateSequenceNumber;
    }

    public String tryGetBiologicalReplicateLabel()
    {
        return biologicalReplicateLabelOrNull;
    }
}
