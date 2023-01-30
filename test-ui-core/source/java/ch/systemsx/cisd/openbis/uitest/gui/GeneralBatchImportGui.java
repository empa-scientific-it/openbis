/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Console;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.layout.GeneralBatchImportLocation;
import ch.systemsx.cisd.openbis.uitest.page.GeneralBatchImport;
import ch.systemsx.cisd.openbis.uitest.type.ImportFile;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class GeneralBatchImportGui implements Command<Void>
{

    @Inject
    private Pages pages;

    @Inject
    private Console console;

    private ImportFile file;

    private final String expectedErrorMsg;

    public GeneralBatchImportGui(ImportFile file, final String expectedErrorMsg)
    {
        this.file = file;
        this.expectedErrorMsg = expectedErrorMsg;
    }

    @Override
    public Void execute()
    {
        GeneralBatchImport page = pages.goTo(new GeneralBatchImportLocation());

        console.startBuffering();
        console.setError("'General Batch Import' failed.");
        page.upload(file.getPath());
        console.waitFor("ms) register_or_update_samples_and_materials ");

        if (expectedErrorMsg != null)
        {
            console.startBuffering();
            console.waitFor(expectedErrorMsg);
        }

        return null;
    }
}
