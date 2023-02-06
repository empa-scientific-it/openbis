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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.space;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpaceTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.space.Space")
public class Space implements Serializable, ICodeHolder, IDescriptionHolder, IModificationDateHolder, IPermIdHolder, IProjectsHolder,
        IRegistrationDateHolder, IRegistratorHolder, ISamplesHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SpaceFetchOptions fetchOptions;

    @JsonProperty
    private SpaceTechId id;

    @JsonProperty
    private SpacePermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty
    private boolean frozen;

    @JsonProperty
    private boolean frozenForProjects;

    @JsonProperty
    private boolean frozenForSamples;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private List<Sample> samples;

    @JsonProperty
    private List<Project> projects;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public SpaceFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(SpaceFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public SpaceTechId getId()
    {
        return id;
    }

    public void setId(SpaceTechId id)
    {
        this.id = id;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public SpacePermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(SpacePermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isFrozen()
    {
        return frozen;
    }

    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    public boolean isFrozenForProjects()
    {
        return frozenForProjects;
    }

    public void setFrozenForProjects(boolean frozenForProjects)
    {
        this.frozenForProjects = frozenForProjects;
    }

    public boolean isFrozenForSamples()
    {
        return frozenForSamples;
    }

    public void setFrozenForSamples(boolean frozenForSamples)
    {
        this.frozenForSamples = frozenForSamples;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Sample> getSamples()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamples())
        {
            return samples;
        } else
        {
            throw new NotFetchedException("Samples have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSamples(List<Sample> samples)
    {
        this.samples = samples;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Project> getProjects()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProjects())
        {
            return projects;
        } else
        {
            throw new NotFetchedException("Projects have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Space " + permId;
    }

}
