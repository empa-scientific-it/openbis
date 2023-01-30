/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
class ExperimentDsl extends Experiment
{
    private final String code;

    private ExperimentType type;

    private Project project;

    private Collection<Sample> samples;

    private Collection<MetaProject> metaProjects;

    ExperimentDsl(ExperimentType type, String code, Project project, Collection<Sample> samples,
            Collection<MetaProject> metaProjects)
    {
        this.type = type;
        this.code = code;
        this.project = project;
        this.samples = samples;
        this.metaProjects = metaProjects;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public ExperimentType getType()
    {
        return type;
    }

    @Override
    public Project getProject()
    {
        return project;
    }

    @Override
    public Collection<Sample> getSamples()
    {
        return samples;
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        return metaProjects;
    }

    void setType(ExperimentType type)
    {
        this.type = type;
    }

    void setProject(Project project)
    {
        this.project = project;
    }

    void setSamples(Collection<Sample> samples)
    {
        this.samples = samples;
    }

    void setMetaProjects(Collection<MetaProject> metaProjects)
    {
        this.metaProjects = metaProjects;
    }

    @Override
    public String toString()
    {
        return "Experiment " + code;
    }
}
