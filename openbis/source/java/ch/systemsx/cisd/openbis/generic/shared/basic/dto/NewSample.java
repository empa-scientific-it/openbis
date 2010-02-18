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

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * A sample to register.
 * 
 * @author Christian Ribeaud
 */
public class NewSample extends Identifier<NewSample> implements Comparable<NewSample>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SAMPLE_REGISTRATION_TEMPLATE_COMMENT =
            "# The \"container\" and \"parent\" columns are optional, only one should be specified. They should contain a sample identifier, e.g. /SPACE/SAMPLE_1\n"
                    + "# If \"container\" sample is provided, the registered sample will become a \"component\" of it.\n"
                    + "# If \"parent\" sample is provided, the registered sample will become a \"child\" of it.\n"
                    + "# The \"experiment\" column is optional, cannot be specified for shared samples and should contain experiment identifier, e.g. /SPACE/PROJECT/EXP_1\n";

    public static final String CONTAINER = "container";

    public static final String PARENT = "parent";

    public static final String EXPERIMENT = "experiment";

    private SampleType sampleType;

    /**
     * The parent identifier.
     */
    private String parentIdentifier;

    /**
     * The container identifier.
     */
    private String containerIdentifier;

    /**
     * The experiment identifier.
     */
    private String experimentIdentifier;

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    private List<NewAttachment> attachments;

    public NewSample()
    {
    }

    public NewSample(final String identifier, final SampleType sampleType,
            final String parentIdentifier, final String containerIdentifier)
    {
        setIdentifier(identifier);
        setSampleType(sampleType);
        setParentIdentifier(parentIdentifier);
        setContainerIdentifier(containerIdentifier);
    }

    public NewSample(final String identifier, SampleType sampleType, String parentIdentifier,
            String containerIdentifier, String experimentIdentifier, IEntityProperty[] properties,
            List<NewAttachment> attachments)
    {
        this(identifier, sampleType, parentIdentifier, containerIdentifier);
        this.experimentIdentifier = experimentIdentifier;
        this.properties = properties;
        this.attachments = attachments;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }

    public final SampleType getSampleType()
    {
        return sampleType;
    }

    public final void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public final String getParentIdentifier()
    {
        return parentIdentifier;
    }

    @BeanProperty(label = PARENT, optional = true)
    public final void setParentIdentifier(final String parent)
    {
        this.parentIdentifier = parent;
    }

    public final String getContainerIdentifier()
    {
        return containerIdentifier;
    }

    @BeanProperty(label = CONTAINER, optional = true)
    public final void setContainerIdentifier(final String container)
    {
        this.containerIdentifier = container;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    @BeanProperty(label = EXPERIMENT, optional = true)
    public void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

    // NOTE:
    // Special equality check for NewSamples that is not complete but speeds up uniqueness check
    // of new sample codes during import. The check on the DB level is complete.
    //
    // Here we compare a pair of container's identifier and new sample's identifier.
    // 1. This comparison doesn't have the knowledge about home group so it will say that
    // 'SAMPLE_1' != /HOME_GROUP/SAMPLE_1'.
    // 2. We need to also use container identifier because when samples are registered container
    // code is not required in its identifier.
    //
    // So this equals may return 'false' for NewSample objects that would in fact create
    // samples with the same identifiers, but when it returns 'true' it is always correct.
    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof NewSample == false)
        {
            return false;
        }
        final NewSample that = (NewSample) obj;
        final String thisCombinedIdentifier = this.getIdentifier() + this.getContainerIdentifier();
        final String thatCombinedIdentifier = that.getIdentifier() + that.getContainerIdentifier();
        return thisCombinedIdentifier.equals(thatCombinedIdentifier);
    }
}
