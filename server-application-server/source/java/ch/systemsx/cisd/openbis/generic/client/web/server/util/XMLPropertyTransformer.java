/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * @author Franz-Josef Elmer
 */
public class XMLPropertyTransformer
{
    public <T> void transformXMLProperties(List<T> rows)
    {
        for (T row : rows)
        {
            if (row instanceof IEntityPropertiesHolder)
            {
                IEntityPropertiesHolder propertiesHolder = (IEntityPropertiesHolder) row;
                List<IEntityProperty> properties = propertiesHolder.getProperties();
                for (IEntityProperty property : properties)
                {
                    if (property instanceof GenericEntityProperty)
                    {
                        GenericEntityProperty entityProperty =
                                (GenericEntityProperty) property;
                        PropertyType propertyType = entityProperty.getPropertyType();
                        if (propertyType.getDataType().getCode().equals(DataTypeCode.XML))
                        {
                            String transformation = propertyType.getTransformation();
                            if (transformation != null)
                            {
                                String xslt = StringEscapeUtils.unescapeHtml4(transformation);
                                Serializable value = entityProperty.getValue();
                                String xmlString =
                                            StringEscapeUtils.unescapeHtml4(getPropertyAsString(value));
                                String renderedXMLString = XmlUtils.transform(xslt, xmlString);
                                entityProperty.setValue(renderedXMLString);
                                entityProperty.setOriginalValue(xmlString);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getPropertyAsString(Serializable value) {
        if(value != null && value.getClass().isArray()) {
            StringBuilder builder = new StringBuilder();
            for (Serializable term : (Serializable[]) value)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(term);
            }
            return builder.toString();
        } else {
            return (String) value;
        }
    }

}
