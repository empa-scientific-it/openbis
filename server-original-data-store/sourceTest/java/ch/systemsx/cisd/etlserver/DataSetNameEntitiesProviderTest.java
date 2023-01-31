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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for corresponding {@link DataSetNameEntitiesProvider} class.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetNameEntitiesProviderTest
{
    private static final String ALPHA = "alpha";

    private static final String BETA = "beta";

    private static final String GAMMA = "gamma";

    private DataSetNameEntitiesProvider provider;

    @BeforeMethod
    public void setup()
    {
        char separator = '.';
        provider =
                new DataSetNameEntitiesProvider(ALPHA + separator + BETA + separator + GAMMA,
                        separator, false);
    }

    @Test
    public void testGetEntityWithValidIndex()
    {
        assertEquals(ALPHA, provider.getEntity(0));
        assertEquals(BETA, provider.getEntity(1));
        assertEquals(GAMMA, provider.getEntity(2));
    }

    @Test
    public void testGetEntityWithValidNegativeIndex()
    {
        assertEquals(ALPHA, provider.getEntity(-3));
        assertEquals(BETA, provider.getEntity(-2));
        assertEquals(GAMMA, provider.getEntity(-1));
    }

    @Test
    public void testGetEntityWithInvalidPositiveIndex()
    {
        try
        {
            provider.getEntity(3);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Invalid data set name 'alpha.beta.gamma'. "
                    + "We need 4 entities, separated by '.', but got only 3.", e.getMessage());
        }
    }

    @Test
    public void testGetEntityWithInvalidNegativeIndex()
    {
        try
        {
            provider.getEntity(-4);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Invalid data set name 'alpha.beta.gamma'. "
                    + "We need 4 entities, separated by '.', but got only 3.", e.getMessage());
        }
    }
}
