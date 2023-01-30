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

import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.FileUpload;

/**
 * @author anttil
 */
public class SampleBatchRegistration
{

    @Locate("openbis_select_sample-typesample-batch-registration")
    public DropDown sampleTypeSelector;

    @Lazy
    @Locate("sample-batch-registration_1")
    public FileUpload upload;

    @Lazy
    @Locate("openbis_sample-batch-registrationsave-button")
    public Button save;

    public void upload(SampleType type, String uploadFile)
    {
        upload(type.getCode(), uploadFile);
    }

    public void uploadMultipleTypes(String uploadFile)
    {
        upload("(multiple)", uploadFile);
    }

    private void upload(String type, String file)
    {
        sampleTypeSelector.select(type);
        upload.setFile(file);
        save.click();
    }

}
