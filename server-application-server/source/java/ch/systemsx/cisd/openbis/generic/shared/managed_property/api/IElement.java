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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import java.util.List;
import java.util.Map;

/**
 * An {@link IElement} is an object that can be transparently converted to {@link String} (see {@link IStructuredPropertyConverter}) e.g. when we want
 * to save the value of a structured (managed) property.
 * <p>
 * {@link IElement}-s are meant to be used as a convenient, hierarchical property-value persistence technique for managed properties.
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IElement
{

    /**
     * @return the element's name.
     */
    String getName();

    /**
     * @return the value of an attribute
     * @throws IllegalArgumentException if the attribute is not defined for this {@link IElement}
     */
    String getAttribute(String key);

    /**
     * @return the value of an attribute or <code>defaultValue</code> if the attribute is not defined for this {@link IElement}
     */
    String getAttribute(String key, String defaultValue);

    /**
     * The "Data" field is intended for a chunk raw data that can be attached to an element instance. This might be useful in scenarios where
     */
    String getData();

    /**
     * @return all children {@link IElement}-s.
     */
    List<IElement> getChildren();

    Map<String, String> getAttributes();

    IElement setAttributes(Map<String, String> attributes);

    /**
     * adds an attribute, replacing any previously existing attributes with the same key.
     */
    IElement addAttribute(String key, String value);

    IElement setData(String data);

    /**
     * sets the children of this {@link IElement}, replacing any previously existing children.
     */
    IElement setChildren(List<IElement> children);

    /**
     * appends a list of children to this children elements list.
     */
    IElement addChildren(IElement... child);

}
