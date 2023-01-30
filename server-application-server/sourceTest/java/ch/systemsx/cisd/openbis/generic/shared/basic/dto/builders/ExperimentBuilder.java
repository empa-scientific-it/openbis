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
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Builder of an {@link Experiment} instance.
 * 
 * @author Franz-Josef Elmer
 */
public class ExperimentBuilder
{
    private static long count = 0;

    private final Experiment experiment = new Experiment();

    public ExperimentBuilder()
    {
        experiment.setProperties(new ArrayList<IEntityProperty>());
    }

    public ExperimentBuilder type(String experimentTypeCode)
    {
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(experimentTypeCode);
        experiment.setExperimentType(experimentType);
        return this;
    }

    public ExperimentBuilder id(long id)
    {
        experiment.setId(id);
        return this;
    }

    public ExperimentBuilder code(String code)
    {
        experiment.setCode(code);
        return this;
    }

    public ExperimentBuilder identifier(String identifier)
    {
        ExperimentIdentifierFactory factory = new ExperimentIdentifierFactory(identifier);
        ExperimentIdentifier experimentIdentifier = factory.createIdentifier();
        experiment.setCode(experimentIdentifier.getExperimentCode());
        Project project = new Project();
        ++count;
        project.setId(count);
        project.setPermId(Long.toString(count));
        project.setCode(experimentIdentifier.getProjectCode());
        Space space = new Space();
        space.setCode(experimentIdentifier.getSpaceCode());
        project.setSpace(space);
        experiment.setProject(project);
        experiment.setIdentifier(identifier);
        return this;
    }

    public ExperimentBuilder permID(String permID)
    {
        experiment.setPermId(permID);
        return this;
    }

    public ExperimentBuilder markDeleted()
    {
        experiment.setDeletion(new Deletion());
        return this;
    }

    public ExperimentBuilder registrator(String userID)
    {
        Person person = new Person();
        person.setUserId(userID);
        experiment.setRegistrator(person);
        return this;
    }

    public ExperimentBuilder registrator(Person registrator)
    {
        experiment.setRegistrator(registrator);
        return this;
    }

    public ExperimentBuilder date(Date date)
    {
        experiment.setRegistrationDate(date);
        return this;
    }

    public ExperimentBuilder modificationDate(Date date)
    {
        experiment.setModificationDate(date);
        return this;
    }

    public PropertyBuilder property(String key)
    {
        List<IEntityProperty> properties = experiment.getProperties();
        PropertyBuilder propertyBuilder = new PropertyBuilder(key);
        properties.add(propertyBuilder.getProperty());
        return propertyBuilder;
    }

    public ExperimentBuilder property(String key, String value)
    {
        property(key).value(value);
        return this;
    }

    public final Experiment getExperiment()
    {
        return experiment;
    }

}
