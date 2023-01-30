/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;

/**
 * An implementation of {@link IStructuredPropertyConverter}, that translates elements either to XML or to JSON and that can translate back both
 * formats.
 * 
 * @author Bernd Rinn
 */
public class XmlOrJsonStructuredPropertyConverter implements IStructuredPropertyConverter
{

    private final XmlStructuredPropertyConverter xmlConverter;

    private final JsonStructuredPropertyConverter jsonConverter;

    private final boolean convertToXML;

    public XmlOrJsonStructuredPropertyConverter(XmlStructuredPropertyConverter xmlConverter,
            JsonStructuredPropertyConverter jsonConverter, boolean convertToXml)
    {
        this.xmlConverter = xmlConverter;
        this.jsonConverter = jsonConverter;
        this.convertToXML = convertToXml;
    }

    @Override
    public List<IElement> convertToElements(IManagedProperty property)
    {
        if (xmlConverter.canHandle(property))
        {
            return xmlConverter.convertToElements(property);
        } else if (jsonConverter.canHandle(property))
        {
            return jsonConverter.convertToElements(property);
        } else
        {
            throw new UserFailureException("Illegal managed property value '" + property.getValue()
                    + "'.");
        }
    }

    @Override
    public List<IElement> convertStringToElements(String string)
    {
        if (xmlConverter.canHandle(string))
        {
            return xmlConverter.convertStringToElements(string);
        } else if (jsonConverter.canHandle(string))
        {
            return jsonConverter.convertStringToElements(string);
        } else
        {
            throw new UserFailureException("Illegal managed property value '" + string + "'.");
        }
    }

    @Override
    public String convertToString(List<IElement> elements)
    {
        return convertToXML ? xmlConverter.convertToString(elements) : jsonConverter
                .convertToString(elements);
    }

}
