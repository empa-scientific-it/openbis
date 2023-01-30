/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Arrays;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescriptionFactory;

/**
 * @author Piotr Buczek
 */
public class ManagedUiActionDescriptionFactory implements IManagedInputWidgetDescriptionFactory,
        Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // for serialization
    public ManagedUiActionDescriptionFactory()
    {
    }

    @Override
    public IManagedInputWidgetDescription createTextInputField(String label)
    {
        ManagedTextInputWidgetDescription inputField = new ManagedTextInputWidgetDescription();
        inputField.setLabel(label);
        return inputField;
    }

    @Override
    public IManagedInputWidgetDescription createMultilineTextInputField(String label)
    {
        ManagedMultilineTextInputWidgetDescription inputField =
                new ManagedMultilineTextInputWidgetDescription();
        inputField.setLabel(label);
        return inputField;
    }

    @Override
    public IManagedInputWidgetDescription createComboBoxInputField(String label, String[] values)
    {
        ManagedComboBoxInputWidgetDescription inputField =
                new ManagedComboBoxInputWidgetDescription();
        inputField.setLabel(label);
        inputField.setOptions(Arrays.asList(values));
        return inputField;
    }

}
