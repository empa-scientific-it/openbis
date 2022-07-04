package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class DatasetTypeImportHelper extends BasicImportHelper
{
    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    public DatasetTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected String getTypeName()
    {
        return "dataset";
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, ImportTypes.DATASET_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        VersionUtils.updateVersion(version, versions, ImportTypes.DATASET_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "Code");
        EntityTypePermId id = new EntityTypePermId(code);

        return delayedExecutor.getDataSetType(id, new DataSetTypeFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "Code");
        String description = getValueByColumnName(header, values, "Description");
        String script = getValueByColumnName(header, values, "Validation script");

        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode(code);
        creation.setDescription(description);
        if (script != null && !script.isEmpty())
        {
            creation.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(creation.getCode(), script)));
        }

        delayedExecutor.createDataSetType(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "Code");
        String description = getValueByColumnName(header, values, "Description");
        String script = getValueByColumnName(header, values, "Validation script");

        DataSetTypeUpdate update = new DataSetTypeUpdate();
        EntityTypePermId permId = new EntityTypePermId(code);
        update.setTypeId(permId);
        update.setDescription(description);

        if (script != null && !script.isEmpty())
        {
            update.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(code, script)));
        }

        delayedExecutor.updateDataSetType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Version");
        checkKeyExistence(header, "Code");
    }
}
