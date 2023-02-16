/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ScriptTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;

import java.util.List;
import java.util.Map;

public class ScriptImportHelper extends BasicImportHelper
{
    private static final String OWNER_CODE = "Code";

    private ScriptTypes scriptType = null;

    private final Map<String, String> scripts;

    private final DelayedExecutionDecorator delayedExecutor;

    public ScriptImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options, Map<String, String> scripts)
    {
        super(mode, options);
        this.scripts = scripts;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.SCRIPT;
    }

    private String getScriptName(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, OWNER_CODE);
        String script = getValueByColumnName(header, values, scriptType.getColumnName());
        if (script == null || script.isEmpty())
        {
            return null;
        }
        return ImportUtils.getScriptName(code, script);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String scriptName = getScriptName(header, values);

        if (scriptName != null && !scriptName.isEmpty())
        {
            PluginPermId permId = new PluginPermId(scriptName);
            return delayedExecutor.getPlugin(permId, new PluginFetchOptions()) != null;
        }

        return false;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String scriptPath = getValueByColumnName(header, values, scriptType.getColumnName());

        if (scriptPath == null || scriptPath.isEmpty())
        {
            return;
        }

        PluginCreation creation = new PluginCreation();
        creation.setName(getScriptName(header, values));
        creation.setScript(this.scripts.get(scriptPath));
        switch (scriptType) {
            case VALIDATION_SCRIPT:
                creation.setPluginType(PluginType.ENTITY_VALIDATION);
                break;
            case DYNAMIC_SCRIPT:
                creation.setPluginType(PluginType.DYNAMIC_PROPERTY);
                break;
        }
        delayedExecutor.createPlugin(creation);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String scriptPath = getValueByColumnName(header, values, scriptType.getColumnName());

        PluginUpdate update = new PluginUpdate();
        PluginPermId permId = new PluginPermId(getScriptName(header, values));
        update.setPluginId(permId);
        update.setScript(this.scripts.get(scriptPath));
        delayedExecutor.updatePlugin(update);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, OWNER_CODE);
        checkKeyExistence(header, scriptType.getColumnName());
    }

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end, ScriptTypes scriptType)
    {
        this.scriptType = scriptType;
        super.importBlock(page, pageIndex, start, end);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        throw new IllegalStateException("This method should have never been called.");
    }
}
