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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

import java.io.Serializable;

/**
 * Factory for creation of instances of {@link IManagedInputWidgetDescription}.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedInputWidgetDescriptionFactory extends Serializable
{

    /**
     * @return a text input field with given <var>label</var>
     */
    IManagedInputWidgetDescription createTextInputField(String label);

    /**
     * @return a multiline input field with given <var>label</var>
     */
    IManagedInputWidgetDescription createMultilineTextInputField(String label);

    /**
     * @return a combo box input field with given <var>label</var> and specified list of selectable <var>values</var>.
     */
    IManagedInputWidgetDescription createComboBoxInputField(String labels, String[] values);

}
