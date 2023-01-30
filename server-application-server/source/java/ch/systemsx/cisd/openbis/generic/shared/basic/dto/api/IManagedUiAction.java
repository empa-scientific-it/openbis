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
import java.util.List;

/**
 * Description of action that a user can perform on the client side.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedUiAction extends Serializable
{
    /** Returns string identifier of the action. */
    public String getName();

    /** Returns string describing the action behaviour. */
    public String getDescription();

    /**
     * Sets a string describing the action behaviour.
     * 
     * @return this (for method chaining)
     */
    public IManagedUiAction setDescription(String description);

    /**
     * Adds specified input widget descriptions that will be used in user interface for modifcation of a managed property.
     */
    public void addInputWidgets(IManagedInputWidgetDescription... widgets);

    /**
     * Returns list of objects describing input widgets that will be used in user interface for modification of the managed property.
     */
    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions();

    /**
     * Convenience method returning value of input widget with given label or null if such widget doesn't exist.
     */
    public String getInputValue(String inputLabel);

    /** Returns invoker of the action or <code>null</code> if action wasn't invoked yet. */
    public IPerson getPerson();
}
