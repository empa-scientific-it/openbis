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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * {@link CodeField} extension for registering {@link VocabularyTerm} that may be optional.
 * 
 * @author Piotr Buczek
 */
public class VocabularyTermField extends CodeField
{
    public VocabularyTermField(IViewContext<?> viewContext, String label, boolean isMandatory)
    {
        super(viewContext, label, CodeFieldKind.CODE_WITH_COLON);
        setEmptyText("Vocabulary term code");
        // by default CodeField is mandatory
        if (isMandatory == false)
        {
            this.setLabelSeparator("");
            this.setAllowBlank(true);
        }
    }

}
