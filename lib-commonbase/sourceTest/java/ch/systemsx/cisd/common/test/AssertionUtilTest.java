/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.test;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class AssertionUtilTest
{

    @Test
    public void testAssertContainsWithExpectedAndActualNull()
    {
        AssertionUtil.assertContainsLines(null, null);
    }

    @Test
    public void testAssertContainsWithExpectedAndActualEmpty()
    {
        AssertionUtil.assertContainsLines("", "");
    }

    @Test
    public void testAssertContainsWithExpectedNull()
    {
        AssertionUtil.assertContainsLines(null, "actualLine");
    }

    @Test
    public void testAssertContainsWithExpectedEmpty()
    {
        AssertionUtil.assertContainsLines("", "actualLine");
    }

    @Test
    public void testAssertContainsWithActualNull()
    {
        try
        {
            AssertionUtil.assertContainsLines("expectedLine", null);
            Assert.fail();
        } catch (AssertionError e)
        {
            Assert.assertEquals(e.getMessage(),
                    "Expected to contain lines:\nexpectedLine\nactual lines:\nnull");
        }
    }

    @Test
    public void testAssertContainsWithActualEmpty()
    {
        try
        {
            AssertionUtil.assertContainsLines("expectedLine", "");
            Assert.fail();
        } catch (AssertionError e)
        {
            Assert.assertEquals(e.getMessage(),
                    "Expected to contain lines:\nexpectedLine\nactual lines:\n");
        }
    }

    @Test
    public void testAssertContainsInSameOrder()
    {
        AssertionUtil.assertContainsLines("expectedLine1\nexpectedLine2", "expectedLine1\nexpectedLine2");
    }

    @Test
    public void testAssertContainsInDifferentOrder()
    {
        try
        {
            AssertionUtil.assertContainsLines("expectedLine1\nexpectedLine2", "expectedLine2\nexpectedLine1");
            Assert.fail();
        } catch (AssertionError e)
        {
            Assert.assertEquals(e.getMessage(),
                    "Expected to contain lines:\nexpectedLine1\nexpectedLine2\nactual lines:\nexpectedLine2\nexpectedLine1");
        }
    }

    @Test
    public void testAssertContainsLinesInSameOrderWithOtherLines()
    {
        AssertionUtil.assertContainsLines("expectedLine1\nexpectedLine2", "actualLine1\nexpectedLine1\nactualLine2\nexpectedLine2\nactualLine3");
    }

    @Test
    public void testAssertContainsLinesInDifferentOrderWithOtherLines()
    {
        try
        {
            AssertionUtil.assertContainsLines("expectedLine1\nexpectedLine2", "actualLine1\nexpectedLine2\nactualLine2\nexpectedLine1\nactualLine3");
            Assert.fail();
        } catch (AssertionError e)
        {
            Assert.assertEquals(e.getMessage(),
                    "Expected to contain lines:\nexpectedLine1\nexpectedLine2\nactual lines:\nactualLine1\nexpectedLine2\nactualLine2\nexpectedLine1\nactualLine3");
        }
    }

}
