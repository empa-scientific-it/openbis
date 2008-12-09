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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * A sample to register.
 * 
 * @author Christian Ribeaud
 */
public final class NewSample extends Identifier<NewSample>
{
    private SampleType sampleType;

    /**
     * The parent identifier.
     */
    private String parentIdentifier;

    /**
     * The container identifier.
     */
    private String containerIdentifier;

    private List<SampleProperty> properties = new ArrayList<SampleProperty>();

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

    @BeanProperty(label = "parent")
    public final void setParentIdentifier(final String parent)
    {
        this.parentIdentifier = parent;
    }

    public final String getContainerIdentifier()
    {
        return containerIdentifier;
    }

    @BeanProperty(label = "container")
    public final void setContainerIdentifier(final String container)
    {
        this.containerIdentifier = container;
    }

    public final List<SampleProperty> getProperties()
    {
        return properties;
    }

    public final void setProperties(final List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public final void addProperty(final SampleProperty property)
    {
        properties.add(property);
    }
}