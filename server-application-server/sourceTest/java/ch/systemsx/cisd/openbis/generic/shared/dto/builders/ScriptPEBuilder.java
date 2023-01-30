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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
public class ScriptPEBuilder
{
    private ScriptPE script = new ScriptPE();

    public ScriptPE getScript()
    {
        return script;
    }

    public ScriptPEBuilder name(String name)
    {
        script.setName(name);
        return this;
    }

    public ScriptPEBuilder description(String description)
    {
        script.setDescription(description);
        return this;
    }

    public ScriptPEBuilder script(String scriptCode)
    {
        script.setScript(scriptCode);
        return this;
    }

    public ScriptPEBuilder pluginType(PluginType type)
    {
        script.setPluginType(type);
        return this;
    }

    public ScriptPEBuilder scriptType(ScriptType type)
    {
        script.setScriptType(type);
        return this;
    }

    public ScriptPEBuilder entityKind(EntityKind entityKind)
    {
        script.setEntityKind(entityKind);
        return this;
    }

    public ScriptPEBuilder available()
    {
        script.setAvailable(true);
        return this;
    }

}
