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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicSampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Izabela Adamczyk
 */
public class SampleUpdatesDTO extends BasicSampleUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExperimentIdentifier experimentIdentifierOrNull;

    private SampleIdentifier sampleIdentifier;

    private Collection<NewAttachment> attachments;

    private boolean updateExperimentLink = true;

    private ProjectIdentifier projectIdentifier;

    public SampleUpdatesDTO(TechId sampleId, List<IEntityProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, ProjectIdentifier projectIdentifier, 
            Collection<NewAttachment> attachments,
            int version, SampleIdentifier sampleIdentifier, String containerIdentifierOrNull,
            String[] modifiedParentCodesOrNull)
    {
        super(sampleId, properties, version, containerIdentifierOrNull, modifiedParentCodesOrNull);
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        if (experimentIdentifierOrNull != null)
        {
            this.projectIdentifier = ProjectIdentifierFactory.parse(experimentIdentifierOrNull.asProjectIdentifierString());
        } else
        {
            this.projectIdentifier = projectIdentifier;
        }
        this.attachments = attachments;
        this.sampleIdentifier = sampleIdentifier;
    }

    public SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(SampleIdentifier sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public ProjectIdentifier getProjectIdentifier()
    {
        return projectIdentifier;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(ExperimentIdentifier experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public boolean isUpdateExperimentLink()
    {
        return updateExperimentLink;
    }

    public void setUpdateExperimentLink(boolean updateExperimentLink)
    {
        this.updateExperimentLink = updateExperimentLink;
    }

    public Collection<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Collection<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }
}
