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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for {@link ScriptDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups = { "db", "script" })
@Friend(toClasses = SpacePE.class)
public final class ScriptDAOTest extends AbstractDAOTest
{

    @DataProvider
    private final static Object[][] scriptTypes()
    {
        return new Object[][] {
                { ScriptType.DYNAMIC_PROPERTY },
                { ScriptType.MANAGED_PROPERTY }

        };
    }

    private static final String DESCRIPTION = "desc";

    private static final String SCRIPT = "1+1";

    private static final String NAME = "name";

    private static String createScriptDescription(int i)
    {
        return DESCRIPTION + i;
    }

    private static String createScriptText(int i)
    {
        return SCRIPT + i;
    }

    private static String createScriptName(int i)
    {
        return NAME + i;
    }

    @Test(dataProvider = "scriptTypes")
    public void testCreateScript(ScriptType scriptType)
    {
        String name = NAME;
        AssertJUnit.assertNull(daoFactory.getScriptDAO().tryFindByName(name));
        String scriptText = SCRIPT;
        String description = DESCRIPTION;
        final ScriptPE script = createScriptInDB(scriptType, name, scriptText, description, null);
        final ScriptPE retrievedScript = daoFactory.getScriptDAO().tryFindByName(name);
        AssertJUnit.assertNotNull(retrievedScript);
        assertEquals(script.getScriptType(), retrievedScript.getScriptType());
        assertEquals(script.getRegistrator(), retrievedScript.getRegistrator());
        assertEquals(script.getScript(), retrievedScript.getScript());
        assertEquals(script.getDescription(), retrievedScript.getDescription());
        assertEquals(script.getName(), retrievedScript.getName());
    }

    @Test(dataProvider = "scriptTypes")
    public void testCreateSampleScript(ScriptType scriptType)
    {
        String name = NAME;
        AssertJUnit.assertNull(daoFactory.getScriptDAO().tryFindByName(name));
        String scriptText = SCRIPT;
        String description = DESCRIPTION;
        EntityKind entityKind = EntityKind.SAMPLE;
        final ScriptPE script =
                createScriptInDB(scriptType, name, scriptText, description, entityKind);
        final ScriptPE retrievedScript = daoFactory.getScriptDAO().tryFindByName(name);
        AssertJUnit.assertNotNull(retrievedScript);
        assertEquals(script.getScriptType(), retrievedScript.getScriptType());
        assertEquals(script.getRegistrator(), retrievedScript.getRegistrator());
        assertEquals(script.getScript(), retrievedScript.getScript());
        assertEquals(script.getDescription(), retrievedScript.getDescription());
        assertEquals(script.getName(), retrievedScript.getName());
        assertEquals(script.getEntityKind(), retrievedScript.getEntityKind());
    }

    @Test(dataProvider = "scriptTypes")
    public void testListScripts(ScriptType scriptType)
    {
        int initialNumberOfScripts = daoFactory.getScriptDAO().listAllEntities().size();
        int scriptNumber = 1;

        String scriptName = createScriptName(scriptNumber);
        String scriptText = createScriptText(scriptNumber);
        String scriptDescription = createScriptDescription(scriptNumber);
        final ScriptPE script =
                createScriptInDB(scriptType, scriptName, scriptText, scriptDescription, null);
        final List<ScriptPE> scripts = daoFactory.getScriptDAO().listAllEntities();
        assertEquals(1 + initialNumberOfScripts, scripts.size());
        ScriptPE registered = null;
        for (ScriptPE s : scripts)
        {
            if (s.getName().equals(scriptName))
            {
                registered = s;
            }
        }
        assertNotNull(registered);

        if (registered != null) // for Eclipse
        {
            assertEquals(script.getScriptType(), registered.getScriptType());
            assertEquals(script.getScript(), registered.getScript());
            assertEquals(script.getDescription(), registered.getDescription());
            assertEquals(script.getName(), registered.getName());
            assertEquals(script.getEntityKind(), registered.getEntityKind());
        }
    }

    @Test(dataProvider = "scriptTypes")
    public void testListSampleCompatibleScripts(ScriptType scriptType)
    {
        EntityKind entityKind = EntityKind.SAMPLE;
        int initialNumberOfScripts =
                daoFactory.getScriptDAO().listEntities(null, entityKind).size();
        int scriptNumber = 1;
        createScriptInDB(scriptType, createScriptName(scriptNumber),
                createScriptText(scriptNumber), createScriptDescription(scriptNumber), entityKind);

        final List<ScriptPE> allScripts = daoFactory.getScriptDAO().listAllEntities();
        final List<ScriptPE> sampleCompatibleScripts = new ArrayList<ScriptPE>();

        for (ScriptPE s : allScripts)
        {
            if (s.getEntityKind() == null || s.getEntityKind() == entityKind)
            {
                sampleCompatibleScripts.add(s);
            }
        }

        assertEquals(1 + initialNumberOfScripts, sampleCompatibleScripts.size());
    }

    @Test(dataProvider = "scriptTypes")
    public final void testDelete(ScriptType scriptType)
    {
        assertNull(daoFactory.getScriptDAO().tryFindByName(NAME));
        ScriptPE script = createScriptInDB(scriptType, NAME, SCRIPT, DESCRIPTION, null);
        assertNotNull(daoFactory.getScriptDAO().tryFindByName(NAME));
        daoFactory.getScriptDAO().delete(script);
        assertNull(daoFactory.getScriptDAO().tryFindByName(NAME));
    }

}