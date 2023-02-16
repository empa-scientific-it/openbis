/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;

public class UserSettingsDialog
{

    @Locate("openbis_change-user-settings-dialog-group-field-legacyUI-field")
    private Checkbox legacyUi;

    @Locate("openbis_dialog-save-button")
    private Button save;

    public void setLegacyUi()
    {
        legacyUi.set(true);
    }

    public void save()
    {
        save.click();
    }
}
