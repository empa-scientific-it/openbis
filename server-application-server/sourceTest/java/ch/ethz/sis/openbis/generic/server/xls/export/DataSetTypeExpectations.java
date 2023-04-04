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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class DataSetTypeExpectations extends Expectations
{

    public DataSetTypeExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.MARCH, 10, 17, 23, 44);

        final Date modificationDate = calendar.getTime();

        allowing(api).getDataSetTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(
                                new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET)))),
                with(any(DataSetTypeFetchOptions.class)));

        will(new CustomAction("getting data set types")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final DataSetTypeFetchOptions fetchOptions =
                        (DataSetTypeFetchOptions) invocation.getParameter(2);

                final DataSetType dataSetType = new DataSetType();
                dataSetType.setFetchOptions(fetchOptions);
                dataSetType.setPermId(new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET));
                dataSetType.setCode("ATTACHMENT");
                dataSetType.setDescription("Attachment");
                dataSetType.setPropertyAssignments(getPropertyAssignments(fetchOptions));
                dataSetType.setMainDataSetPattern(".*\\.jpg");
                dataSetType.setMainDataSetPath("original/images/");
                dataSetType.setDisallowDeletion(false);
                dataSetType.setModificationDate(modificationDate);

                return Collections.singletonMap(new EntityTypePermId("ATTACHMENT"), dataSetType);
            }

            private List<PropertyAssignment> getPropertyAssignments(final DataSetTypeFetchOptions fetchOptions)
            {
                final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions =
                        fetchOptions.withPropertyAssignments();

                final PropertyAssignment[] propertyAssignments = new PropertyAssignment[3];

                propertyAssignments[0] = PropertyAssignmentFactory.createPropertyAssignment(
                        propertyAssignmentFetchOptions);
                propertyAssignments[0].getPropertyType().setCode("$NAME");
                propertyAssignments[0].setMandatory(false);
                propertyAssignments[0].setShowInEditView(true);
                propertyAssignments[0].setSection("General info");
                propertyAssignments[0].getPropertyType().setManagedInternally(true);
                propertyAssignments[0].getPropertyType().setLabel("Name");
                propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
                propertyAssignments[0].getPropertyType().setDescription("Name");

                propertyAssignments[1] = PropertyAssignmentFactory.createPropertyAssignment(
                        propertyAssignmentFetchOptions);
                propertyAssignments[1].getPropertyType().setCode("NOTES");
                propertyAssignments[1].setMandatory(false);
                propertyAssignments[1].setShowInEditView(true);
                propertyAssignments[1].setSection("Comments");
                propertyAssignments[1].getPropertyType().setManagedInternally(false);
                propertyAssignments[1].getPropertyType().setLabel("Notes");
                propertyAssignments[1].getPropertyType().setDataType(DataType.MULTILINE_VARCHAR);
                propertyAssignments[1].getPropertyType().setDescription("Notes");
                propertyAssignments[1].getPropertyType().setMetaData(
                        Collections.singletonMap("custom_widget", "Word Processor"));

                propertyAssignments[2] = PropertyAssignmentFactory.createPropertyAssignment(
                        propertyAssignmentFetchOptions);
                propertyAssignments[2].getPropertyType().setCode("$XMLCOMMENTS");
                propertyAssignments[2].setMandatory(false);
                propertyAssignments[2].setShowInEditView(false);
                propertyAssignments[2].getPropertyType().setManagedInternally(true);
                propertyAssignments[2].getPropertyType().setLabel("Comments List");
                propertyAssignments[2].getPropertyType().setDataType(DataType.XML);
                propertyAssignments[2].getPropertyType().setDescription("Comments log");

                return Arrays.asList(propertyAssignments);
            }

        });
    }

}
