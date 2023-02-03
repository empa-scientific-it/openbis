/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Looks for <code>SOAPAttribute</code> elements in a XML file and tries to digest their 'name-values' children.
 * <p>
 * When its job is finished, result is returned using {@link #getSoapAttributes()}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class SOAPAttributeContentHandler extends DefaultHandler
{

    private static final String NULL = null;

    private final Map<String, String> soapAttributes;

    /**
     * A <code>SOAPAttribute</code> has two possible children: <code>name</code> and <code>values</code>.
     */
    private static enum Child
    {
        soapAttributeName, soapAttributeValues;
    }

    /** Whether we entered the <code>SOAPAttribute</code>. */
    private boolean inSoapAttribute;

    /** The current <code>SOAPAttribute</code> child. */
    private Child currentChild;

    private String soapAttributeName;

    //
    // DefaultHandler
    //

    SOAPAttributeContentHandler()
    {
        soapAttributes = new HashMap<String, String>();
    }

    /** Returns the digested 'name-values' children contained in the SOAP response. */
    final Map<String, String> getSoapAttributes()
    {
        return soapAttributes;
    }

    //
    // DefaultHandler
    //

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        if (CrowdSoapElements.SOAP_ATTRIBUTE.equals(localName))
        {
            inSoapAttribute = true;
            return;
        }
        if (inSoapAttribute)
        {
            if (CrowdSoapElements.NAME.equals(localName))
            {
                currentChild = Child.soapAttributeName;
            } else if (CrowdSoapElements.VALUES.equals(localName))
            {
                currentChild = Child.soapAttributeValues;
            }
        }
    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (CrowdSoapElements.SOAP_ATTRIBUTE.equals(localName))
        {
            inSoapAttribute = false;
        }
    }

    @Override
    public final void characters(char[] ch, int start, int length) throws SAXException
    {
        if (inSoapAttribute)
        {
            final String keyOrValue = String.valueOf(ch, start, length);
            if (currentChild == Child.soapAttributeName)
            {
                soapAttributeName = keyOrValue;
                soapAttributes.put(soapAttributeName, NULL);
            } else if (currentChild == Child.soapAttributeValues)
            {
                if (soapAttributeName != null)
                {
                    soapAttributes.put(soapAttributeName, keyOrValue);
                    soapAttributeName = null;
                }
            }
        }
    }
}
