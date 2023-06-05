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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
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
                fetchOptions.withPhysicalData();
                fetchOptions.withParents();
                fetchOptions.withChildren();

                final Sample[] samples = new Sample[2];

                final Experiment sampleExperiment = new Experiment();
                sampleExperiment.setIdentifier(new ExperimentIdentifier("/TEST/TEST"));

                samples[0] = new Sample();
                samples[0].setIdentifier(new SampleIdentifier("/TEST/TEST/TEST_2"));

                samples[1] = new Sample();
                samples[1].setIdentifier(new SampleIdentifier("/TEST/TEST/TEST_3"));

                final Experiment experiment = new Experiment();
                experiment.setIdentifier(new ExperimentIdentifier("/TEST/TEST_1"));

                final Calendar calendar = Calendar.getInstance();
                calendar.set(2023, Calendar.MARCH, 10, 17, 23, 44);
                final Date registrationDate = calendar.getTime();

                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final Person registrator = new Person();
                registrator.setUserId("system");

                final Person modifier = new Person();
                modifier.setUserId("test");

                final PhysicalData[] physicalData = new PhysicalData[3];
                physicalData[0] = new PhysicalData();
                physicalData[0].setArchivingRequested(false);
                physicalData[0].setPresentInArchive(true);
                physicalData[0].setStorageConfirmation(true);
                physicalData[0].setStatus(ArchivingStatus.AVAILABLE);

                physicalData[1] = new PhysicalData();
                physicalData[1].setArchivingRequested(true);
                physicalData[1].setPresentInArchive(false);
                physicalData[1].setStorageConfirmation(false);
                physicalData[1].setStatus(ArchivingStatus.AVAILABLE);

                physicalData[2] = new PhysicalData();
                physicalData[2].setArchivingRequested(true);
                physicalData[2].setPresentInArchive(true);
                physicalData[2].setStorageConfirmation(false);
                physicalData[2].setStatus(ArchivingStatus.ARCHIVED);

                final DataSet[] dataSets = new DataSet[3];

                dataSets[0] = new DataSet();
                dataSets[0].setFetchOptions(fetchOptions);
                dataSets[0].setPermId(new DataSetPermId("200001010000000-0001"));
                dataSets[0].setCode("TEST_1");
                dataSets[0].setPhysicalData(physicalData[0]);
                dataSets[0].setType(dataSetTypes[1]);
                dataSets[0].setExperiment(experiment);
                dataSets[0].setProperty("$NAME", "<b>Test 1</b>");
                dataSets[0].setProperty("NOTES", "<body><p><i>This is></i><br/>\n<b>multi</b>line<br/>\n<u>text</u>.</p></body>");
                dataSets[0].setRegistrator(registrator);
                dataSets[0].setModifier(modifier);
                dataSets[0].setRegistrationDate(registrationDate);
                dataSets[0].setModificationDate(modificationDate);

                dataSets[1] = new DataSet();
                dataSets[1].setFetchOptions(fetchOptions);
                dataSets[1].setPermId(new DataSetPermId("200001010000000-0002"));
                dataSets[1].setCode("TEST_2");
                dataSets[1].setPhysicalData(physicalData[1]);
                dataSets[1].setSample(samples[0]);
                dataSets[1].setExperiment(sampleExperiment);
                dataSets[1].setType(dataSetTypes[0]);
                dataSets[1].setProperty("$NAME", "<i>Test 2</i>");
                dataSets[1].setProperty("$ATTACHMENT", "file1.bin");
                dataSets[1].setRegistrator(registrator);
                dataSets[1].setModifier(modifier);
                dataSets[1].setRegistrationDate(registrationDate);
                dataSets[1].setModificationDate(modificationDate);
                dataSets[1].setParents(List.of(dataSets[0]));

                dataSets[2] = new DataSet();
                dataSets[2].setFetchOptions(fetchOptions);
                dataSets[2].setPermId(new DataSetPermId("200001010000000-0003"));
                dataSets[2].setCode("TEST_3");
                dataSets[2].setPhysicalData(physicalData[2]);
                dataSets[2].setSample(samples[1]);
                dataSets[2].setExperiment(sampleExperiment);
                dataSets[2].setType(dataSetTypes[0]);
                dataSets[2].setProperty("$NAME", "Test 3");
                dataSets[2].setProperty("$ATTACHMENT", "file2.bin");
                dataSets[2].setRegistrator(registrator);
                dataSets[2].setModifier(modifier);
                dataSets[2].setRegistrationDate(registrationDate);
                dataSets[2].setModificationDate(modificationDate);
                dataSets[2].setParents(List.of(dataSets[0]));

                dataSets[0].setChildren(List.of(dataSets[1], dataSets[2]));

                return Arrays.stream(dataSets).collect(Collectors.toMap(DataSet::getPermId,
                        Function.identity(), (dataSet1, dataSet2) -> dataSet2, LinkedHashMap::new));
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
                dataSetTypes[0].setPermId(new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET));
                dataSetTypes[0].setPropertyAssignments(
                        List.of(namePropertyAssignment, attachmentPropertyAssignment)
                );

                dataSetTypes[1] = new DataSetType();
                dataSetTypes[1].setFetchOptions(typeFetchOptions);
                dataSetTypes[1].setPermId(new EntityTypePermId("RAW_DATA", EntityKind.DATA_SET));
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
                propertyType.setManagedInternally(true);

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
                propertyType.setManagedInternally(true);

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
                propertyType.setManagedInternally(false);

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
