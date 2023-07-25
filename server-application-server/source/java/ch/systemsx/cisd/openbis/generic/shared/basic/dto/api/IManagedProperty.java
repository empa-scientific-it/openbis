/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
 * Interface implemented by managed property.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedProperty extends Serializable
{

    /** Return the code (or name) of the managed property. */
    String getPropertyTypeCode();

    /**
     * Returns <var>true</var> if the detailed view of the entity owning the property will show the managed property in an extra tab.
     */
    boolean isOwnTab();

    /** Sets whether managed property is shown in an extra tab or not. */
    void setOwnTab(boolean ownTab);

    /**
     * Returns <code>true</code> if the value is special, that is either a place-holder value or an error message.
     */
    boolean isSpecialValue();

    /** Returns the current value of the property. */
    Serializable getValue();

    /** Set the value of the property. */
    void setValue(Serializable value);

    /**
     * Return an object which allows to manage data for the user interface (input as well as output).
     */
    IManagedUiDescription getUiDescription();
}
