package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.handler.JSONHandler;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyTypeImportHelper extends BasicImportHelper
{
    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    private final Map<String, String> propertyCache;

    public PropertyTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.propertyCache = new HashMap<>();
    }

    @Override
    protected void validateLine(Map<String, Integer> header, List<String> values) {
        String code = getValueByColumnName(header, values, "Code");
        String propertyLabel = getValueByColumnName(header, values, "Property label");
        String description = getValueByColumnName(header, values, "Description");
        String dataType = getValueByColumnName(header, values, "Data type");
        String vocabularyCode = getValueByColumnName(header, values, "Vocabulary code");
        String metadata = getValueByColumnName(header, values, "Metadata");

        String propertyData = code + propertyLabel + description + dataType + vocabularyCode + metadata;
        if (this.propertyCache.get(code) == null)
        {
            this.propertyCache.put(code, propertyData);
        }
        if (!propertyData.equals(this.propertyCache.get(code)))
        {
            throw new UserFailureException("Unambiguous property " + code + " found, has been declared before with different parameters.");
        }
    }

    @Override protected String getTypeName()
    {
        return "property";
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String newVersion = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        return VersionUtils.isNewVersion(newVersion, VersionUtils.getStoredVersion(versions, ImportTypes.PROPERTY_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        VersionUtils.updateVersion(version, versions, ImportTypes.PROPERTY_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "Code");
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary().withTerms().withVocabulary();

        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);
        return delayedExecutor.getPropertyType(propertyTypePermId, fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "Code");
        String propertyLabel = getValueByColumnName(header, values, "Property label");
        String description = getValueByColumnName(header, values, "Description");
        String dataType = getValueByColumnName(header, values, "Data type");
        String vocabularyCode = getValueByColumnName(header, values, "Vocabulary Code");
        String metadata = getValueByColumnName(header, values, "Metadata");

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(code);
        creation.setLabel(propertyLabel);
        creation.setDescription(description);

        if (dataType.startsWith("SAMPLE:"))
        {
            String sampleType = dataType.split(":")[1];
            creation.setDataType(DataType.SAMPLE);
            creation.setSampleTypeId(new EntityTypePermId(sampleType, EntityKind.SAMPLE));
        } else
        {
            creation.setDataType(DataType.valueOf(dataType));
        }

        creation.setManagedInternally(ImportUtils.isInternalNamespace(creation.getCode()));
        if (vocabularyCode != null && !vocabularyCode.isEmpty())
        {
            creation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        }
        if (metadata != null && !metadata.isEmpty())
        {
            creation.setMetaData(JSONHandler.parseMetaData(metadata));
        }

        delayedExecutor.createPropertyType(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "Code");
        String propertyLabel = getValueByColumnName(header, values, "Property label");
        String description = getValueByColumnName(header, values, "Description");
        String metadata = getValueByColumnName(header, values, "Metadata");

        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(propertyTypePermId);
        update.setLabel(propertyLabel);
        update.setDescription(description);

        if (metadata != null && !metadata.isEmpty())
        {
            update.getMetaData().add(JSONHandler.parseMetaData(metadata));
        }
        delayedExecutor.updatePropertyType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Version");
        checkKeyExistence(header, "Code");
        checkKeyExistence(header, "Property label");
        checkKeyExistence(header, "Description");
        checkKeyExistence(header, "Data type");
    }
}
