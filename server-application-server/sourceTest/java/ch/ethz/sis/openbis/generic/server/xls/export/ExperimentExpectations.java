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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class ExperimentExpectations extends Expectations
{

    public ExperimentExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getExperiments(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        List.of(new ExperimentPermId("200001010000000-0001"),
                                new ExperimentPermId("200001010000000-0002"),
                                new ExperimentPermId("200001010000000-0003")))),
                with(any(ExperimentFetchOptions.class)));

        will(new CustomAction("getting experiments")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final ExperimentFetchOptions fetchOptions = (ExperimentFetchOptions) invocation.getParameter(2);
                final ExperimentType[] experimentTypes = getExperimentTypes(fetchOptions.withType());
                final Space[] spaces = getSpaces();
                final Project[] projects = getProjects(spaces);

                final Experiment[] experiments = new Experiment[3];

                experiments[0] = new Experiment();
                experiments[0].setFetchOptions(fetchOptions);
                experiments[0].setPermId(new ExperimentPermId("200001010000000-0001"));
                experiments[0].setIdentifier(new ExperimentIdentifier("/ELN_SETTINGS/STORAGES/STORAGES_COLLECTION"));
                experiments[0].setCode("STORAGES_COLLECTION");
                experiments[0].setProject(projects[0]);
                experiments[0].setType(experimentTypes[0]);
                experiments[0].setProperty("$NAME", "a".repeat(Short.MAX_VALUE + 1));
                experiments[0].setProperty("$DEFAULT_OBJECT_TYPE", "EXPERIMENTAL_STEP");

                experiments[1] = new Experiment();
                experiments[1].setFetchOptions(fetchOptions);
                experiments[1].setPermId(new ExperimentPermId("200001010000000-0002"));
                experiments[1].setIdentifier(new ExperimentIdentifier("/DEFAULT/DEFAULT/DEFAULT"));
                experiments[1].setCode("DEFAULT");
                experiments[1].setProject(projects[1]);
                experiments[1].setType(experimentTypes[1]);
                experiments[1].setProperty("$NAME", "Default");
                experiments[1].setProperty("FINISHED_FLAG", "FALSE");

                experiments[2] = new Experiment();
                experiments[2].setFetchOptions(fetchOptions);
                experiments[2].setPermId(new ExperimentPermId("200001010000000-0003"));
                experiments[2].setIdentifier(new ExperimentIdentifier("/TEST/TEST/TEST"));
                experiments[2].setCode("TEST");
                experiments[2].setProject(projects[2]);
                experiments[2].setType(experimentTypes[0]);
                experiments[2].setProperty("$NAME", "b".repeat(Short.MAX_VALUE + 1));
                experiments[2].setProperty("$DEFAULT_OBJECT_TYPE", "DEFAULT_SAMPLE");

                return Arrays.stream(experiments).collect(Collectors.toMap(Experiment::getIdentifier,
                        Function.identity(), (experiment1, experiment2) -> experiment2, LinkedHashMap::new));
            }

            private Project[] getProjects(final Space[] spaces)
            {
                final Project[] projects = new Project[3];

                projects[0] = new Project();
                projects[0].setPermId(new ProjectPermId("200001010000000-0001"));
                projects[0].setIdentifier(new ProjectIdentifier("/ELN_SETTINGS/STORAGES"));
                projects[0].setCode("STORAGES");
                projects[0].setDescription("Storages");
                projects[0].setSpace(spaces[0]);

                projects[1] = new Project();
                projects[1].setPermId(new ProjectPermId("200001010000000-0002"));
                projects[1].setIdentifier(new ProjectIdentifier("/DEFAULT/DEFAULT"));
                projects[1].setCode("DEFAULT");
                projects[1].setDescription("Default");
                projects[1].setSpace(spaces[1]);

                projects[2] = new Project();
                projects[2].setPermId(new ProjectPermId("200001010000000-0003"));
                projects[2].setIdentifier(new ProjectIdentifier("/TEST/TEST"));
                projects[2].setCode("TEST");
                projects[2].setDescription("Test");
                projects[2].setSpace(spaces[2]);

                return projects;
            }

            private Space[] getSpaces()
            {
                final Space[] spaces = new Space[3];

                spaces[0] = new Space();
                spaces[0].setCode("ELN_SETTINGS");

                spaces[1] = new Space();
                spaces[1].setCode("DEFAULT");

                spaces[2] = new Space();
                spaces[2].setCode("TEST");

                return spaces;
            }

            private ExperimentType[] getExperimentTypes(final ExperimentTypeFetchOptions typeFetchOptions)
            {
                final PropertyAssignment namePropertyAssignment = getNamePropertyAssignment();
                final PropertyAssignment defaultObjectTypePropertyAssignment = getDefaultObjectTypePropertyAssignment();
                final PropertyAssignment finishedPropertyAssignment = getFinishedPropertyAssignment();

                final ExperimentType[] experimentTypes = new ExperimentType[2];

                experimentTypes[0] = new ExperimentType();
                experimentTypes[0].setFetchOptions(typeFetchOptions);
                experimentTypes[0].setPermId(new EntityTypePermId("COLLECTION"));
                experimentTypes[0].setPropertyAssignments(
                        List.of(namePropertyAssignment, defaultObjectTypePropertyAssignment)
                );

                experimentTypes[1] = new ExperimentType();
                experimentTypes[1].setFetchOptions(typeFetchOptions);
                experimentTypes[1].setPermId(new EntityTypePermId("DEFAULT_EXPERIMENT"));
                experimentTypes[1].setPropertyAssignments(List.of(namePropertyAssignment, finishedPropertyAssignment));
                return experimentTypes;
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

            private PropertyAssignment getDefaultObjectTypePropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("$DEFAULT_OBJECT_TYPE");
                propertyType.setLabel("Default object type");
                propertyType.setDescription("Enter the code of the object type for which the collection is used");
                propertyType.setDataType(DataType.VARCHAR);

                final PropertyAssignment propertyAssignment = new PropertyAssignment();
                propertyAssignment.setFetchOptions(getPropertyAssignmentFetchOptions());
                propertyAssignment.setPropertyType(propertyType);
                propertyAssignment.setMandatory(false);
                propertyAssignment.setShowInEditView(true);
                propertyAssignment.setSection("General info");

                return propertyAssignment;
            }

            private PropertyAssignment getFinishedPropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("FINISHED_FLAG");
                propertyType.setLabel("Experiment completed");
                propertyType.setDescription("Marks the experiment as finished");
                propertyType.setDataType(DataType.BOOLEAN);

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
