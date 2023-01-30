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

/**
 * The type of an entity link.
 * 
 * @author Kaloyan Enimanev
 */
// NOTE: This enum is part of the Managed Properties API.
public enum EntityLinkElementKind
{

    EXPERIMENT("Experiment"), SAMPLE("Sample"), DATA_SET("Dataset", "Data Set"), MATERIAL(
            "Material");

    private final String elementName;

    private final String label;

    private EntityLinkElementKind(String elementName)
    {
        this(elementName, elementName);
    }

    private EntityLinkElementKind(String elementName, String label)
    {
        this.elementName = elementName;
        this.label = label;
    }

    /**
     * the {@link IElement} name corresponding to the link.
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * Returns the label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the {@link EntityLinkElementKind} for a given element name or <code>null</code> if no matching kind exists.
     */
    public static EntityLinkElementKind tryGetForElementName(String elementName)
    {
        for (EntityLinkElementKind kind : values())
        {
            if (kind.getElementName().equalsIgnoreCase(elementName))
            {
                return kind;
            }
        }
        return null;
    }
}
