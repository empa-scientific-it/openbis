/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ScriptProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        final Script s1 = script(EntityKind.EXPERIMENT);
        final Script s2 = script(null);
        context.checking(new Expectations()
            {
                {
                    one(server).listScripts(SESSION_TOKEN, ScriptType.DYNAMIC_PROPERTY,
                            EntityKind.EXPERIMENT);
                    will(returnValue(Arrays.asList(s1, s2)));
                }
            });
        ScriptProvider scriptProvider =
                new ScriptProvider(server, SESSION_TOKEN, ScriptType.DYNAMIC_PROPERTY,
                        EntityKind.EXPERIMENT, null);

        TypedTableModel<Script> tableModel = scriptProvider.createTableModel();

        assertEquals(
                "[NAME, DESCRIPTION, SCRIPT, ENTITY_KIND, SCRIPT_TYPE, PLUGIN_TYPE, REGISTRATOR, MODIFICATION_DATE, IS_AVAILABLE]",
                getHeaderIDs(tableModel).toString());
        assertEquals(
                "[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        assertEquals("[null, null, null, null, null, null, null, null, null]",
                getHeaderEntityKinds(tableModel).toString());
        List<TableModelRowWithObject<Script>> rows = tableModel.getRows();
        assertSame(s1, rows.get(0).getObjectOrNull());
        assertEquals(
                "[my-EXPERIMENT-script, A script for EXPERIMENT, "
                        + "do something with EXPERIMENT, Experiment, Dynamic Property Evaluator, Jython Script Plugin, "
                        + "Einstein, Albert, Thu Jan 01 01:00:04 CET 1970, yes]", rows.get(0)
                        .getValues().toString());
        assertSame(s2, rows.get(1).getObjectOrNull());
        assertEquals(
                "[my-null-script, A script for null, do something with null, All, "
                        + "Dynamic Property Evaluator, Jython Script Plugin, Einstein, Albert, Thu Jan 01 01:00:04 CET 1970, yes]",
                rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
        context.assertIsSatisfied();
    }

    private Script script(EntityKind kind)
    {
        Script script = new Script();
        script.setName("my-" + kind + "-script");
        script.setDescription("A script for " + kind);
        script.setEntityKind(kind == null ? null : new EntityKind[]
        { kind });
        script.setScript("do something with " + kind);
        script.setScriptType(ScriptType.DYNAMIC_PROPERTY);
        script.setPluginType(PluginType.JYTHON);
        script.setModificationDate(new Date(4711));
        script.setRegistrator(new PersonBuilder().name("Albert", "Einstein").getPerson());
        return script;
    }
}
