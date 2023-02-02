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

package ch.systemsx.cisd.common.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.systemsx.cisd.common.xml.XMLInfraStructure;

/**
 * @author Franz-Josef Elmer
 */
public class XMLInfraStructureTest extends AssertJUnit
{
    private static final class MockContentHandler extends DefaultHandler
    {
        private List<String> texts = new ArrayList<String>();

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            String text = new String(ch, start, length).trim();
            if (text.length() > 0)
            {
                texts.add(text);
            }
        }

        @Override
        public String toString()
        {
            return texts.toString();
        }
    }

    private static final String EXAMPLE_XSD =
            "<?xml version='1.0'?>\n"
                    + "<xs:schema targetNamespace='http://my.host.org' xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"
                    + "<xs:element name='note'>\n" + "  <xs:complexType>\n" + "    <xs:sequence>\n"
                    + "      <xs:element name='to' type='xs:string'/>\n"
                    + "      <xs:element name='from' type='xs:string'/>\n"
                    + "      <xs:element name='heading' type='xs:string'/>\n"
                    + "      <xs:element name='body' type='xs:string'/>\n" + "    </xs:sequence>\n"
                    + "  </xs:complexType>\n" + "</xs:element>\n" + "</xs:schema>";

    private static final String VALID_EXAMPLE_XML = "<?xml version='1.0'?>\n" + "<n:note\n"
            + "xmlns:n='http://my.host.org'\n"
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
            + "xsi:schemaLocation='http://my.host.org /note.xsd'>\n" + "  <to>Albert</to>\n"
            + "  <from>Isaac</from>\n" + "  <heading>Space and Time</heading>\n"
            + "  <body>New theory on space and time.</body>\n" + "</n:note>";

    private static final String INVALID_EXAMPLE_XML = "<?xml version='1.0'?>\n" + "<n:note\n"
            + "xmlns:n='http://my.host.org'\n"
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
            + "xsi:schemaLocation='http://my.host.org /note.xsd'>\n" + "  <to>Albert</to>\n"
            + "  <from>Isaac</from>\n" + "  <heading>Space and Time</heading>\n" + "</n:note>";

    private static final String NOT_WELLFORMED_EXAMPLE_XML = "<?xml version='1.0'?>\n"
            + "<n:note\n" + "xmlns:n='http://my.host.org'\n"
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
            + "xsi:schemaLocation='http://my.host.org /note.xsd'>\n" + "  <to>Albert</to>\n";

    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    @BeforeMethod
    public void beforeMethod()
    {
        contentHandler = new MockContentHandler();
        entityResolver = new EntityResolver()
            {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException
                {
                    assertEquals("file:///note.xsd", systemId);
                    return new InputSource(new StringReader(EXAMPLE_XSD));
                }
            };
    }

    @Test
    public void testCreateSchema()
    {
        XMLInfraStructure.createSchema(new ByteArrayInputStream(EXAMPLE_XSD.getBytes()));
    }

    @Test
    public void testCreateInvalidSchema()
    {
        try
        {
            XMLInfraStructure.createSchema(new ByteArrayInputStream(VALID_EXAMPLE_XML.getBytes()));
            fail("Expection expected");
        } catch (Exception ex)
        {
            // ignored
        }
    }

    @Test
    public void testNoValidation()
    {
        XMLInfraStructure xmlInfraStructure = new XMLInfraStructure(false);
        xmlInfraStructure.setEntityResolver(entityResolver);

        xmlInfraStructure.parse(new StringReader(INVALID_EXAMPLE_XML), contentHandler);
        assertEquals("[Albert, Isaac, Space and Time]", contentHandler.toString());
    }

    @Test
    public void testValidXML()
    {
        XMLInfraStructure xmlInfraStructure = new XMLInfraStructure(true);
        xmlInfraStructure.setEntityResolver(entityResolver);

        xmlInfraStructure.parse(new StringReader(VALID_EXAMPLE_XML), contentHandler);
        assertEquals("[Albert, Isaac, Space and Time, New theory on space and time.]",
                contentHandler.toString());
    }

    @Test
    public void testInvalidXML()
    {
        XMLInfraStructure xmlInfraStructure = new XMLInfraStructure(true);
        xmlInfraStructure.setEntityResolver(entityResolver);

        try
        {
            xmlInfraStructure.parse(new StringReader(INVALID_EXAMPLE_XML), contentHandler);
            fail("Exception expected");
        } catch (Exception ex)
        {
            assertTrue(ex.toString(), ex.toString().indexOf(
                    "org.xml.sax.SAXException: XML validation errors:\n"
                            + "Error in line 9 column 10:cvc-complex-type.2.4.b: "
                            + "The content of element 'n:note' is not complete. ") >= 0);
        }
    }

    @Test
    public void testNotWellFormedXML()
    {
        XMLInfraStructure xmlInfraStructure = new XMLInfraStructure(true);
        xmlInfraStructure.setEntityResolver(entityResolver);

        try
        {
            xmlInfraStructure.parse(new StringReader(NOT_WELLFORMED_EXAMPLE_XML), contentHandler);
            fail("Exception expected");
        } catch (Exception ex)
        {
            assertTrue(ex.toString().startsWith("org.xml.sax.SAXParseException"));
            assertTrue(ex.toString().contains(
                    "XML document structures must start and end within the same entity."));
        }
    }
}
