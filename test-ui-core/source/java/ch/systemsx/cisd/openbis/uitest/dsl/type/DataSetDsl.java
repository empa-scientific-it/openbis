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

import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
class DataSetDsl extends DataSet
{

    private DataSetType type;

    private Sample sample;

    private Experiment experiment;

    private Collection<DataSet> parents;

    private Collection<MetaProject> metaProjects;

    public DataSetDsl(DataSetType type, Sample sample, Experiment experiment, Collection<DataSet> parents,
            Collection<MetaProject> metaProjects)
    {
        this.type = type;
        this.sample = sample;
        this.experiment = experiment;
        this.parents = parents;
        this.metaProjects = metaProjects;
    }

    @Override
    public String getCode()
    {
        throw new IllegalStateException("DataSet not created yet, it does not have a code.");
    }

    @Override
    public DataSetType getType()
    {
        return type;
    }

    @Override
    public Sample getSample()
    {
        return sample;
    }

    @Override
    public Experiment getExperiment()
    {
        return experiment;
    }

    @Override
    public Collection<DataSet> getParents()
    {
        return parents;
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        return this.metaProjects;
    }

    void setType(DataSetType type)
    {
        this.type = type;
    }

    void setSample(Sample sample)
    {
        this.sample = sample;
    }

    void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    void setMetaProjects(Collection<MetaProject> metaProjects)
    {
        this.metaProjects = metaProjects;
    }

    void setParents(Collection<DataSet> parents)
    {
        this.parents = parents;
    }

    @Override
    public String toString()
    {
        return "DataSet for sample " + sample + " and experiment " + experiment
                + " that has not been created yet";
    }
}
