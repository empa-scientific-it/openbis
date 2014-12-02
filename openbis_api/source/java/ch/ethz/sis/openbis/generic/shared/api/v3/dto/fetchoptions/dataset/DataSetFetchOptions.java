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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("DataSetFetchOptions")
public class DataSetFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFetchOptions parents;

    @JsonProperty
    private DataSetFetchOptions children;

    @JsonProperty
    private DataSetFetchOptions containers;

    @JsonProperty
    private DataSetFetchOptions contained;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private DataSetTypeFetchOptions type;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private PropertyFetchOptions properties;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new DataSetFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withParentsUsing(DataSetFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new DataSetFetchOptions();
        }
        return children;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withChildrenUsing(DataSetFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withContainers()
    {
        if (containers == null)
        {
            containers = new DataSetFetchOptions();
        }
        return containers;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withContainersUsing(DataSetFetchOptions fetchOptions)
    {
        return containers = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasContainers()
    {
        return containers != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withContained()
    {
        if (contained == null)
        {
            contained = new DataSetFetchOptions();
        }
        return contained;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withContainedUsing(DataSetFetchOptions fetchOptions)
    {
        return contained = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasContained()
    {
        return contained != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public TagFetchOptions withTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public TagFetchOptions withTagsUsing(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasTags()
    {
        return tags != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new DataSetTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetTypeFetchOptions withTypeUsing(DataSetTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasExperiment()
    {
        return experiment != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PropertyFetchOptions withProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PropertyFetchOptions withPropertiesUsing(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasProperties()
    {
        return properties != null;
    }

}
