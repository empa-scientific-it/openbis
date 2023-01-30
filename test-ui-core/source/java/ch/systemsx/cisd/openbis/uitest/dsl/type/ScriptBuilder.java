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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateScriptGui;
import ch.systemsx.cisd.openbis.uitest.type.EntityKind;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.ScriptType;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
public class ScriptBuilder implements Builder<Script>
{

    private String name;

    private ScriptType type;

    private EntityKind kind;

    private String description;

    private String content;

    public ScriptBuilder(UidGenerator uid, ScriptType type)
    {
        this.name = uid.uid();
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

    public ScriptBuilder withImplementation(String fileName)
    {
        this.content = readFile(fileName);
        return this;
    }

    @Override
    public Script build(Application openbis, Ui ui)
    {
        Script script = new ScriptDsl(name, type, kind, description, content);
        if (Ui.WEB.equals(ui))
        {
            return openbis.execute(new CreateScriptGui(script));
        } else if (Ui.DUMMY.equals(ui))
        {
            return script;
        }
        throw new UnsupportedOperationException();

    }

    private static String readFile(String path)
    {
        try
        {
            FileInputStream stream = new FileInputStream(new File("resource/scripts/" + path));
            try
            {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                return Charset.defaultCharset().decode(bb).toString();
            } finally
            {
                stream.close();
            }
        } catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read file " + path);
        }
    }
}
