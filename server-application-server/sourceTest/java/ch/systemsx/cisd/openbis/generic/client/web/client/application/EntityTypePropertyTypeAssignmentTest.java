/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.CheckPropertyTypeAssignmentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.FillPropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A {@link AbstractGWTTestCase} extension to test Entity Property Type Assignment.
 * 
 * @author Izabela Adamczyk
 */
public class EntityTypePropertyTypeAssignmentTest extends AbstractGWTTestCase
{

    private static final String CISD = "CISD";

    private static final String CONTROL_LAYOUT_C1 = "C1";

    private static final String NO_COMMENT = "No comment.";

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String HCS_IMAGE = "HCS_IMAGE";

    private static final String COMMENT = "Comment";

    private static final String DESCRIPTION = "Description";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    private static final EntityKind EXPERIMENT = EntityKind.EXPERIMENT;

    private static final EntityKind SAMPLE = EntityKind.SAMPLE;

    private static final EntityKind DATA_SET = EntityKind.DATA_SET;

    private final void prepareListingAfterAssignment(String propertyTypeLabel,
            String entityTypeCode, EntityKind entityKind, int expectedEntries, boolean isMandatory)
    {
        remoteConsole.prepare(new InvokeActionMenu(
                ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS));
        CheckPropertyTypeAssignmentTable table = new CheckPropertyTypeAssignmentTable();
        table.expectedRow(new Row()
                .withCell(PropertyTypeAssignmentGridColumnIDs.LABEL, propertyTypeLabel)
                .withCell(PropertyTypeAssignmentGridColumnIDs.ASSIGNED_TO, entityTypeCode)
                .withCell(PropertyTypeAssignmentGridColumnIDs.TYPE_OF, entityKind.getDescription())
                .withCell(PropertyTypeAssignmentGridColumnIDs.IS_MANDATORY,
                        SimpleYesNoRenderer.render(isMandatory)));
        remoteConsole.prepare(table.expectedSize(expectedEntries));
    }

    public final void testAssignExperimentPropertyType()
    {
        loginAndInvokeAction(ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE);
        final boolean mandatory = true;
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(mandatory, COMMENT, SIRNA_HCS,
                "a comment", EXPERIMENT));
        prepareListingAfterAssignment(COMMENT, SIRNA_HCS, EXPERIMENT, 36, mandatory);
        launchTest();
    }

    public final void testAssignDataSetPropertyType()
    {
        loginAndInvokeAction(ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE);
        final boolean mandatory = false;
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(mandatory, DESCRIPTION, HCS_IMAGE,
                null, DATA_SET));
        prepareListingAfterAssignment(DESCRIPTION, HCS_IMAGE, DATA_SET, 37, mandatory);
        launchTest();
    }

    public final void testGlobalValueAssignmentSamplePropertyType()
    {
        loginAndInvokeAction(ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE);
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(false, COMMENT, CONTROL_LAYOUT,
                NO_COMMENT, SAMPLE));
        remoteConsole.prepare(new InvokeActionMenu(ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples(CISD, CONTROL_LAYOUT));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow(CONTROL_LAYOUT_C1).identifier(CISD, CISD).valid()
                .withUserPropertyCell(COMMENT, NO_COMMENT));
        launchTest();
    }
}
