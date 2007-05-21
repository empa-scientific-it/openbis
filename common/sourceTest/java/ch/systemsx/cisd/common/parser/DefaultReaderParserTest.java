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

package ch.systemsx.cisd.common.parser;

import java.io.StringReader;
import java.util.List;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for corresponding  {@link DefaultReaderParser} class. 
 *
 * @author Christian Ribeaud
 */
public final class DefaultReaderParserTest
{
    private final String text = "1.line:\t1\t2\t3\n2.line\t4\t5\t6\n3.line\t7\t8\t9";
    
    @BeforeSuite
    public final void init()
    {
        LogInitializer.init();
    }
    
    @Test
    public final void testParseWithoutFactory()
    {
        IReaderParser<String[]> parser = new DefaultReaderParser<String[]>();
        parser.setObjectFactory(IParserObjectFactory.DO_NOTHING_OBJECT_FACTORY);
        List<String[]> result = parser.parse(new StringReader(text));
        assertEquals(result.get(0)[0], "1.line:");
    }
}