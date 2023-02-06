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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.global.fetchoptions.GlobalSearchObjectFetchOptions")
public class GlobalSearchObjectFetchOptions extends FetchOptions<GlobalSearchObject> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private SampleFetchOptions sample;

    @JsonProperty
    private DataSetFetchOptions dataSet;

    @JsonProperty
    private MaterialFetchOptions material;

    @JsonProperty
    private GlobalSearchObjectSortOptions sort;

    @JsonProperty
    private MatchFetchOptions match;

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperiment()
    {
        return experiment != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSample()
    {
        if (sample == null)
        {
            sample = new SampleFetchOptions();
        }
        return sample;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSampleUsing(SampleFetchOptions fetchOptions)
    {
        return sample = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSample()
    {
        return sample != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSet()
    {
        if (dataSet == null)
        {
            dataSet = new DataSetFetchOptions();
        }
        return dataSet;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSetUsing(DataSetFetchOptions fetchOptions)
    {
        return dataSet = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataSet()
    {
        return dataSet != null;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterial()
    {
        if (material == null)
        {
            material = new MaterialFetchOptions();
        }
        return material;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialUsing(MaterialFetchOptions fetchOptions)
    {
        return material = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasMaterial()
    {
        return material != null;
    }

    public MatchFetchOptions withMatch()
    {
        if (match == null)
        {
            match = new MatchFetchOptions();
        }
        return match;
    }

    public boolean hasMatch()
    {
        return match != null;
    }

    public MatchFetchOptions withMatchUsing(final MatchFetchOptions fetchOptions)
    {
        return match = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public GlobalSearchObjectSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new GlobalSearchObjectSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public GlobalSearchObjectSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("GlobalSearchObject", this);
        f.addFetchOption("Experiment", experiment);
        f.addFetchOption("Sample", sample);
        f.addFetchOption("DataSet", dataSet);
        f.addFetchOption("Material", material);
        return f;
    }

}
