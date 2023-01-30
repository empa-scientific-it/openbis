/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class PropertyTypeRenderer
{
    /**
     * @return text which can be used as the specified property description. If the property label is unique in the specified set of properties, it
     *         will be used. Otherwise the code will be attached.
     */
    public static String getDisplayName(final PropertyType propertyType, List<PropertyType> types)
    {
        boolean useCode = isLabelDuplicated(propertyType, types);
        String property = useCode ? getUniqueName(propertyType) : propertyType.getLabel();
        return property;
    }

    private static String getUniqueName(final PropertyType propertyType)
    {
        return propertyType.getLabel() + " (" + propertyType.getCode() + ")";
    }

    private static boolean isLabelDuplicated(PropertyType propertyType,
            List<PropertyType> propertyTypes)
    {
        for (PropertyType prop : propertyTypes)
        {
            // NOTE: equality by reference
            if (prop != propertyType && prop.getLabel().equals(propertyType.getLabel()))
            {
                return true;
            }
        }
        return false;
    }

    public static final String renderAsTooltip(final PropertyType propertyType)
    {
        return renderAsTooltip(propertyType, null);
    }

    public static final String renderAsTooltip(final PropertyType propertyType,
            final String sectionOrNull)
    {
        final String code = propertyType.getCode();
        final String label = propertyType.getLabel();
        final String description = propertyType.getDescription();
        String result = "";
        if (label == null)
        {
            result += "<b>" + code + "</b>";
        } else
        {
            result += "<b>" + label + "</b>";
            result += "<br>code: " + code;
        }
        if (sectionOrNull != null)
        {
            result += "<br>section: <b>" + sectionOrNull + "</b>";
        }
        if (description != null)
        {
            result += "<br><hr>description: <i>" + description + "</i>";
        }
        return result.replace(".", ". ");
    }
}
