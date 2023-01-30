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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.field;

import com.extjs.gxt.ui.client.widget.form.TriggerField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.MetaprojectNameConstants;

/**
 * Metaproject name field.
 * 
 * @author pkupczyk
 */
public class MetaprojectNameField extends TriggerField<String>
{
    public MetaprojectNameField(final IMessageProvider messageProvider)
    {
        VarcharField.configureField(this, messageProvider.getMessage(Dict.NAME), true);
        setMaxLength(MetaprojectNameConstants.MAX_LENGTH);
        setRegex(MetaprojectNameConstants.PATTERN);
        getMessages().setMaxLengthText(MetaprojectNameConstants.MAX_LENGTH_ERROR_MESSAGE);
        getMessages().setRegexText(MetaprojectNameConstants.PATTERN_ERROR_MESSAGE);
        setHideTrigger(true);
    }

}
