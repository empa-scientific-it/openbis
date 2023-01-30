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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Builder for an instance of {@link Sample}.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleBuilder
{
    private Sample sample = new Sample();

    public SampleBuilder()
    {
        sample.setProperties(new ArrayList<IEntityProperty>());
    }

    public SampleBuilder(String identifier)
    {
        this();
        identifier(identifier);
    }

    public SampleBuilder id(long id)
    {
        sample.setId(id);
        return this;
    }

    public SampleBuilder code(String code)
    {
        sample.setCode(code);
        return this;
    }

    public SampleBuilder identifier(String identifier)
    {
        sample.setIdentifier(identifier);
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifier);
        sample.setCode(sampleIdentifier.getSampleCode());
        String sampleSubCode = sampleIdentifier.getSampleSubCode();
        sample.setSubCode(sampleSubCode);
        if (sampleIdentifier.isProjectLevel())
        {
            Project project = new Project();
            ProjectIdentifier projectIdentifier = sampleIdentifier.getProjectLevel();
            project.setCode(projectIdentifier.getProjectCode());
            Space space = new Space();
            space.setCode(projectIdentifier.getSpaceCode());
            project.setSpace(space);
            sample.setProject(project);
        } else if (sampleIdentifier.isSpaceLevel())
        {
            Space space = new Space();
            SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
            space.setCode(spaceLevel.getSpaceCode());
            sample.setSpace(space);
        }
        return this;
    }

    public SampleBuilder type(String typeCode)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(typeCode);
        return type(sampleType);
    }

    public SampleBuilder type(SampleType sampleType)
    {
        sample.setSampleType(sampleType);
        return this;
    }

    public SampleBuilder markDeleted()
    {
        sample.setDeletion(new Deletion());
        return this;
    }

    public SampleBuilder registrator(Person person)
    {
        sample.setRegistrator(person);
        return this;
    }

    public SampleBuilder date(Date date)
    {
        sample.setRegistrationDate(date);
        return this;
    }

    public SampleBuilder modificationDate(Date date)
    {
        sample.setModificationDate(date);
        return this;
    }

    public SampleBuilder version(int version)
    {
        sample.setVersion(version);
        return this;
    }

    public SampleBuilder experiment(Experiment experiment)
    {
        sample.setExperiment(experiment);
        return this;
    }

    public SampleBuilder permID(String permID)
    {
        sample.setPermId(permID);
        return this;
    }

    public SampleBuilder permLink(String permLink)
    {
        sample.setPermlink(permLink);
        return this;
    }

    public SampleBuilder childOf(Sample... parents)
    {
        sample.setParents(new LinkedHashSet<Sample>(Arrays.asList(parents)));
        return this;
    }

    public SampleBuilder partOf(Sample container)
    {
        sample.setContainer(container);
        return this;
    }

    public SampleBuilder parents(Sample... parents)
    {
        sample.setParents(new LinkedHashSet<Sample>(Arrays.asList(parents)));
        return this;
    }

    public PropertyBuilder property(String key)
    {
        List<IEntityProperty> properties = sample.getProperties();
        PropertyBuilder propertyBuilder = new PropertyBuilder(key);
        properties.add(propertyBuilder.getProperty());
        return propertyBuilder;
    }

    public SampleBuilder property(String key, String value)
    {
        property(key).value(value);
        return this;
    }

    public Sample getSample()
    {
        return sample;
    }
}
