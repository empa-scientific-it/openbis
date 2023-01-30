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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicSampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Izabela Adamczyk
 */
public class SampleUpdates extends BasicSampleUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String sessionKey;

    private List<NewAttachment> attachments;
    
    private String projectIdentifierOrNull;

    private ExperimentIdentifier experimentIdentifierOrNull;

    private String sampleIdentifier;

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public SampleUpdates(String sessionKey, TechId sampleId, List<IEntityProperty> properties,
            List<NewAttachment> attachments, ExperimentIdentifier experimentIdentifierOrNull,
            String projectIdentifierOrNull,
            int version, String sampleIdentifier, String containerIdentifierOrNull,
            String[] modifiedParentCodesOrNull)
    {
        super(sampleId, properties, version, containerIdentifierOrNull, modifiedParentCodesOrNull);
        this.sessionKey = sessionKey;
        this.attachments = attachments;
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        this.projectIdentifierOrNull = projectIdentifierOrNull;
        this.sampleIdentifier = sampleIdentifier;
    }

    public SampleUpdates()
    {
    }

    public String getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }

    public String getProjectIdentifierOrNull()
    {
        return projectIdentifierOrNull;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(ExperimentIdentifier experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

}
