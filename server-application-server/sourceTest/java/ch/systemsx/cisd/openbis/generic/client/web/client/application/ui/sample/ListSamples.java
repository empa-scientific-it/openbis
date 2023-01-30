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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample list.
 * 
 * @author Christian Ribeaud
 */
public class ListSamples extends AbstractDefaultTestCommand
{
    private final String groupNameOrNull;

    private final String sampleTypeNameOrNull;

    public ListSamples(final String groupNameOrNull, final String sampleTypeNameOrNull)
    {
        this.groupNameOrNull = groupNameOrNull;
        this.sampleTypeNameOrNull = sampleTypeNameOrNull;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public void execute()
    {
        final SpaceSelectionWidget groupSelector =
                (SpaceSelectionWidget) GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + SpaceSelectionWidget.SUFFIX + SampleBrowserToolbar.ID);

        final ComboBox<SampleTypeModel> sampleTypeSelector =
                (SampleTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(DropDownList.ID
                                + SampleTypeSelectionWidget.SUFFIX + SampleBrowserToolbar.ID);

        // if 'all' type was initially selected group selection would trigger an unwanted callback
        GWTUtils.unselect(sampleTypeSelector);

        GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);
        GWTUtils.setSelectedItem(sampleTypeSelector, ModelDataPropertyNames.CODE,
                sampleTypeNameOrNull);
    }
}
