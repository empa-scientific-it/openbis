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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class DatasetTypeImportHelper extends BasicImportHelper
{
    private enum Attribute implements IAttribute {
        Version("Version", false),
        Code("Code", true),
        Description("Description", true),
        ValidationScript("Validation script", true),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false);

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

    public DatasetTypeImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options, Map<String, Integer> versions)
    {
        super(mode, options);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.DATASET_TYPE;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        if (version == null) {
            return true;
        } else {
            return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, ImportTypes.DATASET_TYPE.getType(), code));
        }
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        if (version == null) {
            Integer storedVersion = VersionUtils.getStoredVersion(versions, ImportTypes.DATASET_TYPE.getType(), code);
            storedVersion++;
            version = storedVersion.toString();
        }

        VersionUtils.updateVersion(version, versions, ImportTypes.DATASET_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        EntityTypePermId id = new EntityTypePermId(code);

        return delayedExecutor.getDataSetType(id, new DataSetTypeFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String validationScript = getValueByColumnName(header, values, Attribute.ValidationScript);

        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode(code);
        creation.setDescription(description);
        if (validationScript != null && !validationScript.isEmpty())
        {
            creation.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(creation.getCode(), validationScript)));
        }

        delayedExecutor.createDataSetType(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String description = getValueByColumnName(header, values, Attribute.Description);
        String validationScript = getValueByColumnName(header, values, Attribute.ValidationScript);

        DataSetTypeUpdate update = new DataSetTypeUpdate();
        EntityTypePermId permId = new EntityTypePermId(code);
        update.setTypeId(permId);
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
        if (validationScript != null && !validationScript.isEmpty())
        {
            update.setValidationPluginId(new PluginPermId(ImportUtils.getScriptName(code, validationScript)));
        }

        delayedExecutor.updateDataSetType(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        attributeValidator.validateHeaders(Attribute.values(), header);
    }
}
