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

package ch.systemsx.cisd.openbis.uitest.page;

import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

public class AddExperimentTypeDialog
{

    @Locate("openbis_dialog-code-field")
    private Text code;

    @Locate("openbis_add-type-dialog-description-field")
    private TextArea description;

    @Locate("openbis_dialog-save-button")
    private Button save;

    @Locate("openbis_dialog-cancel-button")
    private Button cancel;

    public void save()
    {
        save.click();
    }

    public void cancel()
    {
        cancel.click();
    }

    public void fillWith(ExperimentType experimentType)
    {
        code.write(experimentType.getCode());
        description.write(experimentType.getDescription());
    }

}
