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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

/**
 * @author anttil
 */
public class RegisterScript
{
    @Locate("script-type-selection")
    private DropDown type;

    @Locate("openbis_script-register_form-script-registration-name-input")
    private Text name;

    @Locate("openbis_script-register_form-script-registration-entity-kind")
    private DropDown kind;

    @Locate("openbis_script-register_form-script-registration-description-input")
    private TextArea description;

    @Locate("openbis_script-register_form-script-registration-script-content-input")
    private TextArea scriptContent;

    @Locate("openbis_script-register_formsave-button")
    private Button save;

    @Lazy
    @Locate("openbis_script-register_form_tab")
    private WebElement infoBox;

    public void fillWith(Script script)
    {
        type.select(script.getType().getLabel());
        name.write(script.getName());
        kind.select(script.getKind().getLabel());
        description.write(script.getDescription());
        scriptContent.write(script.getContent());
    }

    public void save()
    {
        String scriptName = name.getValue().toUpperCase();
        save.click();
        infoBox.findElement(By.xpath(".//div/b[text()='" + scriptName + "']"));
    }
}
