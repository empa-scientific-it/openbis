/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * {@link IEntityPropertyAdaptor} implementation for xml property with lazy evaluation of rendered value using XSLT script.
 * 
 * @author Piotr Buczek
 */
public class XmlPropertyAdaptor extends BasicPropertyAdaptor
{
    private final String xsltTransformation;

    private String renderedValue = null;

    public XmlPropertyAdaptor(String code, String value, EntityPropertyPE propertyPE,
            String xsltTransformation)
    {
        super(code, value, propertyPE);
        this.xsltTransformation = xsltTransformation;
    }

    public XmlPropertyAdaptor(String code, String value, String xsltTransformation)
    {
        this(code, value, null, xsltTransformation);
    }

    @Override
    public String renderedValue()
    {
        if (renderedValue == null)
        {
            renderedValue = doRenderValue();
        }
        return renderedValue;
    }

    private String doRenderValue()
    {
        final String xml = super.valueAsString();
        return XmlUtils.transform(xsltTransformation, xml);
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyTypeCode() + "\nvalue:\n" + valueAsString() + "\nrendered:\n"
                + renderedValue();
    }

}
