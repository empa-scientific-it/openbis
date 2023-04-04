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

import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExportData.TEST_SCRIPT_CONTENT;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class ExperimentTypeExpectations extends Expectations
{

    public ExperimentTypeExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getExperimentTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(
                                new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT)))),
                with(any(ExperimentTypeFetchOptions.class)));

        will(new CustomAction("getting experiment types")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final ExperimentTypeFetchOptions fetchOptions =
                        (ExperimentTypeFetchOptions) invocation.getParameter(2);
                final PluginFetchOptions pluginFetchOptions = fetchOptions.withValidationPlugin();

                final Calendar calendar = Calendar.getInstance();
                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final ExperimentType experimentType = new ExperimentType();
                experimentType.setFetchOptions(fetchOptions);
                experimentType.setPermId(new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT));
                experimentType.setCode("DEFAULT_EXPERIMENT");
                experimentType.setDescription("Default experiment");
                experimentType.setPropertyAssignments(getPropertyAssignments(fetchOptions));
                experimentType.setModificationDate(modificationDate);

                final Plugin validationPlugin = new Plugin();
                validationPlugin.setName("test");
                validationPlugin.setScript(TEST_SCRIPT_CONTENT);
                validationPlugin.setFetchOptions(pluginFetchOptions);

                experimentType.setValidationPlugin(validationPlugin);

                return Collections.singletonMap(new EntityTypePermId("DEFAULT_EXPERIMENT"), experimentType);
            }

            private List<PropertyAssignment> getPropertyAssignments(final ExperimentTypeFetchOptions fetchOptions)
            {
                final PropertyAssignment[] propertyAssignments = new PropertyAssignment[4];

                propertyAssignments[0] = PropertyAssignmentFactory.createPropertyAssignment(
                        fetchOptions.withPropertyAssignments());
                propertyAssignments[0].getPropertyType().setCode("$NAME");
                propertyAssignments[0].setMandatory(false);
                propertyAssignments[0].setShowInEditView(true);
                propertyAssignments[0].setSection("General info");
                propertyAssignments[0].getPropertyType().setManagedInternally(true);
                propertyAssignments[0].getPropertyType().setLabel("Name");
                propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
                propertyAssignments[0].getPropertyType().setDescription("Name");

                propertyAssignments[1] = PropertyAssignmentFactory.createPropertyAssignment(
                        fetchOptions.withPropertyAssignments());
                propertyAssignments[1].getPropertyType().setCode("$DEFAULT_OBJECT_TYPE");
                propertyAssignments[1].setMandatory(false);
                propertyAssignments[1].setShowInEditView(true);
                propertyAssignments[1].setSection("General info");
                propertyAssignments[1].getPropertyType().setManagedInternally(true);
                propertyAssignments[1].getPropertyType().setLabel("Default object type");
                propertyAssignments[1].getPropertyType().setDataType(DataType.VARCHAR);
                propertyAssignments[1].getPropertyType().setDescription(
                        "Enter the code of the object type for which the collection is used");

                propertyAssignments[2] = PropertyAssignmentFactory.createPropertyAssignment(
                        fetchOptions.withPropertyAssignments());
                propertyAssignments[2].getPropertyType().setCode("NOTES");
                propertyAssignments[2].setMandatory(false);
                propertyAssignments[2].setShowInEditView(true);
                propertyAssignments[2].getPropertyType().setManagedInternally(false);
                propertyAssignments[2].getPropertyType().setLabel("Notes");
                propertyAssignments[2].getPropertyType().setDataType(DataType.MULTILINE_VARCHAR);
                propertyAssignments[2].getPropertyType().setDescription("Notes");
                propertyAssignments[2].getPropertyType().setMetaData(
                        Collections.singletonMap("custom_widget", "Word Processor"));

                propertyAssignments[3] = PropertyAssignmentFactory.createPropertyAssignment(
                        fetchOptions.withPropertyAssignments());
                propertyAssignments[3].getPropertyType().setCode("$XMLCOMMENTS");
                propertyAssignments[3].setMandatory(false);
                propertyAssignments[3].setShowInEditView(false);
                propertyAssignments[3].getPropertyType().setManagedInternally(true);
                propertyAssignments[3].getPropertyType().setLabel("Comments List");
                propertyAssignments[3].getPropertyType().setDataType(DataType.XML);
                propertyAssignments[3].getPropertyType().setDescription("Comments log");

                return Arrays.asList(propertyAssignments);
            }

        });
    }

}
