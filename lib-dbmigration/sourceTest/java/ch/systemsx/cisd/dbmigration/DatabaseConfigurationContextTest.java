/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.dbmigration;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseConfigurationContextTest extends AssertJUnit
{
    @Test
    public void testGetSqlScriptFoldersWithNothingSet()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        assertEquals("[]", context.getSqlScriptFolders().toString());
    }

    @Test
    public void testGetSqlScriptFoldersWithScriptFolderHasUnresolvedVariable()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setScriptFolder("${folder}");
        assertEquals("[]", context.getSqlScriptFolders().toString());
    }

    @Test
    public void testGetSqlScriptFoldersWithResolvedScriptFolder()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setScriptFolder("my-scripts");
        assertEquals("[my-scripts]", context.getSqlScriptFolders().toString());
    }

    @Test
    public void testGetSqlScriptFoldersWithScriptFoldersHaveUnresolvedVariable()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setScriptFolders("${folder}");
        assertEquals("[]", context.getSqlScriptFolders().toString());
    }

    @Test
    public void testGetSqlScriptFoldersWithResolvedScriptFolders()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setScriptFolders("  folder1  ,  folder2  ");
        assertEquals("[folder1, folder2]", context.getSqlScriptFolders().toString());
    }
}
