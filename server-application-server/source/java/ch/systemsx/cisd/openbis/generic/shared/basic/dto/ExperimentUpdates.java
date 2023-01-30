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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Description of the updates which should be performed on the experiment.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentUpdates extends BasicExperimentUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TechId experimentId;

    // ----- the data which should be changed:

    // points to the project which will be set for the experiment
    private String projectIdentifier;

    // the key in the session at which points to the new attachments which will be added to the old
    // ones.
    private String attachmentSessionKey;

    private SampleType sampleType;

    private boolean generateCodes;

    private String samplesSessionKey;

    private List<NewAttachment> attachments;

    public String getAttachmentSessionKey()
    {
        return attachmentSessionKey;
    }

    public void setAttachmentSessionKey(String attachmentSessionKey)
    {
        this.attachmentSessionKey = attachmentSessionKey;
    }

    public TechId getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(TechId experimentId)
    {
        this.experimentId = experimentId;
    }

    public String getProjectIdentifier()
    {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String newProjectIdentifier)
    {
        this.projectIdentifier = newProjectIdentifier;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public void setGenerateCodes(boolean generateCodes)
    {
        this.generateCodes = generateCodes;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public boolean isGenerateCodes()
    {
        return generateCodes;
    }

    public void setSamplesSessionKey(String samplesSessionKey)
    {
        this.samplesSessionKey = samplesSessionKey;
    }

    public String getSamplesSessionKey()
    {
        return samplesSessionKey;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }
}
