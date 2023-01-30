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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;

/**
 * A small {@link CheckBox} extension for registering a boolean. Created field will always be aligned to the left.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
public final class CheckBoxField extends CheckBox
{
    public CheckBoxField(final String labelField, final boolean mandatory)
    {
        setFieldLabel(labelField);
        FieldUtil.setMandatoryFlag(this, mandatory);
    }

    @Override
    // always align checkbox to the left (default implementation aligns to center)
    protected void alignElements()
    {
        input.alignTo(getElement(), "l-l", new int[]
        { 0, 0 });
        if (getBoxLabel() != null)
        {
            boxLabelEl.alignTo(input.dom, "l-r", new int[]
            { 5, 0 });
        }
    }

}
