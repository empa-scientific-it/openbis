/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * @author pkupczyk
 */
@JsonObject("Deletion")
public class Deletion implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Long id;

    private int totalSamplesCount;

    private int totalDatasetsCount;

    private int totalExperimentsCount;

    private String reasonOrNull;

    private List<DeletedEntity> deletedEntities = new ArrayList<DeletedEntity>();

    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        this.id = id;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    @JsonProperty("id")
    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

    public int getTotalSamplesCount()
    {
        return totalSamplesCount;
    }

    public void setTotalSamplesCount(int totalSamplesCount)
    {
        this.totalSamplesCount = totalSamplesCount;
    }

    public int getTotalDatasetsCount()
    {
        return totalDatasetsCount;
    }

    public void setTotalDatasetsCount(int totalDatasetsCount)
    {
        this.totalDatasetsCount = totalDatasetsCount;
    }

    public int getTotalExperimentsCount()
    {
        return totalExperimentsCount;
    }

    public void setTotalExperimentsCount(int totalExperimentsCount)
    {
        this.totalExperimentsCount = totalExperimentsCount;
    }

    public String getReasonOrNull()
    {
        return reasonOrNull;
    }

    public void setReasonOrNull(String reasonOrNull)
    {
        this.reasonOrNull = reasonOrNull;
    }

    public List<DeletedEntity> getDeletedEntities()
    {
        return deletedEntities;
    }

    public void setDeletedEntities(List<DeletedEntity> deletedEntities)
    {
        this.deletedEntities = deletedEntities;
    }

}
