/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.layout.AddPropertyTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class CreatePropertyTypeGui implements Command<PropertyType>
{

    @Inject
    private Pages pages;

    private PropertyType type;

    public CreatePropertyTypeGui(PropertyType type)
    {
        this.type = type;
    }

    @Override
    public PropertyType execute()
    {
        AddPropertyType dialog = pages.goTo(new AddPropertyTypeLocation());
        dialog.fillWith(type);
        dialog.save();
        return type;
    }

}
