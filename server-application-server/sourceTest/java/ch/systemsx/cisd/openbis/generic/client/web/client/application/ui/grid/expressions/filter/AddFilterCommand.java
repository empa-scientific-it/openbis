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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.common.AbstractGridCustomExpressionEditOrRegisterDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * Opens "Add Filter" dialog, fills the form and saves the new filter.
 * 
 * @author Izabela Adamczyk
 */
public final class AddFilterCommand extends CheckTableCommand
{

    private final String gridDisplayId;

    private final String name;

    private final String description;

    private final String expression;

    private final boolean isPublic;

    public AddFilterCommand(String gridDisplayId, String name, String description,
            String expression, boolean isPublic)
    {
        super(GridCustomFilterGrid.createGridId(gridDisplayId));
        this.gridDisplayId = gridDisplayId;
        this.name = name;
        this.description = description;
        this.expression = expression;
        this.isPublic = isPublic;
    }

    @Override
    public final void execute()
    {
        GWTTestUtil.clickButtonWithID(GridCustomFilterGrid.createAddButtonId(gridDisplayId));
        GWTTestUtil.getTextFieldWithID(
                AbstractGridCustomExpressionEditOrRegisterDialog.createId(gridDisplayId,
                        AbstractGridCustomExpressionEditOrRegisterDialog.NAME_FIELD))
                .setValue(name);
        GWTTestUtil.getTextAreaWithId(
                AbstractGridCustomExpressionEditOrRegisterDialog.createId(gridDisplayId,
                        AbstractGridCustomExpressionEditOrRegisterDialog.DESCRIPTION_FIELD))
                .setValue(description);
        GWTTestUtil.getTextAreaWithId(
                AbstractGridCustomExpressionEditOrRegisterDialog.createId(gridDisplayId,
                        AbstractGridCustomExpressionEditOrRegisterDialog.EXPRESSION_FIELD))
                .setValue(expression);
        GWTTestUtil.getCheckboxWithId(
                (AbstractGridCustomExpressionEditOrRegisterDialog.createId(gridDisplayId,
                        AbstractGridCustomExpressionEditOrRegisterDialog.PUBLIC_FIELD))).setValue(
                isPublic);
        GWTTestUtil.clickButtonWithID(AbstractSaveDialog.SAVE_BUTTON_ID);
    }

}
