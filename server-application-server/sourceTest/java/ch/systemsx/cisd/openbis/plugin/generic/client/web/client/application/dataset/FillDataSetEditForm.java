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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextArea;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetParentsArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for editing data set.
 * 
 * @author Piotr Buczek
 */
// TODO 2009-09-21, Piotr Buczek: test modification of Sample
public final class FillDataSetEditForm extends AbstractDefaultTestCommand
{
    private final String formId;

    private String modifiedParentsOrNull;

    // private String newSampleOrNull;

    // private String newFileFormatTypeOrNull;

    private final List<PropertyField> properties;

    public FillDataSetEditForm()
    {
        this(TechId.createWildcardTechId());
    }

    private FillDataSetEditForm(final TechId dataSetId)
    {
        this.formId = AbstractGenericEntityRegistrationForm.createId(dataSetId, EntityKind.DATA_SET);
        this.properties = new ArrayList<PropertyField>();
    }

    public final FillDataSetEditForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    @Override
    public final void execute()
    {
        String simpleId = formId.substring(AbstractGenericEntityRegistrationForm.ID_PREFIX.length());
        for (final PropertyField property : properties)
        {
            GWTTestUtil.setPropertyFieldValue(formId, property);
        }
        if (modifiedParentsOrNull != null)
        {
            final TextArea parentsField =
                    (TextArea) GWTTestUtil.getWidgetWithID(DataSetParentsArea.createId(simpleId));
            parentsField.setRawValue(modifiedParentsOrNull);
        }
        GWTTestUtil.clickButtonWithID(formId + AbstractRegistrationForm.SAVE_BUTTON);
    }

    public final FillDataSetEditForm modifyParents(final String modifiedParents)
    {
        this.modifiedParentsOrNull = modifiedParents;
        return this;
    }

}
