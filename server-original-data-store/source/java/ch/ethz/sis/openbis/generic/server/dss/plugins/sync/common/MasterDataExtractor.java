/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author Ganime Betul Akin
 */
public class MasterDataExtractor
{
    private static final VocabularySearchCriteria VOCABULARY_SEARCH_CRITERIA = new VocabularySearchCriteria();

    private static final PropertyTypeSearchCriteria PROPERTY_TYPE_SEARCH_CRITERIA = new PropertyTypeSearchCriteria();

    private static final ExperimentTypeSearchCriteria EXPERIMENT_TYPE_SEARCH_CRITERIA = new ExperimentTypeSearchCriteria();

    private static final MaterialTypeSearchCriteria MATERIAL_TYPE_SEARCH_CRITERIA = new MaterialTypeSearchCriteria();

    private static final SampleTypeSearchCriteria SAMPLE_TYPE_SEARCH_CRITERIA = new SampleTypeSearchCriteria();

    private static final DataSetTypeSearchCriteria DATA_SET_TYPE_SEARCH_CRITERIA = new DataSetTypeSearchCriteria();

    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final IApplicationServerApi v3Api;

    private final String sessionToken;

    /**
     * @param v3Api
     * @param sessionToken
     * @param masterDataRegistrationTransaction
     */
    public MasterDataExtractor(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }

