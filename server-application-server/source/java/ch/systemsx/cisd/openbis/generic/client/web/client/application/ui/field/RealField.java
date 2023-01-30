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

import com.extjs.gxt.ui.client.widget.form.NumberField;

/**
 * A {@link NumberField} extension for registering an {@link Double}.
 * 
 * @author Christian Ribeaud
 */
public class RealField extends NumberField
{
    public RealField(final String fieldLabel, final boolean mandatory)
    {
        VarcharField.configureField(this, fieldLabel, mandatory);
        getMessages().setBlankText("Real number required");
        setEmptyText("Real number value");
        setBaseChars("0123456789-+eE");
    }
}
