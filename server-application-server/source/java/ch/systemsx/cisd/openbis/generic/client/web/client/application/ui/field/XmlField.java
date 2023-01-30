/*
 * Copyright 2009 ETH Zuerich, CISD
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
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

/**
 * Text field allowing to specify an XML document. The document will be validated on blur (it should be a well-formed XML).
 * 
 * @author Piotr Buczek
 */
public class XmlField extends MultilineVarcharField
{

    public XmlField(String label, boolean mandatory)
    {
        super(label, mandatory);
        // default auto validation is switched off as it would be to slow
        // we use validation on blur here but the other option would be to increase validation delay
        setAutoValidate(false);
        setValidator(new XMLValidator());
        setValidateOnBlur(true);
        setEmptyText("XML document");
        getMessages().setBlankText("XML document required");
    }

    @Override
    protected void onFocus(ComponentEvent be)
    {
        // clearing invalid messages on focus is needed because automatic validation is turned off
        super.onFocus(be);
        clearInvalid();
    }

    /** {@link Validator} that checks if a value is a well formed XML document. */
    protected class XMLValidator implements Validator
    {
        @Override
        public String validate(Field<?> field, final String fieldValue)
        {
            // try to parse the value as an XML file - if it fails then it is not a well-formed XML
            try
            {
                XMLParser.parse(fieldValue);
            } catch (DOMParseException e)
            {
                return "Not a well-formed XML.\n" + e.getMessage();
            }

            // validated value is valid
            return null;
        }

    }

}
