/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.tag.create.TagCreation")
public class TagCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    private List<? extends IExperimentId> experimentIds;

    private List<? extends ISampleId> sampleIds;

    private List<? extends IDataSetId> dataSetIds;

    private List<? extends IMaterialId> materialIds;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<? extends IExperimentId> getExperimentIds()
    {
        return experimentIds;
    }

    public void setExperimentIds(List<? extends IExperimentId> experimentIds)
    {
        this.experimentIds = experimentIds;
    }

    public List<? extends ISampleId> getSampleIds()
    {
        return sampleIds;
    }

    public void setSampleIds(List<? extends ISampleId> sampleIds)
    {
        this.sampleIds = sampleIds;
    }

    public List<? extends IDataSetId> getDataSetIds()
    {
        return dataSetIds;
    }

    public void setDataSetIds(List<? extends IDataSetId> dataSetIds)
    {
        this.dataSetIds = dataSetIds;
    }

    public List<? extends IMaterialId> getMaterialIds()
    {
        return materialIds;
    }

    public void setMaterialIds(List<? extends IMaterialId> materialIds)
    {
        this.materialIds = materialIds;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("code", code).toString();
    }

}
