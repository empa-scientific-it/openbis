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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.FillVocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Vocabulary Registration</i>.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
public class VocabularyRegistrationTest extends AbstractGWTTestCase
{

    private static final String VOCABULARY_CODE = "USER.COLOR";

    private static final String DESCRIPTION = "Color";

    private static final String[] TERMS =
    { "RED:01", "BLACK", "Y.E.L.L.O.W" };

    public final void testRegisterVocabulary()
    {
        loginAndInvokeAction(ActionMenuKind.VOCABULARY_MENU_NEW);
        remoteConsole.prepare(new FillVocabularyRegistrationForm(VOCABULARY_CODE, DESCRIPTION,
                TERMS));
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.VOCABULARY_MENU_BROWSE));
        VocabularyBrowserTest.showControlledVocabularyTerms(remoteConsole, VOCABULARY_CODE, 3,
                TERMS);

        launchTest();
    }
}
