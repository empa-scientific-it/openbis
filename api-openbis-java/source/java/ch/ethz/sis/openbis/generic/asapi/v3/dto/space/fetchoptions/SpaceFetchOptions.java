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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.space.fetchoptions.SpaceFetchOptions")
public class SpaceFetchOptions extends FetchOptions<Space> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private SampleFetchOptions samples;

    @JsonProperty
    private ProjectFetchOptions projects;

    @JsonProperty
    private SpaceSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
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
    public ProjectFetchOptions withProjects()
    {
        if (projects == null)
        {
            projects = new ProjectFetchOptions();
        }
        return projects;
    }

    // Method automatically generated with DtoGenerator
    public ProjectFetchOptions withProjectsUsing(ProjectFetchOptions fetchOptions)
    {
        return projects = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProjects()
    {
        return projects != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SpaceSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SpaceSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SpaceSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Space", this);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Samples", samples);
        f.addFetchOption("Projects", projects);
        return f;
    }

}
