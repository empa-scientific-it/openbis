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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.utilities.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Script} &lt;---&gt; {@link ScriptPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class ScriptTranslator
{
    private ScriptTranslator()
    {
    }

    public final static List<Script> translate(final List<ScriptPE> scripts)
    {
        final List<Script> result = new ArrayList<Script>();
        for (final ScriptPE script : scripts)
        {
            result.add(ScriptTranslator.translate(script));
        }
        return result;
    }

    public static Script translate(final ScriptPE script)
    {
        if (script == null)
        {
            return null;
        }
        final Script result = new Script();
        result.setId(HibernateUtils.getId(script));
        result.setEntityKind(script.getEntityKind());
        result.setName(script.getName());
        result.setDescription(script.getDescription());
        result.setScript(script.getScript());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(script
                .getDatabaseInstance()));
        result.setRegistrationDate(script.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(script.getRegistrator()));
        return ReflectingStringEscaper.escapeShallow(result);
    }

}
