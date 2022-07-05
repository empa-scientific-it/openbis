package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class SampleTypeImportHelper extends BasicImportHelper
{
    private enum Attribute implements IAttribute {
        Version("Version", true),
        Code("Code", true),
        Description("Description", true),
        AutoGenerateCodes("Auto generate codes", true),
        ValidationScript("Validation script", true),
        GeneratedCodePrefix("Generated code prefix", true);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    private final AttributeValidator<Attribute> attributeValidator;

    public SampleTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.SAMPLE_TYPE;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, ImportTypes.SAMPLE_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        VersionUtils.updateVersion(version, versions, ImportTypes.SAMPLE_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        EntityTypePermId id = new EntityTypePermId(code);

        return delayedExecutor.getSampleType(id, new SampleTypeFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String validationScript = getValueByColumnName(header, values, Attribute.ValidationScript);
        String autoGenerateCodes = getValueByColumnName(header, values, Attribute.AutoGenerateCodes);
        String generatedCodePrefix = getValueByColumnName(header, values, Attribute.GeneratedCodePrefix);

        SampleTypeCreation creation = new SampleTypeCreation();

        creation.setCode(code);
        creation.setDescription(description);
        creation.setAutoGeneratedCode(Boolean.parseBoolean(autoGenerateCodes));
        if (validationScript != null && !validationScript.isEmpty())
        {
            creation.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(creation.getCode(), validationScript)));
        }
        if (generatedCodePrefix != null && !generatedCodePrefix.isEmpty())
        {
            creation.setGeneratedCodePrefix(generatedCodePrefix);
        }

        delayedExecutor.createSampleType(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String validationScript = getValueByColumnName(header, values, Attribute.ValidationScript);
        String autoGenerateCodes = getValueByColumnName(header, values, Attribute.AutoGenerateCodes);
        String generatedCodePrefix = getValueByColumnName(header, values, Attribute.GeneratedCodePrefix);

        SampleTypeUpdate update = new SampleTypeUpdate();
        EntityTypePermId permId = new EntityTypePermId(code);
        update.setTypeId(permId);
        update.setDescription(description);
        update.setAutoGeneratedCode(Boolean.parseBoolean(autoGenerateCodes));
        if (validationScript != null && !validationScript.isEmpty())
        {
            update.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(code, validationScript)));
        }
        if (generatedCodePrefix != null && !generatedCodePrefix.isEmpty())
        {
            update.setGeneratedCodePrefix(generatedCodePrefix);
        }

        delayedExecutor.updateSampleType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        attributeValidator.validateHeaders(Attribute.values(), header);
    }
}
