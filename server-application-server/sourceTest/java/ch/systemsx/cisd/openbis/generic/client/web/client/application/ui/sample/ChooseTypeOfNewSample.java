/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for choosing the type of new sample.
 * 
 * @author Izabela Adamczyk
 */
public final class ChooseTypeOfNewSample extends AbstractDefaultTestCommand
{
    private final String sampleTypeNameOrNull;

    public ChooseTypeOfNewSample(final String sampleTypeNameOrNull)
    {
        this.sampleTypeNameOrNull = sampleTypeNameOrNull;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        final ComboBox<SampleTypeModel> sampleTypeSelector =
                (SampleTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(DropDownList.ID
                                + SampleTypeSelectionWidget.SUFFIX + SampleRegistrationPanel.ID);
        GWTUtils.setSelectedItem(sampleTypeSelector, ModelDataPropertyNames.CODE,
                sampleTypeNameOrNull);
    }
}
