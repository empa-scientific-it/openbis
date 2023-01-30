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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import junit.framework.AssertionFailedError;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;

/**
 * A {@link AbstractDefaultTestCommand} extension that expects tab to not exist.
 * 
 * @author Piotr Buczek
 */
public final class CheckTabNotExists extends ActivateTab
{
    public CheckTabNotExists(final String tabPanelId, final String tabItemId)
    {
        super(tabPanelId, tabItemId);
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        try
        {
            super.execute();
            fail("Expected AssertionFailedError because tab '" + tabItemId + "' shouldn't exist");
        } catch (AssertionFailedError e)
        {
            assertTrue(
                    "Unexpected exception message " + e.getMessage(),
                    e.getMessage().startsWith(
                            "No tab item with id '" + tabItemId
                                    + "' could be found in panel with following tabs"));
        }
    }
}
