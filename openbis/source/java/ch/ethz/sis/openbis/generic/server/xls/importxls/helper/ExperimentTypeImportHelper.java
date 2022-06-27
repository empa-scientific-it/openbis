package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExperimentTypeImportHelper extends BasicImportHelper
{
    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    public ExperimentTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected String getTypeName()
    {
        return "experiment type";
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "version");
        String code = getValueByColumnName(header, values, "code");

        return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, ImportTypes.EXPERIMENT_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "version");
        String code = getValueByColumnName(header, values, "code");

        VersionUtils.updateVersion(version, versions, ImportTypes.EXPERIMENT_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "code");
        EntityTypePermId id = new EntityTypePermId(code);

        return delayedExecutor.getExperimentType(id, new ExperimentTypeFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "code");
        String description = getValueByColumnName(header, values, "description");
        String script = getValueByColumnName(header, values, "validation script");

        ExperimentTypeCreation creation = new ExperimentTypeCreation();

        creation.setCode(code);
        creation.setDescription(description);
        if (script != null && !script.isEmpty())
        {
            creation.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(creation.getCode(), script)));
        }

        delayedExecutor.createExperimentType(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "code");
        String description = getValueByColumnName(header, values, "description");
        String script = getValueByColumnName(header, values, "validation script");

        ExperimentTypeUpdate update = new ExperimentTypeUpdate();
        EntityTypePermId permId = new EntityTypePermId(code);
        update.setTypeId(permId);
        update.setDescription(description);

        if (script != null && !script.isEmpty())
        {
            update.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(code, script)));
        }

        delayedExecutor.updateExperimentType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "version");
        checkKeyExistence(header, "code");
    }
}
