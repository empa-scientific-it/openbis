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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class DataSetExpectations extends Expectations
{

    public DataSetExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getDataSets(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        List.of(new DataSetPermId("200001010000000-0001"),
                                new DataSetPermId("200001010000000-0002"),
                                new DataSetPermId("200001010000000-0003")))),
                with(any(DataSetFetchOptions.class)));

        will(new CustomAction("getting data sets")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final DataSetFetchOptions fetchOptions = (DataSetFetchOptions) invocation.getParameter(2);
                final DataSetType[] dataSetTypes = getDataSetTypes(fetchOptions.withType());
                final Space[] spaces = getSpaces();

                final Sample[] samples = new Sample[2];

                samples[0] = new Sample();
                samples[0].setIdentifier(new SampleIdentifier("/TEST/TEST_2"));

                samples[1] = new Sample();
                samples[1].setIdentifier(new SampleIdentifier("/TEST/TEST_3"));

                final Experiment experiment = new Experiment();
                experiment.setIdentifier(new ExperimentIdentifier("/TEST/TEST_1"));


                final DataSet[] dataSets = new DataSet[3];

                dataSets[0] = new DataSet();
                dataSets[0].setFetchOptions(fetchOptions);
                dataSets[0].setPermId(new DataSetPermId("200001010000000-0001"));
                dataSets[0].setCode("TEST_1");
                dataSets[0].setType(dataSetTypes[1]);
                dataSets[0].setExperiment(experiment);
                dataSets[0].setProperty("Name", "Test 1");
                dataSets[0].setProperty("Notes", "This is\nmultiline\ntext.");

                dataSets[1] = new DataSet();
                dataSets[1].setFetchOptions(fetchOptions);
                dataSets[1].setPermId(new DataSetPermId("200001010000000-0002"));
                dataSets[1].setCode("TEST_2");
                dataSets[1].setSample(samples[0]);
                dataSets[1].setType(dataSetTypes[0]);
                dataSets[1].setProperty("Name", "Test 2");
                dataSets[1].setProperty("Attachment", "file1.bin");

                dataSets[2] = new DataSet();
                dataSets[2].setFetchOptions(fetchOptions);
                dataSets[2].setPermId(new DataSetPermId("200001010000000-0003"));
                dataSets[2].setCode("TEST_3");
                dataSets[2].setSample(samples[1]);
                dataSets[2].setType(dataSetTypes[0]);
                dataSets[2].setProperty("Name", "Test 3");
                dataSets[2].setProperty("Attachment", "file2.bin");

                return Arrays.stream(dataSets).collect(Collectors.toMap(DataSet::getPermId,
                        Function.identity(), (dataSet1, dataSet2) -> dataSet2, LinkedHashMap::new));
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

            private DataSetType[] getDataSetTypes(final DataSetTypeFetchOptions typeFetchOptions)
            {
                final PropertyAssignment namePropertyAssignment = getNamePropertyAssignment();
                final PropertyAssignment attachmentPropertyAssignment = getAttachmentPropertyAssignment();
                final PropertyAssignment notesPropertyAssignment = getNotesPropertyAssignment();

                final DataSetType[] dataSetTypes = new DataSetType[2];

                dataSetTypes[0] = new DataSetType();
                dataSetTypes[0].setFetchOptions(typeFetchOptions);
                dataSetTypes[0].setPermId(new EntityTypePermId("ATTACHMENT"));
                dataSetTypes[0].setPropertyAssignments(
                        List.of(namePropertyAssignment, attachmentPropertyAssignment)
                );

                dataSetTypes[1] = new DataSetType();
                dataSetTypes[1].setFetchOptions(typeFetchOptions);
                dataSetTypes[1].setPermId(new EntityTypePermId("RAW_DATA"));
                dataSetTypes[1].setPropertyAssignments(List.of(namePropertyAssignment, notesPropertyAssignment));
                return dataSetTypes;
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

            private PropertyAssignment getAttachmentPropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("$ATTACHMENT");
                propertyType.setLabel("Attachment");
                propertyType.setDescription("Attachment");
                propertyType.setDataType(DataType.VARCHAR);

                final PropertyAssignment propertyAssignment = new PropertyAssignment();
                propertyAssignment.setFetchOptions(getPropertyAssignmentFetchOptions());
                propertyAssignment.setPropertyType(propertyType);
                propertyAssignment.setMandatory(false);
                propertyAssignment.setShowInEditView(true);
                propertyAssignment.setSection("General info");

                return propertyAssignment;
            }

            private PropertyAssignment getNotesPropertyAssignment()
            {
                final PropertyType propertyType = new PropertyType();
                propertyType.setCode("NOTES");
                propertyType.setLabel("Notes");
                propertyType.setDescription("Notes");
                propertyType.setDataType(DataType.MULTILINE_VARCHAR);

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
