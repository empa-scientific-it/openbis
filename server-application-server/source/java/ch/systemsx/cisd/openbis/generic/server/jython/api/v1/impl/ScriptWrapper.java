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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScript;

/**
 * Adapter adapting {@link ScriptImmutable} to {@link IScript}. Setter methods do nothing.
 * 
 * @author kohleman
 */
class ScriptWrapper extends ScriptImmutable implements IScript
{
    ScriptWrapper(ScriptImmutable script)
    {
        super(script.script);
    }

    @Override
    public void setDescription(String description)
    {
    }

    @Override
    public void setName(String name)
    {
    }

    @Override
    public void setScript(String script)
    {
    }

    @Override
    public void setScriptType(String scriptType)
    {
    }

    @Override
    public void setEntityForScript(String entityKind)
    {
    }
}