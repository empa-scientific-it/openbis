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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Date;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link ScriptBO}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses =
{ ScriptBO.class, ScriptBO.IScriptFactory.class, ScriptPE.class })
public final class ScriptBOTest extends AbstractBOTest
{

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] scriptTypes()
    {
        return new Object[][]
        {
                {
                        ScriptType.DYNAMIC_PROPERTY,
                        "Error evaluating '1+': SyntaxError: "
                                + "(\"no viable alternative at input ')'\", "
                                + "('expression: 1+', 1, 14, '__result__=(1+)\\n'))" },
                {
                        ScriptType.MANAGED_PROPERTY,
                        "SyntaxError: (\"no viable alternative at input '\\\\n\\\\n'\", "
                                + "('<string>', 1, 2, '1+\\n'))" }

        };
    }

    private static final String SCRIPT = "1+1";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "desc";

    private final ScriptBO createScriptBO()
    {
        return new ScriptBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, scriptFactory,
                managedPropertyEvaluatorFactory, null, null, new TestJythonEvaluatorPool());
    }

    @Test
    public final void testSaveWithNullScript()
    {
        boolean fail = true;
        try
        {
            createScriptBO().save();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "scriptTypes")
    public final void testDefineAndSave(ScriptType scriptType, String expectedErrorMessage)
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script newScript = new Script();
        newScript.setDescription(DESCRIPTION);
        newScript.setName(NAME);
        newScript.setScript(SCRIPT);
        newScript.setScriptType(scriptType);

        final ScriptPE scriptPE = new ScriptPE();

        context.checking(new Expectations()
            {
                {
                    one(scriptFactory).create();
                    will(returnValue(scriptPE));

                    one(scriptDAO).createOrUpdate(scriptPE);
                }
            });
        scriptBO.define(newScript);
        scriptBO.save();

        assertEquals(scriptType, scriptPE.getScriptType());
        assertEquals(newScript.getDescription(), scriptPE.getDescription());
        assertEquals(newScript.getName(), scriptPE.getName());
        assertEquals(ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), scriptPE.getRegistrator());
        assertEquals(newScript.getScript(), scriptPE.getScript());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "scriptTypes")
    public final void testDefineAndSaveScriptCompilationFail(ScriptType scriptType,
            String expectedErrorMessage)
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script newScript = new Script();
        newScript.setDescription(DESCRIPTION);
        newScript.setName(NAME);
        newScript.setScript("1+");
        newScript.setScriptType(scriptType);

        final ScriptPE scriptPE = new ScriptPE();

        context.checking(new Expectations()
            {
                {
                    one(scriptFactory).create();
                    will(returnValue(scriptPE));
                }
            });
        scriptBO.define(newScript);
        try
        {
            scriptBO.save();
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals(expectedErrorMessage, ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteByTechId() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(returnValue(scriptPE));

                    one(scriptDAO).delete(scriptPE);
                }
            });
        scriptBO.deleteByTechId(techId);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteUnexistentScript() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(throwException(new DataRetrievalFailureException("Not found")));

                }
            });
        boolean excetionThrown = false;
        try
        {
            scriptBO.deleteByTechId(techId);
        } catch (UserFailureException ex)
        {
            excetionThrown = true;
        }
        assertTrue(excetionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteConnectedScript() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(returnValue(scriptPE));

                    one(scriptDAO).delete(scriptPE);
                    will(throwException(new DataIntegrityViolationException("")));

                }
            });
        boolean excetionThrown = false;
        try
        {
            scriptBO.deleteByTechId(techId);
        } catch (UserFailureException ex)
        {
            excetionThrown = true;
        }
        assertTrue(excetionThrown);
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "scriptTypes")
    public void testUpdateScriptNotChanged(ScriptType scriptType, String expectedErrorMessage)
            throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script updates = new Script();
        String description = DESCRIPTION;
        updates.setDescription(description);
        String name = NAME;
        updates.setName(name);
        String script = SCRIPT;
        updates.setScript(script);
        updates.setId(1L);
        updates.setModificationDate(new Date(42));

        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setName(name + 1);
        scriptPE.setScript(script);
        scriptPE.setDescription(description + 1);
        scriptPE.setScriptType(scriptType);
        scriptPE.setModificationDate(updates.getModificationDate());

        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(TechId.create(updates));
                    will(returnValue(scriptPE));

                    one(scriptDAO).createOrUpdate(scriptPE);
                }
            });

        assertFalse(updates.getDescription().equals(scriptPE.getDescription()));
        assertFalse(updates.getName().equals(scriptPE.getName()));
        assertEquals(updates.getScript(), scriptPE.getScript());

        scriptBO.update(updates);

        assertEquals(updates.getDescription(), scriptPE.getDescription());
        assertEquals(updates.getName(), scriptPE.getName());
        assertEquals(updates.getScript(), scriptPE.getScript());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test(dataProvider = "scriptTypes")
    public void testUpdateScriptChanged(final ScriptType scriptType, String expectedErrorMessage)
            throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script updates = new Script();
        String description = DESCRIPTION;
        updates.setDescription(description);
        String name = NAME;
        updates.setName(name);
        String script = SCRIPT;
        updates.setScript(script);
        updates.setId(1L);
        updates.setModificationDate(new Date(42));

        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setName(name + 1);
        scriptPE.setScript(script + 1);
        scriptPE.setDescription(description + 1);
        scriptPE.setScriptType(scriptType);
        scriptPE.setModificationDate(updates.getModificationDate());
        final SampleTypePropertyTypePE etpt = new SampleTypePropertyTypePE();
        SampleTypePE sampleType = new SampleTypePE();
        etpt.setEntityType(sampleType);
        etpt.setScript(scriptPE);
        scriptPE.getSampleAssignments().add(etpt);

        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(TechId.create(updates));
                    will(returnValue(scriptPE));

                    one(scriptDAO).createOrUpdate(scriptPE);

                    if (scriptType == ScriptType.DYNAMIC_PROPERTY)
                    {
                        one(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                        will(returnValue(entityPropertyTypeDAO));

                        one(entityPropertyTypeDAO).scheduleDynamicPropertiesEvaluation(etpt);
                    }
                }
            });

        assertFalse(updates.getDescription().equals(scriptPE.getDescription()));
        assertFalse(updates.getName().equals(scriptPE.getName()));
        assertFalse(updates.getScript().equals(scriptPE.getScript()));

        scriptBO.update(updates);

        assertEquals(updates.getDescription(), scriptPE.getDescription());
        assertEquals(updates.getName(), scriptPE.getName());
        assertEquals(updates.getScript(), scriptPE.getScript());
        context.assertIsSatisfied();
    }

}