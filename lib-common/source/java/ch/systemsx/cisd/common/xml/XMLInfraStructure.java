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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Helper class providing convenient methods for parsing XML document with and without schema validation.
 * 
 * @author Franz-Josef Elmer
 */
public class XMLInfraStructure
{
    private static final SchemaFactory SCHEMA_FACTORY =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    /**
     * Creates a Schema from a classpath resource.
     */
    public static Schema createSchema(String schemaAsClasspathResource)
    {
        return createSchema(XMLInfraStructure.class.getResourceAsStream(schemaAsClasspathResource));
    }

    /**
     * Creates a Schema from an input stream
     */
    public static Schema createSchema(InputStream schemaAsInputStream)
    {
        try
        {
            return SCHEMA_FACTORY.newSchema(new StreamSource(schemaAsInputStream));
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Creates a Schema from a file
     */
    public static Schema createSchema(File schemaFile)
    {
        try
        {
            return SCHEMA_FACTORY.newSchema(new StreamSource(schemaFile));
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Creates a Schema by a URL
     */
    public static Schema createSchema(URL schemaURL)
    {
        try
        {
            return SCHEMA_FACTORY.newSchema(schemaURL);
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Creates a Schema from a source
     */
    public static Schema createSchema(Source schemaAsSource)
    {
        try
        {
            return SCHEMA_FACTORY.newSchema(schemaAsSource);
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final SAXParserFactory parserFactory;

    private EntityResolver entityResolver;

    /**
     * Creates a new instance.
     * 
     * @param validating If <code>true</code> Schema validation is enabled.
     */
    public XMLInfraStructure(boolean validating)
    {
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        parserFactory.setValidating(validating);
    }

    /**
     * Replaces the default entity resolver by the specified one.
     */
    public void setEntityResolver(EntityResolver entityResolver)
    {
        this.entityResolver = entityResolver;
    }

    /**
     * Parses the specified XML document and deliver all content event to the specified content handler. An exception with detailed error messages is
     * thrown in case of enabled Schema validation.
     */
    public void parse(Reader xmlDocument, ContentHandler contentHandler)
    {
        try
        {
            SAXParser saxParser = parserFactory.newSAXParser();
            if (parserFactory.isValidating())
            {
                if (parserFactory.getSchema() == null)
                {
                    saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                            "http://www.w3.org/2001/XMLSchema");
                }
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setEntityResolver(entityResolver);
                final List<SAXParseException> exceptions = new ArrayList<SAXParseException>();
                xmlReader.setErrorHandler(new ErrorHandler()
                    {
                        @Override
                        public void warning(SAXParseException exception) throws SAXException
                        {
                        }

                        @Override
                        public void fatalError(SAXParseException exception) throws SAXException
                        {
                            exceptions.add(exception);
                        }

                        @Override
                        public void error(SAXParseException exception) throws SAXException
                        {
                            exceptions.add(exception);
                        }
                    });
                xmlReader.setContentHandler(contentHandler);
                xmlReader.parse(new InputSource(xmlDocument));
                if (exceptions.isEmpty() == false)
                {
                    StringBuilder builder = new StringBuilder();
                    for (SAXParseException exception : exceptions)
                    {
                        builder.append("\n");
                        builder.append("Error in line ").append(exception.getLineNumber());
                        builder.append(" column ").append(exception.getColumnNumber());
                        builder.append(":").append(exception.getMessage());
                    }
                    throw new SAXException("XML validation errors:" + builder);
                }
            } else
            {
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setContentHandler(contentHandler);
                xmlReader.parse(new InputSource(xmlDocument));
            }
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    // for debugging

    public static String getJaxpImplementationInfo()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory
                .newInstance().getClass()));
        sb.append("\n");
        sb.append(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
        sb.append("\n");
        sb.append(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance()
                .getClass()));
        sb.append("\n");
        sb.append(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance()
                .getClass()));
        sb.append("\n");
        sb.append(getJaxpImplementationInfo("SchemaFactory", SCHEMA_FACTORY.getClass()));
        sb.append("\n");
        return sb.toString();
    }

    private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass)
    {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        Package p = componentClass.getPackage();

        return MessageFormat
                .format(
                        "{0} loaded from: {1},\n\timpl: {2}\n\tpackage: {3},\n\timplVendor: {4},\n\tspecVer: {5},\n\timplVer: {6}",
                        componentName, source == null ? "Java Runtime" : source.getLocation(),
                        componentClass.getName(), p.getName(), p.getImplementationVendor(), p
                                .getSpecificationVersion(), p.getImplementationVersion());
    }

}