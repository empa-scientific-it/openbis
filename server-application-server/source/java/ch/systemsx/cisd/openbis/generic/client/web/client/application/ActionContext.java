/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Context of the last action performed by the user.
 * 
 * @author Piotr Buczek
 */
public class ActionContext
{
    private Experiment experimentOrNull;

    private Project projectOrNull;

    private String spaceCodeOrNull;

    private SampleType sampleTypeOrNull;

    private ExperimentType experimentTypeOrNull;

    private Sample parent;

    public ActionContext()
    {
    }

    public ActionContext(Experiment experiment)
    {
        this.experimentOrNull = experiment;
        this.projectOrNull = experiment.getProject();
        this.spaceCodeOrNull = projectOrNull.getSpace().getCode();
    }

    public Sample getParent()
    {
        return parent;
    }

    public void setParent(Sample parent)
    {
        this.parent = parent;
    }

    public Experiment tryGetExperiment()
    {
        return experimentOrNull;
    }

    public void setExperiment(Experiment experiment)
    {
        this.experimentOrNull = experiment;
    }

    public Project tryGetProject()
    {
        return projectOrNull;
    }

    public String tryGetProjectIdentifier()
    {
        return projectOrNull == null ? null : projectOrNull.getIdentifier();
    }

    public void setProject(Project project)
    {
        this.projectOrNull = project;
    }

    public SampleType tryGetSampleType()
    {
        return sampleTypeOrNull;
    }

    public String tryGetSampleTypeCode()
    {
        return (sampleTypeOrNull == null || sampleTypeOrNull.isAllTypesCode()) ? null
                : sampleTypeOrNull.getCode();
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleTypeOrNull = sampleType;
    }

    public ExperimentType tryGetExperimentType()
    {
        return experimentTypeOrNull;
    }

    public String tryGetExperimentTypeCode()
    {
        return (experimentTypeOrNull == null || experimentTypeOrNull.isAllTypesCode()) ? null
                : experimentTypeOrNull.getCode();
    }

    public void setExperimentType(ExperimentType experimentType)
    {
        this.experimentTypeOrNull = experimentType;
    }

    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCodeOrNull = spaceCode;
    }

}
