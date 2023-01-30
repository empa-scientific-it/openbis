/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * Contains a list of new basic experiments and their type.
 * 
 * @author Izabela Adamczyk
 */
public class UpdatedExperimentsWithType
{
    ExperimentType experimentType;

    List<UpdatedBasicExperiment> updatedExperiments;

    public UpdatedExperimentsWithType()
    {
    }

    public UpdatedExperimentsWithType(ExperimentType experimentType,
            List<UpdatedBasicExperiment> updatedExperiments)
    {
        this.experimentType = experimentType;
        this.updatedExperiments = updatedExperiments;
    }

    public List<UpdatedBasicExperiment> getUpdatedExperiments()
    {
        return updatedExperiments;
    }

    public void setUpdatedExperiments(List<UpdatedBasicExperiment> updatedExperiments)
    {
        this.updatedExperiments = updatedExperiments;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }
}
