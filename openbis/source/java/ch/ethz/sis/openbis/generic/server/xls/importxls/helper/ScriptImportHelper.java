package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ScriptTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.List;
import java.util.Map;

public class ScriptImportHelper extends BasicImportHelper
{
    private ScriptTypes scriptType = ScriptTypes.UNKNOWN;

    private final Map<String, String> scripts;

    private final DelayedExecutionDecorator delayedExecutor;

    public ScriptImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, String> scripts)
    {
        super(mode);
        this.scripts = scripts;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.SCRIPT;
    }

    private String getScriptName(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "Code");
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
        creation.setPluginType(scriptType.equals(ScriptTypes.VALIDATION_SCRIPT) ? PluginType.ENTITY_VALIDATION : PluginType.DYNAMIC_PROPERTY);
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
        checkKeyExistence(header, "Code");
        switch (scriptType)
        {
            case DYNAMIC_SCRIPT:
            case VALIDATION_SCRIPT:
                checkKeyExistence(header, scriptType.getColumnName());
                break;
            case UNKNOWN:
            default:
                throw new UserFailureException("Unknown script type");
        }
    }

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end, ScriptTypes scriptType)
    {
        this.scriptType = scriptType;
        super.importBlock(page, pageIndex, start, end);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        throw new UserFailureException("This method is not allowed to be used.");
    }
}
