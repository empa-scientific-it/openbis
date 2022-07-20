package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.handler.JSONHandler;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.SAMPLE_DATA_TYPE_MANDATORY_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.SAMPLE_DATA_TYPE_PREFIX;

public class PropertyTypeImportHelper extends BasicImportHelper
{
    private enum Attribute implements IAttribute {
        Version("Version", true),
        Code("Code", true),
        Mandatory("Mandatory", false),
        ShowInEditViews("Show in edit views", false),
        Section("Section", false),
        PropertyLabel("Property label", true),
        DataType("Data type", true),
        VocabularyCode("Vocabulary code", true),
        Description("Description", true),
        Metadata("Metadata", false),
        DynamicScript("Dynamic script", false);

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

    private final Map<String, String> propertyCache;

    private final AttributeValidator<Attribute> attributeValidator;

    public PropertyTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options, Map<String, Integer> versions)
    {
        super(mode, options);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.propertyCache = new HashMap<>();
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override
    protected void validateLine(Map<String, Integer> headers, List<String> values) {
        // Validate Unambiguous
        String code = getValueByColumnName(headers, values, Attribute.Code);
        String propertyLabel = getValueByColumnName(headers, values, Attribute.PropertyLabel);
        String description = getValueByColumnName(headers, values, Attribute.Description);
        String dataType = getValueByColumnName(headers, values, Attribute.DataType);
        String vocabularyCode = getValueByColumnName(headers, values, Attribute.VocabularyCode);
        String metadata = getValueByColumnName(headers, values, Attribute.Metadata);

        String propertyData = code + propertyLabel + description + dataType + vocabularyCode + metadata;
        if (this.propertyCache.get(code) == null)
        {
            this.propertyCache.put(code, propertyData);
        }
        if (!propertyData.equals(this.propertyCache.get(code)))
        {
            throw new UserFailureException("Unambiguous property " + code + " found, has been declared before with different attributes.");
        }
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.PROPERTY_TYPE;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String newVersion = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        return VersionUtils.isNewVersion(newVersion, VersionUtils.getStoredVersion(versions, ImportTypes.PROPERTY_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        VersionUtils.updateVersion(version, versions, ImportTypes.PROPERTY_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary().withTerms().withVocabulary();

        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);
        return delayedExecutor.getPropertyType(propertyTypePermId, fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String propertyLabel = getValueByColumnName(header, values, Attribute.PropertyLabel);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String dataType = getValueByColumnName(header, values, Attribute.DataType);
        String vocabularyCode = getValueByColumnName(header, values, Attribute.VocabularyCode);
        String metadata = getValueByColumnName(header, values, Attribute.Metadata);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(code);
        creation.setLabel(propertyLabel);
        creation.setDescription(description);

        if (dataType.startsWith(SAMPLE_DATA_TYPE_PREFIX))
        {
            creation.setDataType(DataType.SAMPLE);
            if (dataType.contains(SAMPLE_DATA_TYPE_MANDATORY_TYPE)) {
                String sampleType = dataType.split(SAMPLE_DATA_TYPE_MANDATORY_TYPE)[1];
                creation.setSampleTypeId(new EntityTypePermId(sampleType, EntityKind.SAMPLE));
            }
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
        String code = getValueByColumnName(header, values, Attribute.Code);
        String propertyLabel = getValueByColumnName(header, values, Attribute.PropertyLabel);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String dataType = getValueByColumnName(header, values, Attribute.DataType);
        String vocabularyCode = getValueByColumnName(header, values, Attribute.VocabularyCode);
        String metadata = getValueByColumnName(header, values, Attribute.Metadata);

        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(propertyTypePermId);
        update.setLabel(propertyLabel);
        update.setDescription(description);

        PropertyTypeFetchOptions propertyTypeFetchOptions = new PropertyTypeFetchOptions();
        propertyTypeFetchOptions.withVocabulary();
        propertyTypeFetchOptions.withSampleType();
        PropertyType propertyType = delayedExecutor.getPropertyType(propertyTypePermId, propertyTypeFetchOptions);
        if (vocabularyCode != null && !vocabularyCode.isEmpty())
        {
            if (vocabularyCode.equals(propertyType.getVocabulary().getCode()) == false)
            {
                throw new UserFailureException("Vocabulary types can't be updated.");
            }
        }
        if (dataType != null && !dataType.isEmpty())
        {
            String currentDataType = propertyType.getDataType().name();
            if (propertyType.getDataType() == DataType.SAMPLE && propertyType.getSampleType() != null)
            {
                currentDataType += ":" + propertyType.getSampleType().getCode();
            }
            if (dataType.equals(currentDataType) == false)
            {
                update.convertToDataType(DataType.valueOf(dataType));
            }
        }
        if (metadata != null && !metadata.isEmpty())
        {
            update.getMetaData().add(JSONHandler.parseMetaData(metadata));
        }
        delayedExecutor.updatePropertyType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> headers)
    {
        attributeValidator.validateHeaders(Attribute.values(), headers);
    }
}
