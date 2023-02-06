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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.global;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.global.GlobalSearchObject")
public class GlobalSearchObject implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private GlobalSearchObjectFetchOptions fetchOptions;

    @JsonProperty
    private GlobalSearchObjectKind objectKind;

    @JsonProperty
    private IObjectId objectPermId;

    @JsonProperty
    private IObjectId objectIdentifier;

    @JsonProperty
    private String match;

    @JsonProperty
    private double score;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Sample sample;

    @JsonProperty
    private DataSet dataSet;

    @JsonProperty
    private Material material;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public GlobalSearchObjectFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(GlobalSearchObjectFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public GlobalSearchObjectKind getObjectKind()
    {
        return objectKind;
    }

    // Method automatically generated with DtoGenerator
    public void setObjectKind(GlobalSearchObjectKind objectKind)
    {
        this.objectKind = objectKind;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IObjectId getObjectPermId()
    {
        return objectPermId;
    }

    // Method automatically generated with DtoGenerator
    public void setObjectPermId(IObjectId objectPermId)
    {
        this.objectPermId = objectPermId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IObjectId getObjectIdentifier()
    {
        return objectIdentifier;
    }

    // Method automatically generated with DtoGenerator
    public void setObjectIdentifier(IObjectId objectIdentifier)
    {
        this.objectIdentifier = objectIdentifier;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getMatch()
    {
        return match;
    }

    // Method automatically generated with DtoGenerator
    public void setMatch(String match)
    {
        this.match = match;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public double getScore()
    {
        return score;
    }

    // Method automatically generated with DtoGenerator
    public void setScore(double score)
    {
        this.score = score;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Experiment getExperiment()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Sample getSample()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSample())
        {
            return sample;
        }
        else
        {
            throw new NotFetchedException("Sample has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSample(Sample sample)
    {
        this.sample = sample;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataSet getDataSet()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSet())
        {
            return dataSet;
        }
        else
        {
            throw new NotFetchedException("Data Set has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSet(DataSet dataSet)
    {
        this.dataSet = dataSet;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Material getMaterial()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterial())
        {
            return material;
        }
        else
        {
            throw new NotFetchedException("Material has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setMaterial(Material material)
    {
        this.material = material;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "GlobalSearchObject kind: " + objectKind + ", permId: " + objectPermId + ", identifier: " + objectIdentifier;
    }

}
