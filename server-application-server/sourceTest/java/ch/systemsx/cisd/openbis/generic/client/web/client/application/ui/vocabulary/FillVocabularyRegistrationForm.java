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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link VocabularyRegistrationForm}.
 * 
 * @author Christian Ribeaud
 */
public final class FillVocabularyRegistrationForm extends AbstractDefaultTestCommand
{
    private final String code;

    private final String descriptionOrNull;

    private final String[] terms;

    public FillVocabularyRegistrationForm(final String code, final String descriptionOrNull,
            final String... terms)
    {
        assert code != null : "Unspecified code.";
        assert terms.length > 0 : "Unspecified vocabulary terms.";
        this.code = code;
        this.descriptionOrNull = descriptionOrNull;
        this.terms = terms;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        GWTTestUtil.setTextField(VocabularyRegistrationForm.ID_PREFIX + "form"
                + VocabularyRegistrationFieldSet.ID + "_code", code);
        if (StringUtils.isBlank(descriptionOrNull) == false)
        {
            GWTTestUtil.setTextField(VocabularyRegistrationForm.ID_PREFIX + "form"
                    + VocabularyRegistrationFieldSet.ID + "_description", descriptionOrNull);
        }
        GWTTestUtil.setTextAreaValue(VocabularyRegistrationForm.ID_PREFIX + "form"
                + VocabularyRegistrationFieldSet.ID + "_terms", StringUtils.join(terms, ","));
        GWTTestUtil.clickButtonWithID(VocabularyRegistrationForm.ID
                + AbstractRegistrationForm.SAVE_BUTTON);
    }
}
