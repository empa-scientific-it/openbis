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
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

public class RegisterExperiment
{

    @Locate("openbis_select_experiment-typeopenbis_experiment-registration")
    private DropDown experimentTypeList;

    @Lazy
    @Locate("openbis_generic-experiment-register_formcode")
    private Text code;

    @Lazy
    @Locate("openbis_select_projectgeneric-experiment-register_form")
    private DropDown projectList;

    @Lazy
    @Locate("generic-experiment-register_form_samples")
    private TextArea samples;

    @Lazy
    @Locate("openbis_generic-experiment-register_formsave-button")
    private Button saveButton;

    @Lazy
    @Locate("confirmation_dialog")
    private DeletionConfirmationBox dialog;

    @Lazy
    @Locate("openbis_experiment-registration_tab")
    private WebElement infoBox;

    public void fillWith(Experiment experiment)
    {
        code.write(experiment.getCode());
        projectList.select(experiment.getProject().getCode() + " ("
                + experiment.getProject().getSpace().getCode() + ")");

        samples.clear();
        for (Sample sample : experiment.getSamples())
        {
            samples.append(sample.getCode() + ", ");
        }
    }

    public void selectExperimentType(ExperimentType experimentType)
    {

        experimentTypeList.select(experimentType.getCode());

        // BIS-208
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
        String experimentCode = code.getValue().toUpperCase();
        this.saveButton.click();
        infoBox.findElement(By.xpath(".//div/a[contains(text(), '" + experimentCode + "')]"));
    }

    @Override
    public String toString()
    {
        return "Register Experiment tab";
    }
}
