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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Fillable;
import ch.systemsx.cisd.openbis.uitest.widget.Form;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

public class RegisterSample
{

    @Locate("openbis_select_sample-typeopenbis_sample-registration")
    private DropDown sampleTypes;

    @Lazy
    @Locate("openbis_generic-sample-register_formcode")
    private Text code;

    @Lazy
    @Locate("openbis_generic-sample-register_formexperiment")
    private Text experiment;

    @Lazy
    @Locate("register-sample-space-selection")
    private DropDown spaces;

    @Lazy
    @Locate("openbis_generic-sample-register_formcontainer")
    private Text container;

    @Lazy
    @Locate("openbis_generic-sample-register_formsave-button")
    private Button save;

    @Lazy
    @Locate("registration-panel-openbis_generic-sample-register_form")
    private Form form;

    @Lazy
    @Locate("confirmation_dialog")
    private DeletionConfirmationBox dialog;

    @Lazy
    @Locate("openbis_sample-registration_tab")
    private WebElement infoBox;

    public void fillWith(Sample sample)
    {
        code.write(sample.getCode());
        spaces.select(sample.getSpace().getCode());
        if (sample.getExperiment() != null)
        {
            experiment.write("/" + sample.getSpace().getCode() + "/"
                    + sample.getExperiment().getProject().getCode()
                    + "/" + sample.getExperiment().getCode());
        }

        if (sample.getContainer() != null)
        {
            Sample containerSample = sample.getContainer();
            container.write("/" + containerSample.getSpace().getCode() + "/"
                    + containerSample.getCode());
        }

        Map<PropertyType, Object> properties = sample.getProperties();

        for (PropertyType propertyType : properties.keySet())
        {
            Fillable widget = (Fillable) form.getWidget(propertyType);
            widget.fillWith(properties.get(propertyType).toString());
        }
    }

    public void selectSampleType(SampleType sampleType)
    {
        sampleTypes.select(sampleType.getCode());

        SeleniumTest.setImplicitWait(500, TimeUnit.MILLISECONDS);
        try
        {
            dialog.confirm();
        } catch (RuntimeException e)
        {
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }
    }

    public void save()
    {
        String sampleCode = code.getValue();
        save.click();
        infoBox.findElement(By.xpath(".//div/a[contains(text(), '" + sampleCode + "')]"));
    }

    public Collection<String> getProperties()
    {
        Collection<String> properties = new HashSet<String>();
        for (String label : form.getLabels())
        {
            properties.add(label.replace(":", "").replace("*", "").trim());
        }
        return properties;
    }

    @Override
    public String toString()
    {
        return "Register Sample tab with properties " + this.getProperties();
    }
}
