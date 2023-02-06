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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.tag;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IOwnerHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISamplesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
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
@JsonObject("as.dto.tag.Tag")
public class Tag implements Serializable, ICodeHolder, IDataSetsHolder, IDescriptionHolder, IExperimentsHolder, IMaterialsHolder, IOwnerHolder, IPermIdHolder, IRegistrationDateHolder, ISamplesHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private TagFetchOptions fetchOptions;

    @JsonProperty
    private TagPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty(value="private")
    private Boolean isPrivate;

    @JsonProperty
    private List<Experiment> experiments;

    @JsonProperty
    private List<Sample> samples;

    @JsonProperty
    private List<DataSet> dataSets;

    @JsonProperty
    private List<Material> materials;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person owner;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public TagFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(TagFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public TagPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(TagPermId permId)
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

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isPrivate()
    {
        return isPrivate;
    }

    // Method automatically generated with DtoGenerator
    public void setPrivate(Boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Experiment> getExperiments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperiments())
        {
            return experiments;
        }
        else
        {
            throw new NotFetchedException("Experiments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiments(List<Experiment> experiments)
    {
        this.experiments = experiments;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Sample> getSamples()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamples())
        {
            return samples;
        }
        else
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
    public List<DataSet> getDataSets()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSets())
        {
            return dataSets;
        }
        else
        {
            throw new NotFetchedException("Data sets have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSets(List<DataSet> dataSets)
    {
        this.dataSets = dataSets;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Material> getMaterials()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterials())
        {
            return materials;
        }
        else
        {
            throw new NotFetchedException("Materials have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setMaterials(List<Material> materials)
    {
        this.materials = materials;
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
    public Person getOwner()
    {
        if (getFetchOptions() != null && getFetchOptions().hasOwner())
        {
            return owner;
        }
        else
        {
            throw new NotFetchedException("Owner has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setOwner(Person owner)
    {
        this.owner = owner;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Tag " + code;
    }

}
