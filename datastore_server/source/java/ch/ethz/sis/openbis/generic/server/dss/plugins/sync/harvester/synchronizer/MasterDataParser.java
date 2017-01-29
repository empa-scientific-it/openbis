/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.ERROR_PROPERTY_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.map.MultiKeyMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.DefaultNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class MasterDataParser
{
    private final INameTranslator nameTranslator;
    
    private Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();

    private Map<String, SampleType> sampleTypes = new HashMap<String, SampleType>();

    private Map<String, DataSetType> dataSetTypes = new HashMap<String, DataSetType>();

    private Map<String, ExperimentType> experimentTypes = new HashMap<String, ExperimentType>();

    private Map<String, MaterialType> materialTypes = new HashMap<String, MaterialType>();
    private Map<String, NodeList> materialTypePropertyAssignmentsMap = new HashMap<String, NodeList>();

    private Map<String, NewVocabulary> vocabularies = new HashMap<String, NewVocabulary>();

    MultiKeyMap<String, List<NewETPTAssignment>> entityPropertyAssignments = new MultiKeyMap<String, List<NewETPTAssignment>>();

    public Map<String, NewVocabulary> getVocabularies()
    {
        return vocabularies;
    }

    private MasterDataParser(INameTranslator nameTranslator)
    {
        this.nameTranslator = nameTranslator;
    }

    public static MasterDataParser create(INameTranslator nameTranslator)
    {
        if (nameTranslator == null)
        {
            return create();
        }
        return new MasterDataParser(nameTranslator);
    }

    public static MasterDataParser create()
    {
        return create(new DefaultNameTranslator());
    }

    public void parseMasterData(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        XPathExpression expr =
                xpath.compile("//s:url/s:loc[normalize-space(.)='" + uri + "']//following-sibling::*[local-name() = 'masterData'][1]");
        Node xdNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (xdNode == null)
        {
            throw new XPathExpressionException("The master data resurce list should contain 1 master data element");
        }
        Element docElement = (Element) xdNode;
        parseVocabularies(docElement.getElementsByTagName("vocabularies"));
        parseMaterialTypes(docElement.getElementsByTagName("materialTypes"));
        parsePropertyTypes(docElement.getElementsByTagName("propertyTypes"));
        parseSampleTypes(docElement.getElementsByTagName("sampleTypes"));
        parseDataSetTypes(docElement.getElementsByTagName("dataSetTypes"));
        parseExperimentTypes(docElement.getElementsByTagName("experimentTypes"));
    }

    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    public MultiKeyMap<String, List<NewETPTAssignment>> getEntityPropertyAssignments()
    {
        return entityPropertyAssignments;
    }

    public Map<String, SampleType> getSampleTypes()
    {
        return sampleTypes;
    }

    public Map<String, DataSetType> getDataSetTypes()
    {
        return dataSetTypes;
    }

    public Map<String, ExperimentType> getExperimentTypes()
    {
        return experimentTypes;
    }

    public Map<String, MaterialType> getMaterialTypes()
    {
        return materialTypes;
    }

    private void parseVocabularies(NodeList vocabulariesNode)
    {
        if (vocabulariesNode.getLength() == 1)
        {
            Element vocabsElement = (Element) vocabulariesNode.item(0);
            NodeList vocabNodes = vocabsElement.getElementsByTagName("vocabulary");

            for (int i = 0; i < vocabNodes.getLength(); i++)
            {
                Element vocabElement = (Element) vocabNodes.item(i);
                String code = getAttribute(vocabElement, "code");

                NewVocabulary newVocabulary = new NewVocabulary();
                newVocabulary.setCode(code);
                newVocabulary.setDescription(getAttribute(vocabElement, "description"));
                newVocabulary.setURLTemplate(getAttribute(vocabElement, "urlTemplate"));
                newVocabulary.setManagedInternally(Boolean.valueOf(getAttribute(vocabElement, "managedInternally")));
                newVocabulary.setInternalNamespace(Boolean.valueOf(getAttribute(vocabElement, "internalNamespace")));
                newVocabulary.setChosenFromList(Boolean.valueOf(getAttribute(vocabElement, "chosenFromList")));
                vocabularies.put(CodeConverter.tryToBusinessLayer(newVocabulary.getCode(), newVocabulary.isInternalNamespace()), newVocabulary);
                parseVocabularyTerms(vocabElement, newVocabulary);
            }
        }
    }

    private void parseVocabularyTerms(Element vocabElement, NewVocabulary newVocabulary)
    {
        NodeList termNodes = vocabElement.getElementsByTagName("term");
        for (int i = 0; i < termNodes.getLength(); i++)
        {
            Element termElement = (Element) termNodes.item(i);
            VocabularyTerm newVocabularyTerm = new VocabularyTerm();
            newVocabularyTerm.setCode(getAttribute(termElement, "code"));
            newVocabularyTerm.setLabel(getAttribute(termElement, "label"));
            newVocabularyTerm.setDescription(getAttribute(termElement, "description"));
            newVocabularyTerm.setOrdinal(Long.valueOf(getAttribute(termElement, "ordinal")));
            // TODO setUrl? There is no way to set it
            newVocabulary.getTerms().add(newVocabularyTerm);
        }
    }

    private String getAttribute(Element termElement, String attr)
    {
        return termElement.getAttributes().getNamedItem(attr).getTextContent();
    }

    private void parseMaterialTypes(NodeList matTypesNode)
    {
        if (matTypesNode.getLength() == 1)
        {
            Element matTypesElement = (Element) matTypesNode.item(0);
            NodeList matTypeNodes = matTypesElement.getElementsByTagName("materialType");
            for (int i = 0; i < matTypeNodes.getLength(); i++)
            {
                Element materialTypeElement = (Element) matTypeNodes.item(i);
                MaterialType materialType = new MaterialType();
                materialType.setCode(getAttribute(materialTypeElement, "code"));
                materialType.setDescription(getAttribute(materialTypeElement, "description"));
                materialTypes.put(materialType.getCode(), materialType);

                parsePropertyAssignments(EntityKind.MATERIAL, materialType, materialTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseExperimentTypes(NodeList expTypesNode)
    {
        if (expTypesNode.getLength() == 1)
        {
            Element expTypesElement = (Element) expTypesNode.item(0);
            NodeList expTypeNodes = expTypesElement.getElementsByTagName("experimentType");
            for (int i = 0; i < expTypeNodes.getLength(); i++)
            {
                Element expTypeElement = (Element) expTypeNodes.item(i);
                String code = getAttribute(expTypeElement, "code");
                ExperimentType expType = new ExperimentType();
                expType.setCode(code);
                expType.setDescription(getAttribute(expTypeElement, "description"));
                experimentTypes.put(expType.getCode(), expType);

                parsePropertyAssignments(EntityKind.EXPERIMENT, expType, expTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseSampleTypes(NodeList sampleTypesNode)
    {
        if (sampleTypesNode.getLength() == 1)
        {
            Element sampleTypesElement = (Element) sampleTypesNode.item(0);
            NodeList sampleTypeNodes = sampleTypesElement.getElementsByTagName("sampleType");
            for (int i = 0; i < sampleTypeNodes.getLength(); i++)
            {
                Element sampleTypeElement = (Element) sampleTypeNodes.item(i);
                SampleType sampleType = new SampleType();
                sampleType.setCode(getAttribute(sampleTypeElement, "code"));
                sampleType.setDescription(getAttribute(sampleTypeElement, "description"));
                sampleType.setListable(Boolean.valueOf(getAttribute(sampleTypeElement, "listable")));
                sampleType.setShowContainer(Boolean.valueOf(getAttribute(sampleTypeElement, "showContainer")));
                sampleType.setShowParents(Boolean.valueOf(getAttribute(sampleTypeElement, "showParents")));
                sampleType.setShowParentMetadata(Boolean.valueOf(getAttribute(sampleTypeElement, "showParentMetadata")));
                sampleType.setSubcodeUnique(Boolean.valueOf(getAttribute(sampleTypeElement, "subcodeUnique")));
                sampleType.setAutoGeneratedCode(Boolean.valueOf(getAttribute(sampleTypeElement, "autoGeneratedCode")));
                sampleType.setGeneratedCodePrefix(getAttribute(sampleTypeElement, "generatedCodePrefix"));
                sampleTypes.put(sampleType.getCode(), sampleType);

                parsePropertyAssignments(EntityKind.SAMPLE, sampleType, sampleTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseDataSetTypes(NodeList dataSetTypesNode)
    {
        if (dataSetTypesNode.getLength() == 1)
        {
            Element dataSetTypesElement = (Element) dataSetTypesNode.item(0);
            NodeList dataSetTypeNodes = dataSetTypesElement.getElementsByTagName("dataSetType");
            for (int i = 0; i < dataSetTypeNodes.getLength(); i++)
            {
                Element dataSetTypeElement = (Element) dataSetTypeNodes.item(i);
                String code = getAttribute(dataSetTypeElement, "code");
                DataSetType dataSetType = new DataSetType();
                dataSetType.setCode(code);
                dataSetType.setDescription(getAttribute(dataSetTypeElement, "description"));
                dataSetType.setDataSetKind(DataSetKind.valueOf(getAttribute(dataSetTypeElement, "dataSetKind")));
                String mainDataSetPattern = getAttribute(dataSetTypeElement, "mainDataSetPattern");
                if (mainDataSetPattern.length() < 1)
                {
                    dataSetType.setMainDataSetPattern(null);
                }
                else
                {
                    dataSetType.setMainDataSetPattern(mainDataSetPattern);
                }
                if (mainDataSetPattern.length() < 1)
                {
                    dataSetType.setMainDataSetPath(null);
                }
                else
                {
                    dataSetType.setMainDataSetPath(mainDataSetPattern);
                }
                dataSetType.setDeletionDisallow(Boolean.valueOf(getAttribute(dataSetTypeElement, "deletionDisallowed")));
                dataSetTypes.put(dataSetType.getCode(), dataSetType);

                parsePropertyAssignments(EntityKind.DATA_SET, dataSetType, dataSetTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parsePropertyAssignments(EntityKind entityKind, EntityType entityType, NodeList propertyAssignmentsNode)
    {
        if (propertyAssignmentsNode.getLength() == 1)
        {
            List<NewETPTAssignment> list = new ArrayList<NewETPTAssignment>();
            Element propertyAssignmentsElement = (Element) propertyAssignmentsNode.item(0);
            NodeList propertyAssignmentNodes = propertyAssignmentsElement.getElementsByTagName("propertyAssignment");
            for (int i = 0; i < propertyAssignmentNodes.getLength(); i++)
            {
                Element propertyAssignmentElement = (Element) propertyAssignmentNodes.item(i);
                String propertyTypeCode = getAttribute(propertyAssignmentElement, "property_type_code");
                NewETPTAssignment assignment = new NewETPTAssignment();
                assignment.setPropertyTypeCode(propertyTypeCode);
                assignment.setEntityKind(entityType.getEntityKind());
                assignment.setEntityTypeCode(entityType.getCode());
                assignment.setMandatory(Boolean.valueOf(getAttribute(propertyAssignmentElement, "mandatory")));
                assignment.setDefaultValue(ERROR_PROPERTY_PREFIX);
                assignment.setSection(getAttribute(propertyAssignmentElement, "section"));
                assignment.setOrdinal(Long.valueOf(getAttribute(propertyAssignmentElement, "ordinal")));
                assignment.setShownInEditView(Boolean.valueOf(getAttribute(propertyAssignmentElement, "showInEdit")));
                list.add(assignment);
            }
            entityPropertyAssignments.put(entityType.getEntityKind().name(), entityType.getCode(), list);
        }
    }

    private void parsePropertyTypes(NodeList propertyTypesNode)
    {
        if (propertyTypesNode.getLength() == 1)
        {
            Element propertyTypesElement = (Element) propertyTypesNode.item(0);
            NodeList propertyTypeNodes = propertyTypesElement.getElementsByTagName("propertyType");
            for (int i = 0; i < propertyTypeNodes.getLength(); i++)
            {
                Element propertyTypeElement = (Element) propertyTypeNodes.item(i);
                PropertyType newPropertyType =  new PropertyType();
                
                String code = getAttribute(propertyTypeElement, "code");
                newPropertyType.setLabel(getAttribute(propertyTypeElement, "label"));
                DataTypeCode dataTypeCode = DataTypeCode.valueOf(getAttribute(propertyTypeElement, "dataType"));
                newPropertyType.setDataType(new DataType(dataTypeCode));
                newPropertyType.setDescription(getAttribute(propertyTypeElement, "description"));
                newPropertyType.setManagedInternally(Boolean.valueOf(getAttribute(propertyTypeElement, "managedInternally")));
                newPropertyType.setInternalNamespace(Boolean.valueOf(getAttribute(propertyTypeElement, "internalNamespace")));
                newPropertyType.setCode(CodeConverter.tryToBusinessLayer(code, newPropertyType.isInternalNamespace()));

                propertyTypes.put(newPropertyType.getCode(), newPropertyType);
                if (dataTypeCode.equals(DataTypeCode.CONTROLLEDVOCABULARY))
                {
                    String vocabularyCode = getAttribute(propertyTypeElement, "vocabulary");
                    newPropertyType.setVocabulary(vocabularies.get(vocabularyCode));
                }
                else if (dataTypeCode.equals(DataTypeCode.MATERIAL))
                {
                    String materialCode = getAttribute(propertyTypeElement, "material");
                    if (materialCode.trim().length() < 1)
                    {
                        newPropertyType.setMaterialType(null); // material of any type
                    }
                    else
                    {
                        newPropertyType.setMaterialType(materialTypes.get(materialCode));
                    }
              }
            }
        }
    }
}
