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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating materials list.
 * 
 * @author Izabela Adamczyk
 */
public class ListMaterials extends AbstractDefaultTestCommand
{
    private final String materialTypeName;

    public ListMaterials(final String materialTypeNameOrNull)
    {
        this.materialTypeName = materialTypeNameOrNull;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public void execute()
    {

        final ComboBox<MaterialTypeModel> typeSelector =
                (MaterialTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(DropDownList.ID
                                + MaterialTypeSelectionWidget.SUFFIX + MaterialBrowserToolbar.ID);

        GWTUtils.setSelectedItem(typeSelector, ModelDataPropertyNames.CODE, materialTypeName);
    }
}
