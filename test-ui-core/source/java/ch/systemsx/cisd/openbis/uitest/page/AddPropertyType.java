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

import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

public class AddPropertyType
{

    @Locate("openbis_property-type-registration_form_code")
    private Text code;

    @Locate("openbis_property-type-registration_form_label")
    private Text label;

    @Locate("openbis_property-type-registration_form_description")
    private TextArea description;

    @Locate("openbis_select_data-type")
    private DropDown dataType;

    @Lazy
    @Locate("openbis_select_vocabulary-select")
    private DropDown vocabulary;

    @Locate("openbis_property-type-registration_formsave-button")
    private Button save;

    @Lazy
    @Locate("openbis_property-type-registration_form_tab")
    private WebElement infoBox;

    public void fillWith(PropertyType propertyType)
    {
        code.write(propertyType.getCode());
        label.write(propertyType.getLabel());
        description.write(propertyType.getDescription());
        dataType.select(propertyType.getDataType().getName());

        if (propertyType.getDataType().equals(PropertyTypeDataType.CONTROLLED_VOCABULARY))
        {
            vocabulary.select(propertyType.getVocabulary().getCode());
        }
    }

    public void save()
    {
        String propertyCode = code.getValue().toUpperCase();
        save.click();
        infoBox.findElement(By.xpath(".//div/b[text()='" + propertyCode + "']"));

    }
}
