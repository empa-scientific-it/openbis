/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;

import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author Franz-Josef Elmer
 */
public class MasterDataDeliverer extends AbstractEntityDeliverer<Object>
{
    private static final VocabularySearchCriteria VOCABULARY_SEARCH_CRITERIA = new VocabularySearchCriteria();

    private static final PropertyTypeSearchCriteria PROPERTY_TYPE_SEARCH_CRITERIA = new PropertyTypeSearchCriteria();

    private static final ExperimentTypeSearchCriteria EXPERIMENT_TYPE_SEARCH_CRITERIA = new ExperimentTypeSearchCriteria();

    private static final MaterialTypeSearchCriteria MATERIAL_TYPE_SEARCH_CRITERIA = new MaterialTypeSearchCriteria();

    private static final SampleTypeSearchCriteria SAMPLE_TYPE_SEARCH_CRITERIA = new SampleTypeSearchCriteria();

    private static final DataSetTypeSearchCriteria DATA_SET_TYPE_SEARCH_CRITERIA = new DataSetTypeSearchCriteria();

    MasterDataDeliverer(DeliveryContext context)
    {
        super(context, "master data");
    }

    @Override
    public void deliverEntities(DeliveryExecutionContext context) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        startUrlElement(writer);
        addLocation(writer, "MASTER_DATA", "MASTER_DATA");
        addLastModificationDate(writer, context.getRequestTimestamp());
        writer.writeStartElement("xmd:masterData");
        String sessionToken = context.getSessionToken();
        addFileFormatTypes(writer, context.getQueryService(), sessionToken);
        addValidationPlugins(context, writer, sessionToken);
        addVocabularies(context, writer, sessionToken);
        addPropertyTypes(context, writer, sessionToken);
        addSampleTypes(context, writer, sessionToken);
        addExperimentTypes(context, writer, sessionToken);
        addDataSetTypes(context, writer, sessionToken);
        addMaterialTypes(context, writer, sessionToken);
        addExternalDataManagementSystems(context, writer, sessionToken);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void addFileFormatTypes(XMLStreamWriter writer, IDataSourceQueryService queryService, String sessionToken) throws XMLStreamException
    {
        String openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        EncapsulatedCommonServer encapsulatedServer = ServiceFinderUtils.getEncapsulatedCommonServer(sessionToken, openBisServerUrl);
        MasterDataRegistrationService service = new MasterDataRegistrationService(encapsulatedServer);
        IMasterDataRegistrationTransaction masterDataRegistrationTransaction = service.transaction();
        List<IFileFormatTypeImmutable> fileFormatTypes = masterDataRegistrationTransaction.listFileFormatTypes();
        if (fileFormatTypes.size() > 0)
        {
            writer.writeStartElement("xmd:fileFormatTypes");
            for (IFileFormatTypeImmutable fileFormatType : fileFormatTypes)
            {
                writer.writeStartElement("xmd:fileFormatType");
                addAttribute(writer, "code", fileFormatType.getCode());
                addAttribute(writer, "description", fileFormatType.getDescription());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private void addValidationPlugins(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        List<Plugin> plugins = context.getV3api().searchPlugins(sessionToken, new PluginSearchCriteria(), fetchOptions).getObjects();
        if (plugins.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:validationPlugins");
        for (Plugin plugin : plugins)
        {
            writer.writeStartElement("xmd:validationPlugin");
            addAttributeAndExtractFilePaths(executionContext, writer, "description", plugin.getDescription());
            addAttribute(writer, "entityKind", getEntityKind(plugin));
            addAttribute(writer, "isAvailable", String.valueOf(plugin.isAvailable()));
            addAttribute(writer, "name", plugin.getName());
            addAttribute(writer, "type", plugin.getPluginType(), t -> t.toString());
            addAttribute(writer, "registration-timestamp", plugin.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addAttribute(writer, "modification-timestamp", plugin.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            if (plugin.getScript() != null)
            {
                writer.writeCData(plugin.getScript());
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private String getEntityKind(Plugin plugin)
    {
        String entityKind = "All";
        Set<EntityKind> entityKinds = plugin.getEntityKinds();
        if (entityKinds != null)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (EntityKind kind : entityKinds)
            {
                builder.append(kind.toString());
            }
            entityKind = builder.toString();
        }
        return entityKind;
    }

    private void addVocabularies(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms().withRegistrator();
        fetchOptions.withRegistrator();
        List<Vocabulary> vocabularies = context.getV3api().searchVocabularies(sessionToken, VOCABULARY_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (vocabularies.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:controlledVocabularies");
        for (Vocabulary vocabulary : vocabularies)
        {
            writer.writeStartElement("xmd:controlledVocabulary");
            String code = vocabulary.isManagedInternally()
                    && vocabulary.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX) ? CodeConverter.tryToDatabase(vocabulary.getCode())
                            : vocabulary.getCode();
            addAttribute(writer, "chosenFromList", String.valueOf(vocabulary.isChosenFromList()));
            addAttribute(writer, "code", code);
            addAttribute(writer, "description", vocabulary.getDescription());
            addAttribute(writer, "managedInternally", String.valueOf(vocabulary.isManagedInternally()));
            addAttribute(writer, "urlTemplate", vocabulary.getUrlTemplate());
            addAttribute(writer, "registration-timestamp", vocabulary.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addAttribute(writer, "registrator", vocabulary.getRegistrator().getUserId());
            addAttribute(writer, "modification-timestamp", vocabulary.getModificationDate(), h -> DataSourceUtils.convertToW3CDate(h));

            for (VocabularyTerm term : vocabulary.getTerms())
            {
                writer.writeStartElement("xmd:term");
                addAttribute(writer, "code", term.getCode());
                addAttributeAndExtractFilePaths(executionContext, writer, "description", term.getDescription());
                addAttribute(writer, "label", term.getLabel());
                addAttribute(writer, "ordinal", String.valueOf(term.getOrdinal()));
                addAttribute(writer, "registration-timestamp", term.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
                addAttribute(writer, "registrator", term.getRegistrator().getUserId());
                addAttribute(writer, "url", vocabulary.getUrlTemplate(),
                        t -> t.replaceAll(BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code)
                                .replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code));
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addPropertyTypes(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        fetchOptions.withVocabulary();
        fetchOptions.withRegistrator();
        List<PropertyType> propertyTypes =
                context.getV3api().searchPropertyTypes(sessionToken, PROPERTY_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (propertyTypes.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:propertyTypes");

        for (PropertyType propertyType : propertyTypes)
        {
            Boolean managedInternally = propertyType.isManagedInternally();
            String code =
                    (managedInternally && propertyType.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX))
                            ? CodeConverter.tryToDatabase(propertyType.getCode())
                            : propertyType.getCode();
            writer.writeStartElement("xmd:propertyType");
            addAttribute(writer, "code", code);
            addAttribute(writer, "dataType", propertyType.getDataType(), t -> t.name());
            addAttributeAndExtractFilePaths(executionContext, writer, "description", propertyType.getDescription());
            addAttribute(writer, "label", propertyType.getLabel());
            addAttribute(writer, "managedInternally", managedInternally);
            addAttribute(writer, "registration-timestamp", propertyType.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addAttribute(writer, "registrator", propertyType.getRegistrator().getUserId());
            if (propertyType.getDataType().name().equals(DataType.CONTROLLEDVOCABULARY.name()))
            {
                addAttribute(writer, "vocabulary", propertyType.getVocabulary(), v -> v.getCode());
            } else if (propertyType.getDataType().name().equals(DataType.MATERIAL.name()))
            {
                if (propertyType.getMaterialType() != null)
                {
                    addAttribute(writer, "material", propertyType.getMaterialType(), t -> t.getCode());
                } else
                {
                    // for properties like "inhibitor_of" where it is of Material of Any Type
                    addAttribute(writer, "material", "");
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addSampleTypes(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<SampleType> types = context.getV3api().searchSampleTypes(sessionToken, SAMPLE_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:objectTypes");
        for (SampleType type : types)
        {
            writeTypeElement(executionContext, writer, "xmd:objectType", type);
            addAttribute(writer, "autoGeneratedCode", type.isAutoGeneratedCode());
            addAttribute(writer, "generatedCodePrefix", type.getGeneratedCodePrefix());
            addAttribute(writer, "listable", type.isListable());
            addAttribute(writer, "showContainer", type.isShowContainer());
            addAttribute(writer, "showParentMetadata", type.isShowParentMetadata());
            addAttribute(writer, "showParents", type.isShowParents());
            addAttribute(writer, "subcodeUnique", type.isSubcodeUnique());
            addAttribute(writer, "validationPlugin", type.getValidationPlugin(), p -> p.getName());
            addAttribute(writer, "modification-timestamp", type.getModificationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addPropertyAssignments(writer, type.getPropertyAssignments());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addExperimentTypes(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<ExperimentType> types =
                context.getV3api().searchExperimentTypes(sessionToken, EXPERIMENT_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:collectionTypes");
        for (ExperimentType type : types)
        {
            writeTypeElement(executionContext, writer, "xmd:collectionType", type);
            addAttribute(writer, "validationPlugin", type.getValidationPlugin(), p -> p.getName());
            addAttribute(writer, "modification-timestamp", type.getModificationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addPropertyAssignments(writer, type.getPropertyAssignments());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addDataSetTypes(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<DataSetType> types = context.getV3api().searchDataSetTypes(sessionToken, DATA_SET_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:dataSetTypes");
        for (DataSetType type : types)
        {
            writeTypeElement(executionContext, writer, "xmd:dataSetType", type);
            addAttribute(writer, "deletionDisallowed", type.isDisallowDeletion());
            addAttribute(writer, "mainDataSetPath", type.getMainDataSetPath());
            addAttribute(writer, "mainDataSetPattern", type.getMainDataSetPattern());
            addAttribute(writer, "validationPlugin", type.getValidationPlugin(), p -> p.getName());
            addAttribute(writer, "modification-timestamp", type.getModificationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addPropertyAssignments(writer, type.getPropertyAssignments());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addMaterialTypes(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<MaterialType> types = context.getV3api().searchMaterialTypes(sessionToken, MATERIAL_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        writer.writeStartElement("xmd:materialTypes");
        for (MaterialType type : types)
        {
            writeTypeElement(executionContext, writer, "xmd:materialType", type);
            addAttribute(writer, "validationPlugin", type.getValidationPlugin(), p -> p.getName());
            addAttribute(writer, "modification-timestamp", type.getModificationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            addPropertyAssignments(writer, type.getPropertyAssignments());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void addExternalDataManagementSystems(DeliveryExecutionContext executionContext, XMLStreamWriter writer, 
            String sessionToken) throws XMLStreamException
    {
        ExternalDmsSearchCriteria searchCriteria = new ExternalDmsSearchCriteria();
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> externalDataManagementSystems =
                context.getV3api().searchExternalDataManagementSystems(sessionToken, searchCriteria, fetchOptions).getObjects();
        if (externalDataManagementSystems.isEmpty() == false)
        {
            writer.writeStartElement("xmd:externalDataManagementSystems");
            for (ExternalDms externalDms : externalDataManagementSystems)
            {
                writer.writeStartElement("xmd:externalDataManagementSystem");
                addAttribute(writer, "address", externalDms.getAddress());
                addAttribute(writer, "addressType", externalDms.getAddressType(), t -> t.toString());
                addAttribute(writer, "code", externalDms.getCode());
                addAttribute(writer, "label", externalDms.getLabel());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private <T extends ICodeHolder & IDescriptionHolder & IPropertyAssignmentsHolder> void writeTypeElement(
            DeliveryExecutionContext executionContext, XMLStreamWriter writer, String elementType, T type) throws XMLStreamException
    {
        writer.writeStartElement(elementType);
        addAttribute(writer, "code", type.getCode());
        addAttributeAndExtractFilePaths(executionContext, writer, "description", type.getDescription());
    }

    private void addPropertyAssignments(XMLStreamWriter writer, List<PropertyAssignment> propertyAssignments) throws XMLStreamException
    {
        writer.writeStartElement("xmd:propertyAssignments");
        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            writer.writeStartElement("xmd:propertyAssignment");
            addAttribute(writer, "mandatory", propertyAssignment.isMandatory());
            addAttribute(writer, "ordinal", propertyAssignment.getOrdinal(), i -> String.valueOf(i));
            addAttribute(writer, "plugin", propertyAssignment.getPlugin(), p -> p.getPermId().getPermId());
            addAttribute(writer, "pluginType", propertyAssignment.getPlugin(), p -> p.getPluginType().toString());
            addAttribute(writer, "propertyTypeCode", propertyAssignment.getPropertyType(), t -> t.getCode());
            addAttribute(writer, "section", propertyAssignment.getSection());
            addAttribute(writer, "showInEdit", propertyAssignment.isShowInEditView());
            addAttribute(writer, "showRawValueInForms", propertyAssignment.isShowRawValueInForms());
            addAttribute(writer, "registration-timestamp", propertyAssignment.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

}
