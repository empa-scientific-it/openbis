/*
 * Copyright 2020 ETH Zuerich, SIS
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

import com.extjs.gxt.ui.client.widget.form.DateField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;

/**
 * @author Franz-Josef Elmer
 *
 */
public class DateDateFormField extends DateField
{
    public DateDateFormField(final String fieldLabel, final boolean mandatory)
    {
        VarcharField.configureField(this, fieldLabel, mandatory);
        getPropertyEditor().setFormat(DateRenderer.SHORT_DATE_TIME_FORMAT);
        final String pattern = DateRenderer.SHORT_DATE_FORMAT_PATTERN;
        setEmptyText(pattern.toUpperCase());
        setMaxLength(pattern.length() + 6);
    }
}
