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
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
class SampleDsl extends Sample
{
    private SampleType type;

    private final String code;

    private Experiment experiment;

    private Space space;

    private Collection<Sample> parents;

    private Map<PropertyType, Object> properties;

    private Collection<MetaProject> metaProjects;

    private Sample container;

    public SampleDsl(SampleType type, String code, Experiment experiment, Space space,
            Sample container, Collection<Sample> parents, Map<PropertyType, Object> properties,
            Collection<MetaProject> metaProjects)
    {
        this.type = type;
        this.code = code;
        this.experiment = experiment;
        this.space = space;
        this.container = container;
        this.parents = parents;
        this.properties = properties;
        this.metaProjects = metaProjects;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public SampleType getType()
    {
        return type;
    }

    @Override
    public Experiment getExperiment()
    {
        return experiment;
    }

    @Override
    public Space getSpace()
    {
        return space;
    }

    @Override
    public Sample getContainer()
    {
        return container;
    }

    @Override
    public Collection<Sample> getParents()
    {
        return parents;
    }

    @Override
    public Map<PropertyType, Object> getProperties()
    {
        return properties;
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        return metaProjects;
    }

    void setType(SampleType type)
    {
        this.type = type;
    }

    void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    void setSpace(Space space)
    {
        this.space = space;
    }

    void setContainer(Sample container)
    {
        this.container = container;
    }

    void setParents(Collection<Sample> parents)
    {
        this.parents = parents;
    }

    void setProperties(Map<PropertyType, Object> properties)
    {
        this.properties = properties;
    }

    void setMetaProjects(Collection<MetaProject> metaProjects)
    {
        this.metaProjects = metaProjects;
    }

    @Override
    public String toString()
    {
        return "Sample " + this.code;
    }
}
