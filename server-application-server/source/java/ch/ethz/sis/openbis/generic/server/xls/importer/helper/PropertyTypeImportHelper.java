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
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.SAMPLE_DATA_TYPE_MANDATORY_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.SAMPLE_DATA_TYPE_PREFIX;

public class PropertyTypeImportHelper extends BasicImportHelper
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PropertyTypeImportHelper.class);

    private enum Attribute implements IAttribute
    {
        Version("Version", false),
        Code("Code", true),
        Mandatory("Mandatory", false),
        DefaultValue("Default Value",
                false),  // Ignored, only used by PropertyAssignmentImportHelper
        ShowInEditViews("Show in edit views", false),
        Section("Section", false),
        PropertyLabel("Property label", true),
        DataType("Data type", true),
        VocabularyCode("Vocabulary code", true),
        Description("Description", true),
        Metadata("Metadata", false),
        DynamicScript("Dynamic script", false),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false),
        MultiValued("Multivalued", false);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory)
        {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName()
        {
            return headerName;
        }

        public boolean isMandatory()
        {
            return mandatory;
        }
    }

    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    private final Map<String, String> propertyCache;

    private final AttributeValidator<Attribute> attributeValidator;

    public PropertyTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode,
            ImportOptions options, Map<String, Integer> versions)
    {
        super(mode, options);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.propertyCache = new HashMap<>();
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override
    protected void validateLine(Map<String, Integer> headers, List<String> values)
    {
        // Validate Unambiguous
        String code = getValueByColumnName(headers, values, Attribute.Code);
        String propertyLabel = getValueByColumnName(headers, values, Attribute.PropertyLabel);
        String description = getValueByColumnName(headers, values, Attribute.Description);
        String dataType = getValueByColumnName(headers, values, Attribute.DataType);
        String vocabularyCode = getValueByColumnName(headers, values, Attribute.VocabularyCode);
        String metadata = getValueByColumnName(headers, values, Attribute.Metadata);

        String propertyData =
                code + propertyLabel + description + dataType + vocabularyCode + metadata;
        if (this.propertyCache.get(code) == null)
        {
            this.propertyCache.put(code, propertyData);
        }
        if (!propertyData.equals(this.propertyCache.get(code)))
        {
            throw new UserFailureException(
                    "Unambiguous property " + code + " found, has been declared before with different attributes.");
        }
    }

    @Override
    protected ImportTypes getTypeName()
    {
        return ImportTypes.PROPERTY_TYPE;
    }

    @Override
    protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        if (version == null) {
            return true;
        } else {
            return VersionUtils.isNewVersion(version,
                    VersionUtils.getStoredVersion(versions, ImportTypes.PROPERTY_TYPE.getType(), code));
        }
    }

    @Override
    protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        if (version == null) {
            Integer storedVersion = VersionUtils.getStoredVersion(versions, ImportTypes.PROPERTY_TYPE.getType(), code);
            storedVersion++;
            version = storedVersion.toString();
        }

        VersionUtils.updateVersion(version, versions, ImportTypes.PROPERTY_TYPE.getType(), code);
    }

    @Override
    protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary().withTerms().withVocabulary();

        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);
        return delayedExecutor.getPropertyType(propertyTypePermId, fetchOptions) != null;
    }

    @Override
    protected void createObject(Map<String, Integer> header, List<String> values, int page,
            int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String propertyLabel = getValueByColumnName(header, values, Attribute.PropertyLabel);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String dataType = getValueByColumnName(header, values, Attribute.DataType);
        String vocabularyCode = getValueByColumnName(header, values, Attribute.VocabularyCode);
        String metadata = getValueByColumnName(header, values, Attribute.Metadata);
        String multiValued = getValueByColumnName(header, values, Attribute.MultiValued);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(code);
        creation.setLabel(propertyLabel);
        creation.setDescription(description);

        if (dataType.startsWith(SAMPLE_DATA_TYPE_PREFIX))
        {
            creation.setDataType(DataType.SAMPLE);
            if (dataType.contains(SAMPLE_DATA_TYPE_MANDATORY_TYPE))
            {
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

        if (multiValued != null && !multiValued.isEmpty())
        {
            creation.setMultiValue(Boolean.parseBoolean(multiValued));
        } else
        {
            creation.setMultiValue(false);
        }

        delayedExecutor.createPropertyType(creation, page, line);
    }

    @Override
    protected void updateObject(Map<String, Integer> header, List<String> values, int page,
            int line)
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
        if (propertyLabel != null)
        {
            if (propertyLabel.equals("--DELETE--") || propertyLabel.equals("__DELETE__"))
            {
                update.setLabel("");
            } else if (!propertyLabel.isEmpty())
            {
                update.setLabel(propertyLabel);
            }
        }
        if (description != null)
        {
            if (description.equals("--DELETE--") || description.equals("__DELETE__"))
            {
                update.setDescription("");
            } else if (!description.isEmpty())
            {
                update.setDescription(description);
            }
        }

        PropertyTypeFetchOptions propertyTypeFetchOptions = new PropertyTypeFetchOptions();
        propertyTypeFetchOptions.withVocabulary();
        propertyTypeFetchOptions.withSampleType();
        PropertyType propertyType =
                delayedExecutor.getPropertyType(propertyTypePermId, propertyTypeFetchOptions);
        if (vocabularyCode != null && !vocabularyCode.isEmpty())
        {
            if (vocabularyCode.equals(propertyType.getVocabulary().getCode()) == false)
            {
                operationLog.warn(
                        "PROPERTY TYPE [" + code + "] : Vocabulary types can't be updated. Ignoring the update.");
                //   throw new UserFailureException("Vocabulary types can't be updated.");
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
                operationLog.warn(
                        "PROPERTY TYPE [" + code + "] : Data Types can't be converted with Master Data XLS. Ignoring the update.");
                // update.convertToDataType(DataType.valueOf(dataType));
            }
        }
        if (metadata != null && !metadata.isEmpty())
        {
            update.getMetaData().add(JSONHandler.parseMetaData(metadata));
        }
        delayedExecutor.updatePropertyType(update, page, line);
    }

    @Override
    protected void validateHeader(Map<String, Integer> headers)
    {
        attributeValidator.validateHeaders(Attribute.values(), headers);
    }
}
