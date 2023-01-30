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
 * Interface implemented by objects describing an input UI element (text field, combo box).
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedInputWidgetDescription extends Serializable
{
    /**
     * Returns the code of this input field.
     */
    String getCode();

    /**
     * @return label of this input field
     */
    String getLabel();

    /**
     * @return type of this input field
     */
    ManagedInputFieldType getManagedInputFieldType();

    /**
     * @return value set in this input field (either default value or value provided by a user).
     */
    String getValue();

    /**
     * Sets value / default value of this input field.
     * 
     * @return this (for method chaining)
     */
    IManagedInputWidgetDescription setValue(String value);

    /**
     * @returns description of this input field.
     */
    String getDescription();

    /**
     * Sets description of this input field.
     * 
     * @return this (for method chaining)
     */
    IManagedInputWidgetDescription setDescription(String description);

    /**
     * @return mandatory flag of this input field
     */
    boolean isMandatory();

    /**
     * Sets mandatory flag of this input field (default: false)
     * 
     * @return this (for method chaining)
     */
    IManagedInputWidgetDescription setMandatory(boolean mandatory);

}
