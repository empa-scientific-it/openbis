/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A text area to specify a script.
 * <p>
 * It is possible to type tabs in the field area.
 * 
 * @author Piotr Buczek
 */
public class ScriptField extends MultilineVarcharField
{

    private final static String BLANK_TEXT_MSG = "Script text required";

    public ScriptField(IMessageProvider messageProvider)
    {
        super(
                messageProvider
                        .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.PLUGIN_SCRIPT),
                true, 20);
        getMessages().setBlankText(BLANK_TEXT_MSG);
        treatTabKeyAsInput();
    }

}
