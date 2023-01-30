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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.shared.basic.ValidationUtilities.HyperlinkValidationHelper;

/**
 * A {@link TextField} extension for registering text (<code>String</code>) that will be rendered as a link. Default protocol ("http://") is added on
 * blur if user doesn't provide one.<br>
 * The text characters are validated.
 * 
 * @author Piotr Buczek
 */
public class HyperlinkField extends VarcharField
{

    public HyperlinkField(final String label, final boolean mandatory)
    {
        super(label, mandatory);
        // default auto validation is bad if we want to add a prefix automatically
        // we use validation on blur here but the other option would be to increase validation delay
        setAutoValidate(false);
        setValidator(new HyperlinkValidator());
        setValidateOnBlur(true);
        setEmptyText("Hyperlink");
        getMessages().setBlankText("Hyperlink required");
    }

    @Override
    protected void onFocus(ComponentEvent be)
    {
        // clearing invalid messages on focus is needed because automatic validation is turned off
        super.onFocus(be);
        clearInvalid();
    }

    /** {@link Validator} for external hyperlink value. */
    protected class HyperlinkValidator implements Validator
    {
        private final static String PROTOCOL_PART = "://";

        private final static String DEFAULT_PROTOCOL = "http://";

        @Override
        public String validate(Field<?> field, final String fieldValue)
        {
            // add default protocol if none is provided
            String validatedValue = fieldValue;
            if (validatedValue.contains(PROTOCOL_PART) == false)
            {
                validatedValue = DEFAULT_PROTOCOL + fieldValue;
                field.setRawValue(validatedValue);
            }

            // validate protocols and format
            if (HyperlinkValidationHelper.isProtocolValid(validatedValue) == false)
            {
                return "Hyperlink should start with one of the following protocols: "
                        + HyperlinkValidationHelper.getValidProtocolsAsString();
            }
            if (HyperlinkValidationHelper.isFormatValid(validatedValue) == false)
            {
                return "Hyperlink has improper format";
            }
            // validated value is valid
            return null;
        }

    }
}
