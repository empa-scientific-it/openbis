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

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Dynamic;
import ch.systemsx.cisd.openbis.uitest.widget.Fillable;
import ch.systemsx.cisd.openbis.uitest.widget.RadioButton;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

public class AssignDataSetPropertyType
{

    @Locate("openbis_select_property-typeopenbis_property-type-assignment_DATA_SETproperty_type")
    private DropDown propertyType;

    @Locate("openbis_select_data-set-typeopenbis_property-type-assignment_data_set_type")
    private DropDown dataSetType;

    @Locate("openbis_property-type-assignment_DATA_SETmandatory_checkbox")
    private Checkbox mandatory;

    @Lazy
    @Locate("openbis_property-type-assignment_DATA_SETdefault_value")
    private Dynamic initialValue;

    @Locate("openbis_property-type-assignment_scriptable_checkbox")
    private Checkbox handledByScript;

    @Lazy
    @Locate("openbis_property-type-assignment_managed_radio")
    private RadioButton managedProperty;

    @Lazy
    @Locate("openbis_property-type-assignment_dynamic_radio")
    private RadioButton dynamicProperty;

    @Lazy
    @Locate("openbis_property-type-assignment_script_chooser-input")
    private Text scriptName;

    @Locate("openbis_property-type-assignment_DATA_SETsave-button")
    private Button save;

    @Lazy
    @Locate("openbis_property-type-assignment_DATA_SET_tab")
    private WebElement infoBox;

    public void fillWith(PropertyTypeAssignment assignment)
    {
        mandatory.set(assignment.isMandatory());
        propertyType.select(assignment.getPropertyType().getLabel());
        dataSetType.select(assignment.getEntityType().getCode());

        if (assignment.getInitialValue() != null && assignment.getInitialValue().length() > 0)
        {
            Widget c =
                    initialValue.define(assignment.getPropertyType().getDataType().representedAs());
            ((Fillable) c).fillWith(assignment.getInitialValue());
        }

        if (assignment.getScript() != null)
        {
            handledByScript.set(true);
            switch (assignment.getScript().getType())
            {
                case DYNAMIC_PROPERTY_EVALUATOR:
                    dynamicProperty.select();
                    break;
                case MANAGED_PROPERTY_HANDLER:
                    managedProperty.select();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid script type: "
                            + assignment.getScript().getType());
            }
            scriptName.fillWith(assignment.getScript().getName());
        }
    }

    public void save()
    {
        String dataSetTypeCode = dataSetType.getValue();
        this.save.click();
        SeleniumTest.setImplicitWait(30, TimeUnit.MINUTES);
        infoBox.findElement(By.xpath(".//div[contains(text(), '" + dataSetTypeCode + "')]"));
        SeleniumTest.setImplicitWaitToDefault();
    }
}
