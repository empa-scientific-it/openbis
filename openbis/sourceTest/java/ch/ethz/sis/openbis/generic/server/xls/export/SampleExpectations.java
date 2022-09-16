package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
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
                                new SamplePermId("200001010000000-0003"), new SamplePermId("200001010000000-0004")
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
                sampleType.setPermId(new EntityTypePermId("STORAGE"));
                sampleType.setFetchOptions(fetchOptions.withType());
                sampleType.setPropertyAssignments(List.of(namePropertyAssignment, boxesCountPropertyAssignment));

                final Space space = new Space();
                space.setCode("ELN_SETTINGS");
                space.setPermId(new SpacePermId("ELN_SETTINGS"));

                final Project project = new Project();
                project.setCode("STORAGES");
                project.setIdentifier(new ProjectIdentifier("/ELN_SETTINGS/STORAGES"));

                final Experiment experiment = new Experiment();
                experiment.setCode("STORAGES_COLLECTION");
                experiment.setIdentifier(new ExperimentIdentifier("/ELN_SETTINGS/STORAGES/STORAGES_COLLECTION"));

                final Sample[] samples = new Sample[4];

                samples[0] = new Sample();
                samples[0].setType(sampleType);
                samples[0].setFetchOptions(fetchOptions);
                samples[0].setPermId(new SamplePermId("200001010000000-0001"));
                samples[0].setCode("BENCH");
                samples[0].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null, "BENCH"));
                samples[0].setSpace(space);
                samples[0].setProject(project);
                samples[0].setExperiment(experiment);
                samples[0].setProperty("Name", "Bench");
                samples[0].setProperty("Number of Boxes", "9999");

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
                samples[1].setProperty("Name", "Default Storage");
                samples[1].setProperty("Number of Boxes", "1111");

                samples[2] = new Sample();
                samples[2].setType(sampleType);
                samples[2].setFetchOptions(fetchOptions);
                samples[2].setPermId(new SamplePermId("200001010000000-0003"));
                samples[2].setCode("CHILD_1");
                samples[2].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null,
                        "CHILD_1"));
                samples[2].setSpace(space);
                samples[2].setProject(project);
                samples[2].setExperiment(experiment);
                samples[2].setProperty("Name", "Child 1");
                samples[2].setProperty("Number of Boxes", "1");

                samples[3] = new Sample();
                samples[3].setType(sampleType);
                samples[3].setFetchOptions(fetchOptions);
                samples[3].setPermId(new SamplePermId("200001010000000-0004"));
                samples[3].setCode("CHILD_2");
                samples[3].setIdentifier(new SampleIdentifier(space.getCode(), project.getCode(), null,
                        "CHILD_2"));
                samples[3].setSpace(space);
                samples[3].setProject(project);
                samples[3].setExperiment(experiment);
                samples[3].setProperty("Name", "Child 2");
                samples[3].setProperty("Number of Boxes", "2");

                samples[0].setChildren(List.of(samples[2], samples[3]));
                samples[1].setChildren(List.of(samples[2], samples[3]));
                samples[2].setParents(List.of(samples[0], samples[1]));
                samples[3].setParents(List.of(samples[0], samples[1]));

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
