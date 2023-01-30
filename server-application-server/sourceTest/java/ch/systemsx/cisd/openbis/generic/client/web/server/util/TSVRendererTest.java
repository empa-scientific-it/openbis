/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;

/**
 * Tests of {@link TSVRenderer}
 * 
 * @author Tomasz Pylak
 */
public class TSVRendererTest extends AssertJUnit
{
    private Mockery context;

    private ITableDataProvider dataProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataProvider = context.mock(ITableDataProvider.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRenderer()
    {
        prepareGetAllColumnTitles("h0", "h1");
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).getRows();
                    List<List<String>> entities = new ArrayList<List<String>>();
                    entities.add(Arrays.asList("x", "y"));
                    entities.add(Arrays.asList("a", "b"));
                    will(returnValue(entities));
                }
            });

        String content = TSVRenderer.createTable(dataProvider, "#");

        assertEquals("h0\th1#x\ty#a\tb#", content);
        context.assertIsSatisfied();
    }

    @Test
    public void testRendererNoRows()
    {
        prepareGetAllColumnTitles("h0", "h1");
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).getRows();
                    List<List<String>> entities = new ArrayList<List<String>>();
                    will(returnValue(entities));
                }
            });

        String content = TSVRenderer.createTable(dataProvider, "\n");

        assertEquals("h0\th1\n", content);
        context.assertIsSatisfied();
    }

    private void prepareGetAllColumnTitles(final String... titles)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).getAllColumnTitles();
                    will(returnValue(Arrays.asList(titles)));
                }
            });

    }

}
