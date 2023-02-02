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
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Loads an XML file to the bean of the specified class. The bean should be annotated with JAXB annotations.
 * <p>
 * NOTE: It is not thread safe as it holds {@link Unmarshaller} instance.
 * 
 * @author Tomasz Pylak
 */
public class JaxbXmlParser<T>
{
    // Set to true to turn on debbuging help
    private static final boolean DEBUG = false;

    /**
     * NOTE: The implementation is creating an unmarshaller for every file that will be parsed. It is very inefficient especially when parsing many
     * small files with the same schema at once (creating unmarshaller can take even 10x more time than parsing a file, not to mention validation). It
     * is better to create {@link JaxbXmlParser} once and call {@link JaxbXmlParser#doParse(File)} for every file. On the other hand Unmarshaller is
     * not thread safe so pooling may be needed in some cases.
     * 
     * @param validate if true the parsed xml will be validated with the XML Schema. It is expected that the "schema.xsd" file with XML Schema can be
     *            found in the same folder as the specified bean class.
     */
    public static <T> T parse(Class<T> beanClass, File dataSet, boolean validate)
    {
        JaxbXmlParser<T> parser = new JaxbXmlParser<T>(beanClass, validate);
        return parser.doParse(dataSet);
    }

    private final Unmarshaller unmarshaller;

    private final Class<T> beanClass;

    public JaxbXmlParser(Class<T> beanClass, boolean validate)
    {
        this.beanClass = beanClass;
        this.unmarshaller = createUnmarshaller(beanClass);
        if (DEBUG)
            useDebugValidationEventHandler();

        if (validate)
        {
            unmarshaller.setSchema(XMLInfraStructure.createSchema("/"
                    + beanClass.getPackage().getName().replace('.', '/') + "/schema.xsd"));
        }
    }

    private void useDebugValidationEventHandler()
    {
        try
        {
            unmarshaller.setEventHandler(new DefaultValidationEventHandler());
        } catch (JAXBException ex)
        {
            ex.printStackTrace();
        }
    }

    private static <T> Unmarshaller createUnmarshaller(Class<T> beanClass)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            return context.createUnmarshaller();
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static <T> void writeSchema(Class<T> beanClass)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            debugGenerateXmlSchema(context);
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static void debugGenerateXmlSchema(JAXBContext context)
    {
        try
        {
            context.generateSchema(new SchemaOutputResolver()
                {
                    @Override
                    public Result createOutput(String namespaceUri, String schemaName)
                            throws IOException
                    {
                        return new StreamResult(new File(".", schemaName));
                    }
                });
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public T doParse(File file)
    {
        try
        {
            // WORKAROUND to incorrect handling of files with '%' in the name.
            // We cannot use StreamSource constructor with a file directly as an argument.
            Source source = new StreamSource(new FileInputStream(file));
            JAXBElement<T> object = unmarshaller.unmarshal(source, beanClass);
            return object.getValue();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
