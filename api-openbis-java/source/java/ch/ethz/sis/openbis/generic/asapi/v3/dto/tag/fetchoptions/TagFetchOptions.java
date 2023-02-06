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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.tag.fetchoptions.TagFetchOptions")
public class TagFetchOptions extends FetchOptions<Tag> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions experiments;

    @JsonProperty
    private SampleFetchOptions samples;

    @JsonProperty
    private DataSetFetchOptions dataSets;

    @JsonProperty
    private MaterialFetchOptions materials;

    @JsonProperty
    private PersonFetchOptions owner;

    @JsonProperty
    private TagSortOptions sort;

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperiments()
    {
        if (experiments == null)
        {
            experiments = new ExperimentFetchOptions();
        }
        return experiments;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperimentsUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiments = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperiments()
    {
        return experiments != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamples()
    {
        if (samples == null)
        {
            samples = new SampleFetchOptions();
        }
        return samples;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamplesUsing(SampleFetchOptions fetchOptions)
    {
        return samples = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSamples()
    {
        return samples != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSets()
    {
        if (dataSets == null)
        {
            dataSets = new DataSetFetchOptions();
        }
        return dataSets;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSetsUsing(DataSetFetchOptions fetchOptions)
    {
        return dataSets = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataSets()
    {
        return dataSets != null;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterials()
    {
        if (materials == null)
        {
            materials = new MaterialFetchOptions();
        }
        return materials;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialsUsing(MaterialFetchOptions fetchOptions)
    {
        return materials = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasMaterials()
    {
        return materials != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwner()
    {
        if (owner == null)
        {
            owner = new PersonFetchOptions();
        }
        return owner;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwnerUsing(PersonFetchOptions fetchOptions)
    {
        return owner = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasOwner()
    {
        return owner != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public TagSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new TagSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public TagSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Tag", this);
        f.addFetchOption("Experiments", experiments);
        f.addFetchOption("Samples", samples);
        f.addFetchOption("DataSets", dataSets);
        f.addFetchOption("Materials", materials);
        f.addFetchOption("Owner", owner);
        return f;
    }

}
