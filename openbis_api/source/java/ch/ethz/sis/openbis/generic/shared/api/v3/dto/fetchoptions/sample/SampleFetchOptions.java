/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("SampleFetchOptions")
public class SampleFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SampleTypeFetchOptions type;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private PropertyFetchOptions properties;

    @JsonProperty
    private SampleFetchOptions parents;

    @JsonProperty
    private SampleFetchOptions children;

    @JsonProperty
    private SampleFetchOptions container;

    @JsonProperty
    private SampleFetchOptions contained;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    public SampleTypeFetchOptions fetchType()
    {
        if (this.type == null)
        {
            this.type = new SampleTypeFetchOptions();
        }
        return this.type;
    }

    public SampleTypeFetchOptions fetchType(SampleTypeFetchOptions fetchOptions)
    {
        return this.type = fetchOptions;
    }

    public boolean hasType()
    {
        return this.type != null;
    }

    public SpaceFetchOptions fetchSpace()
    {
        if (this.space == null)
        {
            this.space = new SpaceFetchOptions();
        }
        return this.space;
    }

    public SpaceFetchOptions fetchSpace(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    public boolean hasSpace()
    {
        return this.space != null;
    }

    public ExperimentFetchOptions fetchExperiment()
    {
        if (this.experiment == null)
        {
            this.experiment = new ExperimentFetchOptions();
        }
        return this.experiment;
    }

    public ExperimentFetchOptions fetchExperiment(ExperimentFetchOptions fetchOptions)
    {
        return this.experiment = fetchOptions;
    }

    public boolean hasExperiment()
    {
        return this.experiment != null;
    }

    public PropertyFetchOptions fetchProperties()
    {
        if (this.properties == null)
        {
            this.properties = new PropertyFetchOptions();
        }
        return this.properties;
    }

    public PropertyFetchOptions fetchProperties(PropertyFetchOptions fetchOptions)
    {
        return this.properties = fetchOptions;
    }

    public boolean hasProperties()
    {
        return this.properties != null;
    }

    public SampleFetchOptions fetchParents()
    {
        if (this.parents == null)
        {
            this.parents = new SampleFetchOptions();
        }
        return this.parents;
    }

    public SampleFetchOptions fetchParents(SampleFetchOptions fetchOptions)
    {
        return this.parents = fetchOptions;
    }

    public boolean hasParents()
    {
        return this.parents != null;
    }

    public SampleFetchOptions fetchChildren()
    {
        if (this.children == null)
        {
            this.children = new SampleFetchOptions();
        }
        return this.children;
    }

    public SampleFetchOptions fetchChildren(SampleFetchOptions fetchOptions)
    {
        return this.children = fetchOptions;
    }

    public boolean hasChildren()
    {
        return this.children != null;
    }

    public SampleFetchOptions fetchContainer()
    {
        if (this.container == null)
        {
            this.container = new SampleFetchOptions();
        }
        return this.container;
    }

    public SampleFetchOptions fetchContainer(SampleFetchOptions fetchOptions)
    {
        return this.container = fetchOptions;
    }

    public boolean hasContainer()
    {
        return this.container != null;
    }

    public SampleFetchOptions fetchContained()
    {
        if (this.contained == null)
        {
            this.contained = new SampleFetchOptions();
        }
        return this.contained;
    }

    public SampleFetchOptions fetchContained(SampleFetchOptions fetchOptions)
    {
        return this.contained = fetchOptions;
    }

    public boolean hasContained()
    {
        return this.contained != null;
    }

    public TagFetchOptions fetchTags()
    {
        if (this.tags == null)
        {
            this.tags = new TagFetchOptions();
        }
        return this.tags;
    }

    public TagFetchOptions fetchTags(TagFetchOptions fetchOptions)
    {
        return this.tags = fetchOptions;
    }

    public boolean hasTags()
    {
        return this.tags != null;
    }

    public PersonFetchOptions fetchRegistrator()
    {
        if (this.registrator == null)
        {
            this.registrator = new PersonFetchOptions();
        }
        return this.registrator;
    }

    public PersonFetchOptions fetchRegistrator(PersonFetchOptions fetchOptions)
    {
        return this.registrator = fetchOptions;
    }

    public boolean hasRegistrator()
    {
        return this.registrator != null;
    }

    public PersonFetchOptions fetchModifier()
    {
        if (this.modifier == null)
        {
            this.modifier = new PersonFetchOptions();
        }
        return this.modifier;
    }

    public PersonFetchOptions fetchModifier(PersonFetchOptions fetchOptions)
    {
        return this.modifier = fetchOptions;
    }

    public boolean hasModifier()
    {
        return this.modifier != null;
    }

    public AttachmentFetchOptions fetchAttachments()
    {
        if (this.attachments == null)
        {
            this.attachments = new AttachmentFetchOptions();
        }
        return this.attachments;
    }

    public AttachmentFetchOptions fetchAttachments(AttachmentFetchOptions fetchOptions)
    {
        return this.attachments = fetchOptions;
    }

    public boolean hasAttachments()
    {
        return this.attachments != null;
    }

}
