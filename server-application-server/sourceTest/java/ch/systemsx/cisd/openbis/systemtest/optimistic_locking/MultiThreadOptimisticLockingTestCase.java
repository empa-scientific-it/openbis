/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import java.lang.reflect.Method;

import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.systemtest.PersistentSystemTestCase;

/**
 * Super class of system test doing commit after each service operation. Can be used to write tests with more than one Thread.
 * 
 * @author Franz-Josef Elmer
 */
@Transactional
public class MultiThreadOptimisticLockingTestCase extends PersistentSystemTestCase
{
    static final String CREATING_ENTITIES = "creating";

    protected ToolBox toolBox;

    @BeforeMethod
    public void createSpacesAndProjects()
    {
        toolBox = new ToolBox(commonServer, genericServer, etlService, systemSessionToken, applicationContext);
        toolBox.createSpacesAndProjects();
    }

    @AfterMethod
    public void deleteSpaces(Method m)
    {
        try
        {
            toolBox.deleteSpaces();
        } catch (Throwable t)
        {
            throw new Error(m.getName() + "() : ", t);
        }
    }

}
