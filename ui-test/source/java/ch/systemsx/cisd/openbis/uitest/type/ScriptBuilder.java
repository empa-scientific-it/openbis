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

package ch.systemsx.cisd.openbis.uitest.type;

import ch.systemsx.cisd.openbis.uitest.application.ApplicationRunner;

/**
 * @author anttil
 */
public class ScriptBuilder implements Builder<Script>
{

    private ApplicationRunner openbis;

    private String name;

    private ScriptType type;

    private EntityKind kind;

    private String description;

    private String content;

    public ScriptBuilder(ApplicationRunner openbis, ScriptType type)
    {
        this.openbis = openbis;
        this.name = openbis.uid();
        this.type = type;
        this.kind = EntityKind.ALL;
        this.description = "Description of script " + name;
        this.content = type.getDummyScript();
    }

    @SuppressWarnings("hiding")
    public ScriptBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public Script create()
    {
        return openbis.create(build());
    }

    @Override
    public Script build()
    {
        return new Script(name, type, kind, description, content);
    }

}
