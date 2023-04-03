/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SampleExpectations extends Expectations
{

    public SampleExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getSamples(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        List.of(
                                new SamplePermId("200001010000000-0001"), new SamplePermId("200001010000000-0002"),
                                new SamplePermId("200001010000000-0003"), new SamplePermId("200001010000000-0004"),
                                new SamplePermId("200001010000000-0005")
                        )
                )),
                with(any(SampleFetchOptions.class))
        );

        will(new CustomAction("getting samples")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final SampleFetchOptions fetchOptions = (SampleFetchOptions) invocation.getParameter(2);
                final PropertyAssignment namePropertyAssignment = getNamePropertyAssignment();
                final PropertyAssignment boxesCountPropertyAssignment = getBoxesCountPropertyAssignment();

                final SampleType sampleType = new SampleType();
                sampleType.setCode("STORAGE");
                sampleType.setPermId(new EntityTypePermId("STORAGE", EntityKind.SAMPLE));
                sampleType.setFetchOptions(fetchOptions.withType());
                sampleType.setPropertyAssignments(List.of(namePropertyAssignment, boxesCountPropertyAssignment));

                final SampleType defaultSampleType = new SampleType();
                defaultSampleType.setCode("DEFAULT");
                defaultSampleType.setPermId(new EntityTypePermId("DEFAULT", EntityKind.SAMPLE));
                defaultSampleType.setFetchOptions(fetchOptions.withType());
                defaultSampleType.setPropertyAssignments(List.of(namePropertyAssignment));

                final Space space = new Space();
                space.setCode("ELN_SETTINGS");
                space.setPermId(new SpacePermId("ELN_SETTINGS"));

                final Space defaultSpace = new Space();
                defaultSpace.setCode("DEFAULT");
                defaultSpace.setPermId(new SpacePermId("DEFAULT"));

                final Project project = new Project();
                project.setCode("STORAGES");
                project.setIdentifier(new ProjectIdentifier("/ELN_SETTINGS/STORAGES"));

                final Project defaultProject = new Project();
                defaultProject.setCode("DEFAULT");
                defaultProject.setIdentifier(new ProjectIdentifier("/DEFAULT/DEFAULT"));

                final Experiment experiment = new Experiment();
                experiment.setCode("STORAGES_COLLECTION");
                experiment.setIdentifier(new ExperimentIdentifier("/ELN_SETTINGS/STORAGES/STORAGES_COLLECTION"));

                final Experiment defaultExperiment = new Experiment();
                defaultExperiment.setCode("DEFAULT");
                defaultExperiment.setIdentifier(new ExperimentIdentifier("/DEFAULT/DEFAULT/DEFAULT"));

                final Calendar calendar = Calendar.getInstance();
                calendar.set(2023, Calendar.MARCH, 10, 17, 23, 44);
                final Date registrationDate = calendar.getTime();

                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final Person registrator = new Person();
                registrator.setUserId("system");

                final Person modifier = new Person();
                modifier.setUserId("test");

                final Sample[] samples = new Sample[5];

                samples[0] = new Sample();
                samples[0].setType(sampleType);
                samples[0].setFetchOptions(fetchOptions);
                samples[0].setPermId(new SamplePermId("200001010000000-0001"));
                samples[0].setCode("BENCH");
                samples[0].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null, "BENCH"));
                samples[0].setSpace(space);
                samples[0].setProject(project);
                samples[0].setExperiment(experiment);
                samples[0].setProperty("$NAME", "Bench");
                samples[0].setProperty("$STORAGE.BOX_NUM", "9999");
                samples[0].setRegistrator(registrator);
                samples[0].setModifier(modifier);
                samples[0].setRegistrationDate(registrationDate);
                samples[0].setModificationDate(modificationDate);

                samples[1] = new Sample();
                samples[1].setType(sampleType);
                samples[1].setFetchOptions(fetchOptions);
                samples[1].setPermId(new SamplePermId("200001010000000-0002"));
                samples[1].setCode("DEFAULT_STORAGE");
                samples[1].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null,
                        "DEFAULT_STORAGE"));
                samples[1].setSpace(space);
                samples[1].setProject(project);
                samples[1].setExperiment(experiment);
                samples[1].setProperty("$NAME", "Default Storage");
                samples[1].setProperty("$STORAGE.BOX_NUM", "1111");
                samples[1].setRegistrator(registrator);
                samples[1].setModifier(modifier);
                samples[1].setRegistrationDate(registrationDate);
                samples[1].setModificationDate(modificationDate);

                samples[2] = new Sample();
                samples[2].setType(defaultSampleType);
                samples[2].setFetchOptions(fetchOptions);
                samples[2].setPermId(new SamplePermId("200001010000000-0003"));
                samples[2].setCode("DEFAULT");
                samples[2].setIdentifier(new SampleIdentifier(defaultSpace.getCode(), defaultProject.getCode(), null,
                        "DEFAULT"));
                samples[2].setSpace(defaultSpace);
                samples[2].setProject(defaultProject);
                samples[2].setExperiment(defaultExperiment);
                samples[2].setProperty("$NAME", "Default");
                samples[2].setRegistrator(registrator);
                samples[2].setModifier(modifier);
                samples[2].setRegistrationDate(registrationDate);
                samples[2].setModificationDate(modificationDate);

                samples[3] = new Sample();
                samples[3].setType(sampleType);
                samples[3].setFetchOptions(fetchOptions);
                samples[3].setPermId(new SamplePermId("200001010000000-0004"));
                samples[3].setCode("CHILD_1");
                samples[3].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null,
                        "CHILD_1"));
                samples[3].setSpace(space);
                samples[3].setProject(project);
                samples[3].setExperiment(experiment);
                samples[3].setProperty("$NAME", "Child 1");
                samples[3].setProperty("$STORAGE.BOX_NUM", "1");
                samples[3].setRegistrator(registrator);
                samples[3].setModifier(modifier);
                samples[3].setRegistrationDate(registrationDate);
                samples[3].setModificationDate(modificationDate);

                samples[4] = new Sample();
                samples[4].setType(sampleType);
                samples[4].setFetchOptions(fetchOptions);
                samples[4].setPermId(new SamplePermId("200001010000000-0005"));
                samples[4].setCode("CHILD_2");
                samples[4].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null,
                        "CHILD_2"));
                samples[4].setSpace(space);
                samples[4].setProject(project);
                samples[4].setExperiment(experiment);
                samples[4].setProperty("$NAME", "Child 2");
                samples[4].setProperty("$STORAGE.BOX_NUM", "2");
                samples[4].setRegistrator(registrator);
                samples[4].setModifier(modifier);
                samples[4].setRegistrationDate(registrationDate);
                samples[4].setModificationDate(modificationDate);

                samples[0].setChildren(List.of(samples[3], samples[4]));
                samples[1].setChildren(List.of(samples[3], samples[4]));
                samples[3].setParents(List.of(samples[0], samples[1]));
                samples[4].setParents(List.of(samples[0], samples[1]));

                return Arrays.stream(samples).collect(Collectors.toMap(Sample::getPermId, Function.identity(),
                        (sample1, sample2) -> sample2, LinkedHashMap::new));
            }

            private PropertyAssignment getBoxesCountPropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("$STORAGE.BOX_NUM");
                propertyType.setLabel("Number of Boxes");
                propertyType.setDescription("Number of Boxes");
                propertyType.setDataType(DataType.INTEGER);
                propertyType.setManagedInternally(true);

                final PropertyAssignment propertyAssignment = new PropertyAssignment();
                propertyAssignment.setFetchOptions(getPropertyAssignmentFetchOptions());
                propertyAssignment.setPropertyType(propertyType);
                propertyAssignment.setMandatory(false);
                propertyAssignment.setShowInEditView(true);
                propertyAssignment.setSection("General info");

                return propertyAssignment;
            }

            private PropertyAssignment getNamePropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("$NAME");
                propertyType.setLabel("Name");
                propertyType.setDescription("Name");
                propertyType.setDataType(DataType.VARCHAR);
                propertyType.setManagedInternally(true);

                final PropertyAssignment propertyAssignment = new PropertyAssignment();
                propertyAssignment.setFetchOptions(getPropertyAssignmentFetchOptions());
                propertyAssignment.setPropertyType(propertyType);
                propertyAssignment.setMandatory(false);
                propertyAssignment.setShowInEditView(true);
                propertyAssignment.setSection("General info");

                return propertyAssignment;
            }

            private PropertyAssignmentFetchOptions getPropertyAssignmentFetchOptions()
            {
                final PropertyAssignmentFetchOptions fetchOptions = new PropertyAssignmentFetchOptions();
                fetchOptions.withPropertyType();
                return fetchOptions;
            }

        });
    }

}
