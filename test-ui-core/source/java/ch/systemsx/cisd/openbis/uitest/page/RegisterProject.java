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

import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

public class RegisterProject
{

    @Locate("openbis_project-register_form_code")
    private Text code;

    @Lazy
    @Locate("openbis_select_group-selectopenbis_project-register_form")
    private DropDown spaces;

    @Locate("openbis_project-register_formsave-button")
    private Button save;

    @Lazy
    @Locate("openbis_project-register_form_tab")
    private WebElement infoBox;

    public void fillWith(Project project)
    {
        code.write(project.getCode());
        spaces.select(project.getSpace().getCode());
    }

    public void save()
    {
        String projectCode = code.getValue().toUpperCase();
        save.click();
        infoBox.findElement(By.xpath(".//div/b[contains(text(), '" + projectCode + "')]"));
    }

    @Override
    public String toString()
    {
        return "Register Sample tab";
    }
}
