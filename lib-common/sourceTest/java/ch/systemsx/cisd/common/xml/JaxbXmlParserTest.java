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

package ch.systemsx.cisd.common.xml;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Piotr Buczek
 */
public class JaxbXmlParserTest extends AbstractFileSystemTestCase
{

    private static JaxbXmlParser<ExampleBean> PARSER_INSTANCE =
            new JaxbXmlParser<ExampleBean>(ExampleBean.class, false);

    @Test
    public void testSimpleExampleBeanParsing()
    {
        File file = new File(workingDirectory, "test.xml");
        parseExampleXmlFile(file);
    }

    @Test
    public void testFileNameWithSpecialCharacters()
    {
        File file = new File(workingDirectory, "t@e&s%t.xml");
        parseExampleXmlFile(file);
    }

    private void parseExampleXmlFile(File file)
    {
        FileUtilities.writeToFile(file, EXAMPLE_XML);
        ExampleBean bean = PARSER_INSTANCE.doParse(file);
        assertEquals("example value", bean.getExampleField());
    }

    private static final String EXAMPLE_XML =
            "<?xml version='1.0' ?>\n               "
                    + "<ExampleBean>                                                            "
                    + "  <exampleField>example value</exampleField>                             "
                    + "</ExampleBean>                                                           ";

}
