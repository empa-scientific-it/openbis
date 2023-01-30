/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * A experiment to register.
 * 
 * @author Izabela Adamczyk
 */
public final class NewExperiment extends Identifier<NewExperiment> implements IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String experimentTypeCode = null;

    private String[] samples = new String[0];

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    private SampleType sampleType = null;

    private boolean generateCodes = false;

    private boolean registerSamples = false;

    private List<NewSamplesWithTypes> newSamples;

    private List<NewAttachment> attachments;

    private String[] metaprojectsOrNull;

    public NewExperiment()
    {
    }

    public NewExperiment(final String identifier, final String experimentTypeCode)
    {
        setIdentifier(identifier);
        setExperimentTypeCode(experimentTypeCode);
    }

    public final String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public final void setExperimentTypeCode(final String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    @Override
    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    @Override
    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

    public String[] getSamples()
    {
        return samples;
    }

    public void setSamples(String[] samples)
    {
        this.samples = samples;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setGenerateCodes(boolean generateCodes)
    {
        this.generateCodes = generateCodes;
    }

    public boolean isGenerateCodes()
    {
        return generateCodes;
    }

    public void setRegisterSamples(boolean register)
    {
        this.registerSamples = register;
    }

    public boolean isRegisterSamples()
    {
        return registerSamples;
    }

    public void setNewSamples(List<NewSamplesWithTypes> newSamples)
    {
        this.newSamples = newSamples;
    }

    public List<NewSamplesWithTypes> getNewSamples()
    {
        return newSamples;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }

    public String[] getMetaprojectsOrNull()
    {
        return metaprojectsOrNull;
    }

    public void setMetaprojectsOrNull(String[] metaprojectsOrNull)
    {
        this.metaprojectsOrNull = metaprojectsOrNull;
    }

}