    public String fetchAsXmlString() throws ParserConfigurationException, TransformerException
    {
        StringWriter writer = new StringWriter();
        try
        {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(writer);
            // xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeStartElement("xmd:masterData");
            xmlStreamWriter.writeAttribute("xmlns:xmd", "https://sis.id.ethz.ch/software/#openbis/xmdterms/");
            appendFileFormatTypes(xmlStreamWriter);
            appendValidationPlugins(xmlStreamWriter);
            appendVocabularies(xmlStreamWriter);
            appendPropertyTypes(xmlStreamWriter);
            appendSampleTypes(xmlStreamWriter);
            appendExperimentTypes(xmlStreamWriter);
            appendDataSetTypes(xmlStreamWriter);
            appendMaterialTypes(xmlStreamWriter);
            appendExternalDataManagementSystems(xmlStreamWriter);
            xmlStreamWriter.writeEndElement();
            return writer.toString();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private void appendExternalDataManagementSystems(XMLStreamWriter out) throws XMLStreamException
    {
        ExternalDmsSearchCriteria searchCriteria = new ExternalDmsSearchCriteria();
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> externalDataManagementSystems =
                v3Api.searchExternalDataManagementSystems(sessionToken, searchCriteria, fetchOptions).getObjects();
        if (externalDataManagementSystems.isEmpty() == false)
        {
            out.writeStartElement("xmd:externalDataManagementSystems");
            for (ExternalDms externalDms : externalDataManagementSystems)
            {
                out.writeStartElement("xmd:externalDataManagementSystem");
                writeAttributeIfNotNull(out, "code", externalDms.getCode());
                writeAttributeIfNotNull(out, "label", externalDms.getLabel());
                writeAttributeIfNotNull(out, "address", externalDms.getAddress());
                writeAttributeIfNotNull(out, "addressType", externalDms.getAddressType().toString());
                out.writeEndElement();
            }
            out.writeEndElement();
        }
    }

    private void writeAttributeIfNotNull(XMLStreamWriter out, String key, String value) throws XMLStreamException
    {
        if (value != null)
        {
            out.writeAttribute(key, value);
        }
    }

    private void appendValidationPlugins(XMLStreamWriter out) throws XMLStreamException
    {
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        List<Plugin> plugins = v3Api.searchPlugins(sessionToken, new PluginSearchCriteria(), fetchOptions).getObjects();
        if (plugins.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:validationPlugins");
        for (Plugin plugin : plugins)
        {
            out.writeStartElement("xmd:validationPlugin");
            writeAttributeIfNotNull(out, "name", plugin.getName());
            writeAttributeIfNotNull(out, "description", plugin.getDescription());
            writeAttributeIfNotNull(out, "type", plugin.getPluginType().toString());
            writeAttributeIfNotNull(out, "entityKind", getEntityKind(plugin));
            writeAttributeIfNotNull(out, "isAvailable", String.valueOf(plugin.isAvailable()));
            if (plugin.getScript() != null)
            {
                out.writeCData(plugin.getScript());
            }
            out.writeEndElement();
        }
        out.writeEndElement();
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

    private void appendFileFormatTypes(XMLStreamWriter out) throws XMLStreamException
    {
        List<IFileFormatTypeImmutable> fileFormatTypes = masterDataRegistrationTransaction.listFileFormatTypes();
        if (fileFormatTypes.size() > 0)
        {
            out.writeStartElement("xmd:fileFormatTypes");
            for (IFileFormatTypeImmutable fileFormatType : fileFormatTypes)
            {
                out.writeStartElement("xmd:fileFormatType");
                writeAttributeIfNotNull(out, "code", fileFormatType.getCode());
                writeAttributeIfNotNull(out, "description", fileFormatType.getDescription());
                out.writeEndElement();
            }
            out.writeEndElement();
        }
    }

    private void appendPropertyTypes(XMLStreamWriter out) throws XMLStreamException
    {
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        fetchOptions.withVocabulary();
        List<PropertyType> propertyTypes = v3Api.searchPropertyTypes(sessionToken, PROPERTY_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (propertyTypes.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:propertyTypes");

        for (PropertyType propertyType : propertyTypes)
        {
            Boolean managedInternally = propertyType.isManagedInternally();
            String code =
                    (managedInternally && propertyType.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX))
                            ? CodeConverter.tryToDatabase(propertyType.getCode())
                            : propertyType.getCode();
            out.writeStartElement("xmd:propertyType");
            writeAttributeIfNotNull(out, "code", code);
            writeAttributeIfNotNull(out, "label", propertyType.getLabel());
            writeAttributeIfNotNull(out, "dataType", propertyType.getDataType().name());
            writeAttributeIfNotNull(out, "managedInternally", String.valueOf(managedInternally));
            writeAttributeIfNotNull(out, "description", propertyType.getDescription());
            if (propertyType.getDataType().name().equals(DataType.CONTROLLEDVOCABULARY.name()))
            {
                writeAttributeIfNotNull(out, "vocabulary", propertyType.getVocabulary().getCode());
            } else if (propertyType.getDataType().name().equals(DataType.MATERIAL.name()))
            {
                if (propertyType.getMaterialType() != null)
                {
                    writeAttributeIfNotNull(out, "material", propertyType.getMaterialType().getCode());
                } else
                {
                    // for properties like "inhibitor_of" where it is of Material of Any Type
                    writeAttributeIfNotNull(out, "material", "");
                }
            }
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private void appendVocabularies(XMLStreamWriter out) throws XMLStreamException
    {
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        List<Vocabulary> vocabularies = v3Api.searchVocabularies(sessionToken, VOCABULARY_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (vocabularies.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:controlledVocabularies");
        for (Vocabulary vocabulary : vocabularies)
        {
            out.writeStartElement("xmd:controlledVocabulary");
            String code = vocabulary.isManagedInternally()
                    && vocabulary.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX) ? CodeConverter.tryToDatabase(vocabulary.getCode())
                            : vocabulary.getCode();
            writeAttributeIfNotNull(out, "code", code);
            writeAttributeIfNotNull(out, "description", vocabulary.getDescription());
            String urlTemplate = vocabulary.getUrlTemplate();
            writeAttributeIfNotNull(out, "urlTemplate", urlTemplate);
            writeAttributeIfNotNull(out, "managedInternally", String.valueOf(vocabulary.isManagedInternally()));
            writeAttributeIfNotNull(out, "chosenFromList", String.valueOf(vocabulary.isChosenFromList()));

            for (VocabularyTerm term : vocabulary.getTerms())
            {
                out.writeStartElement("xmd:term");
                writeAttributeIfNotNull(out, "code", term.getCode());
                writeAttributeIfNotNull(out, "label", term.getLabel());
                writeAttributeIfNotNull(out, "description", term.getDescription());
                writeAttributeIfNotNull(out, "ordinal", String.valueOf(term.getOrdinal()));
                writeAttributeIfNotNull(out, "url", createUrl(urlTemplate, term.getCode()));
                out.writeEndElement();
            }
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private String createUrl(String urlTemplate, String code)
    {
        if (urlTemplate == null)
        {
            return null;
        }
        String url = urlTemplate.replaceAll(BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code);
        return url.replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code);
    }

    private void appendMaterialTypes(XMLStreamWriter out) throws XMLStreamException
    {
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<MaterialType> types = v3Api.searchMaterialTypes(sessionToken, MATERIAL_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:materialTypes");
        for (MaterialType type : types)
        {
            writeTypeElement(out, "xmd:materialType", type);
            writeAttributeIfNotNull(out, "validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
            appendPropertyAssignments(out, type.getPropertyAssignments());
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private void appendExperimentTypes(XMLStreamWriter out) throws XMLStreamException
    {
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<ExperimentType> types = v3Api.searchExperimentTypes(sessionToken, EXPERIMENT_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:collectionTypes");
        for (ExperimentType type : types)
        {
            writeTypeElement(out, "xmd:collectionType", type);
            writeAttributeIfNotNull(out, "validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
            appendPropertyAssignments(out, type.getPropertyAssignments());
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private void appendSampleTypes(XMLStreamWriter out) throws XMLStreamException
    {
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<SampleType> types = v3Api.searchSampleTypes(sessionToken, SAMPLE_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:objectTypes");
        for (SampleType type : types)
        {
            writeTypeElement(out, "xmd:objectType", type);
            writeAttributeIfNotNull(out, "listable", String.valueOf(type.isListable()));
            writeAttributeIfNotNull(out, "showContainer", String.valueOf(type.isShowContainer()));
            writeAttributeIfNotNull(out, "showParents", String.valueOf(type.isShowParents()));
            writeAttributeIfNotNull(out, "showParentMetadata", String.valueOf(type.isShowParentMetadata()));
            writeAttributeIfNotNull(out, "subcodeUnique", String.valueOf(type.isSubcodeUnique()));
            writeAttributeIfNotNull(out, "autoGeneratedCode", String.valueOf(type.isAutoGeneratedCode()));
            writeAttributeIfNotNull(out, "generatedCodePrefix", type.getGeneratedCodePrefix());
            writeAttributeIfNotNull(out, "validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
            appendPropertyAssignments(out, type.getPropertyAssignments());
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private void appendDataSetTypes(XMLStreamWriter out) throws XMLStreamException
    {
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<DataSetType> types = v3Api.searchDataSetTypes(sessionToken, DATA_SET_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        out.writeStartElement("xmd:dataSetTypes");
        for (DataSetType type : types)
        {
            writeTypeElement(out, "xmd:dataSetType", type);
            writeAttributeIfNotNull(out, "mainDataSetPattern", type.getMainDataSetPattern());
            writeAttributeIfNotNull(out, "mainDataSetPath", type.getMainDataSetPath());
            writeAttributeIfNotNull(out, "deletionDisallowed", String.valueOf(type.isDisallowDeletion()));
            writeAttributeIfNotNull(out, "validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
            appendPropertyAssignments(out, type.getPropertyAssignments());
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    private <T extends ICodeHolder & IDescriptionHolder & IPropertyAssignmentsHolder> void writeTypeElement(
            XMLStreamWriter out, String elementType, T type) throws XMLStreamException
    {
        out.writeStartElement(elementType);
        writeAttributeIfNotNull(out, "code", type.getCode());
        writeAttributeIfNotNull(out, "description", type.getDescription());
    }

    private void appendPropertyAssignments(XMLStreamWriter out, List<PropertyAssignment> propertyAssignments) throws XMLStreamException
    {
        out.writeStartElement("xmd:propertyAssignments");
        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            out.writeStartElement("xmd:propertyAssignment");
            writeAttributeIfNotNull(out, "propertyTypeCode", propertyAssignment.getPropertyType().getCode());
            writeAttributeIfNotNull(out, "ordinal", String.valueOf(propertyAssignment.getOrdinal()));
            writeAttributeIfNotNull(out, "section", propertyAssignment.getSection());
            writeAttributeIfNotNull(out, "showInEdit", String.valueOf(propertyAssignment.isShowInEditView()));
            writeAttributeIfNotNull(out, "mandatory", String.valueOf(propertyAssignment.isMandatory()));
            writeAttributeIfNotNull(out, "showRawValueInForms", String.valueOf(propertyAssignment.isShowRawValueInForms()));
            Plugin plugin = propertyAssignment.getPlugin();
            if (plugin != null)
            {
                writeAttributeIfNotNull(out, "plugin", plugin.getPermId().getPermId());
                writeAttributeIfNotNull(out, "pluginType", plugin.getPluginType().toString());
            }
            out.writeEndElement();
        }
        out.writeEndElement();
    }

}
