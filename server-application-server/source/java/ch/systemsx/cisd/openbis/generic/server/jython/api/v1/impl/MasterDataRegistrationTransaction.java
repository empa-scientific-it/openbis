/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IAbstractType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IEntityType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignmentImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScript;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTermImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationTransaction implements IMasterDataRegistrationTransaction
{
    private final EncapsulatedCommonServer commonServer;

    private final List<ExperimentType> createdExperimentTypes = new ArrayList<ExperimentType>();

    private final List<SampleType> createdSampleTypes = new ArrayList<SampleType>();

    private final List<DataSetType> createdDataSetTypes = new ArrayList<DataSetType>();

    private final List<Script> createdScripts = new ArrayList<Script>();

    private final List<MaterialType> createdMaterialTypes = new ArrayList<MaterialType>();

    private final List<PropertyType> createdPropertyTypes = new ArrayList<PropertyType>();

    private final List<FileFormatType> createdFileTypes = new ArrayList<FileFormatType>();

    private final List<Vocabulary> createdVocabularies = new ArrayList<Vocabulary>();

    private final Map<Long, List<VocabularyTerm>> createdVocabularyTerms =
            new HashMap<Long, List<VocabularyTerm>>();

    private final List<VocabularyTermImmutable> updatedVocabularyTerms =
            new ArrayList<VocabularyTermImmutable>();

    private final List<PropertyAssignment> createdAssignments = new ArrayList<PropertyAssignment>();

    private final List<ExternalDataManagementSystem> createdExternalDataManagementSystems =
            new ArrayList<ExternalDataManagementSystem>();

    private final MasterDataTransactionErrors transactionErrors = new MasterDataTransactionErrors();

    public MasterDataRegistrationTransaction(EncapsulatedCommonServer commonServer)
    {
        this.commonServer = commonServer;
    }

    public MasterDataTransactionErrors getTransactionErrors()
    {
        return transactionErrors;
    }

    public boolean hasErrors()
    {
        return transactionErrors.hasErrors();
    }

    @Override
    public IExperimentType createNewExperimentType(String code)
    {
        ExperimentType experimentType = new ExperimentType(code);
        createdExperimentTypes.add(experimentType);
        return experimentType;
    }

    @Override
    public IExperimentTypeImmutable getExperimentType(String code)
    {
        return findTypeForCode(commonServer.listExperimentTypes(), code);
    }

    @Override
    public IExperimentType getOrCreateNewExperimentType(String code)
    {
        final IExperimentTypeImmutable experimentType = getExperimentType(code);
        if (experimentType != null)
        {
            return new ExperimentTypeWrapper((ExperimentTypeImmutable) experimentType);
        }
        return createNewExperimentType(code);
    }

    @Override
    public List<IExperimentTypeImmutable> listExperimentTypes()
    {
        return commonServer.listExperimentTypes();
    }

    @Override
    public ISampleType createNewSampleType(String code)
    {
        SampleType sampleType = new SampleType(code);
        createdSampleTypes.add(sampleType);
        return sampleType;
    }

    @Override
    public ISampleTypeImmutable getSampleType(String code)
    {
        return findTypeForCode(commonServer.listSampleTypes(), code);
    }

    @Override
    public ISampleType getOrCreateNewSampleType(String code)
    {
        ISampleTypeImmutable sampleType = getSampleType(code);
        if (sampleType != null)
        {
            return new SampleTypeWrapper((SampleTypeImmutable) sampleType);
        }
        return createNewSampleType(code);
    }

    @Override
    public List<ISampleTypeImmutable> listSampleTypes()
    {
        return commonServer.listSampleTypes();
    }

    @Override
    public IDataSetType createNewDataSetType(String code)
    {
        DataSetType dataSetType = new DataSetType(code);
        createdDataSetTypes.add(dataSetType);
        return dataSetType;
    }

    @Override
    public IDataSetTypeImmutable getDataSetType(String code)
    {
        return findTypeForCode(commonServer.listDataSetTypes(), code);
    }

    @Override
    public IDataSetType getOrCreateNewDataSetType(String code)
    {
        IDataSetTypeImmutable dataSetType = getDataSetType(code);
        if (dataSetType != null)
        {
            return new DataSetTypeWrapper((DataSetTypeImmutable) dataSetType);
        }
        return createNewDataSetType(code);
    }

    @Override
    public List<IDataSetTypeImmutable> listDataSetTypes()
    {
        return commonServer.listDataSetTypes();
    }

    @Override
    public IScriptImmutable getScript(String code)
    {
        return findTypeForCode(commonServer.listScripts(), code);
    }

    @Override
    public IScript getOrCreateNewScript(String code)
    {
        IScriptImmutable script = getScript(code);
        if (script != null)
        {
            return new ScriptWrapper((ScriptImmutable) script);
        }
        return createNewScript();
    }

    private IScript createNewScript()
    {
        Script script = new Script();
        createdScripts.add(script);
        return script;
    }

    @Override
    public List<IScriptImmutable> listScripts()
    {
        return commonServer.listScripts();
    }

    @Override
    public IMaterialType createNewMaterialType(String code)
    {
        MaterialType materialType = new MaterialType(code);
        createdMaterialTypes.add(materialType);
        return materialType;
    }

    @Override
    public IMaterialTypeImmutable getMaterialType(String code)
    {
        return findTypeForCode(commonServer.listMaterialTypes(), code);
    }

    @Override
    public IMaterialType getOrCreateNewMaterialType(String code)
    {
        IMaterialTypeImmutable materialType = getMaterialType(code);
        if (materialType != null)
        {
            return new MaterialTypeWrapper((MaterialTypeImmutable) materialType);
        }
        return createNewMaterialType(code);
    }

    @Override
    public List<IMaterialTypeImmutable> listMaterialTypes()
    {
        return commonServer.listMaterialTypes();
    }

    @Override
    public IFileFormatType createNewFileFormatType(String code)
    {
        FileFormatType fileFormatType = new FileFormatType(code);
        createdFileTypes.add(fileFormatType);
        return fileFormatType;
    }

    @Override
    public IFileFormatTypeImmutable getFileFormatType(String code)
    {
        return findTypeForCode(commonServer.listFileFormatTypes(), code);
    }

    @Override
    public IFileFormatType getOrCreateNewFileFormatType(String code)
    {
        IFileFormatTypeImmutable fileFormatType = getFileFormatType(code);
        if (fileFormatType != null)
        {
            return new FileFormatTypeWrapper((FileFormatTypeImmutable) fileFormatType);
        }
        return createNewFileFormatType(code);
    }

    @Override
    public List<IFileFormatTypeImmutable> listFileFormatTypes()
    {
        return commonServer.listFileFormatTypes();
    }

    @Override
    public IPropertyType createNewPropertyType(String code, DataType dataType)
    {
        PropertyType propertyType = new PropertyType(code, dataType);
        createdPropertyTypes.add(propertyType);
        return propertyType;
    }

    @Override
    public IPropertyTypeImmutable getPropertyType(String code)
    {
        List<IPropertyTypeImmutable> propertyTypes = commonServer.listPropertyTypes();
        for (IPropertyTypeImmutable propertyType : propertyTypes)
        {
            String fullCode = (propertyType.isManagedInternally() ? "$" : "") + code;
            if (propertyType.getCode().equalsIgnoreCase(fullCode))
            {
                return propertyType;
            }
        }
        return null;
    }

    @Override
    public IPropertyType getOrCreateNewPropertyType(String code, DataType dataType)
    {
        IPropertyTypeImmutable propertyType = getPropertyType(code);
        if (propertyType != null)
        {
            return new PropertyTypeWrapper((PropertyTypeImmutable) propertyType);
        }
        return createNewPropertyType(code, dataType);
    }

    @Override
    public List<IPropertyTypeImmutable> listPropertyTypes()
    {
        return commonServer.listPropertyTypes();
    }

    @Override
    public IPropertyAssignment assignPropertyType(IEntityType entityType,
            IPropertyTypeImmutable propertyType)
    {
        EntityKind entityKind = EntityKind.valueOf(entityType.getEntityKind().name());
        IPropertyAssignmentImmutable assigment = findAssignment(entityType, propertyType);
        if (assigment != null)
        {
            return new PropertyAssignmentWrapper((PropertyAssignmentImmutable) assigment);
        }
        return createAssignment(entityKind, entityType, propertyType);
    }

    private <T extends IAbstractType> T findTypeForCode(List<T> types, String code)
    {
        for (T type : types)
        {
            if (type.getCode().equalsIgnoreCase(code))
            {
                return type;
            }
        }
        return null;
    }

    private IPropertyAssignmentImmutable findAssignment(IEntityType entityType,
            IPropertyTypeImmutable propertyType)
    {
        for (IPropertyAssignmentImmutable assignment : listPropertyAssignments())
        {
            if (assignment.getEntityKind().equals(entityType.getEntityKind())
                    && assignment.getEntityTypeCode().equalsIgnoreCase(entityType.getCode())
                    && assignment.getPropertyTypeCode().equalsIgnoreCase(propertyType.getCode()))
            {
                return assignment;
            }
        }
        return null;
    }

    private IVocabularyImmutable findVocabularyForCode(List<IVocabularyImmutable> vocabularies,
            String code)
    {
        for (IVocabularyImmutable vocabulary : vocabularies)
        {
            String fullCode = (vocabulary.isManagedInternally() ? "$" : "") + code;
            if (vocabulary.getCode().equalsIgnoreCase(fullCode))
            {
                return vocabulary;
            }
        }
        return null;
    }

    private PropertyAssignment createAssignment(EntityKind entityKind, IEntityType type,
            IPropertyTypeImmutable propertyType)
    {
        String propTypeCode = propertyType.getCode();
        if (false == CodeConverter.isInternalNamespace(propTypeCode)
                && propertyType.isManagedInternally())
        {
            propTypeCode = CodeConverter.tryToBusinessLayer(propTypeCode, true);
        }
        PropertyAssignment assignment =
                new PropertyAssignment(entityKind, type.getCode(), propTypeCode);
        createdAssignments.add(assignment);
        return assignment;

    }

    @Override
    public List<IPropertyAssignmentImmutable> listPropertyAssignments()
    {
        return commonServer.listPropertyAssignments();
    }

    @Override
    public IVocabularyTerm createNewVocabularyTerm(String code)
    {
        return new VocabularyTerm(code);
    }

    @Override
    public IVocabulary createNewVocabulary(String code)
    {
        Vocabulary vocabulary = new Vocabulary(code);
        createdVocabularies.add(vocabulary);
        return vocabulary;
    }

    @Override
    public IVocabularyTerm getVocabularyTerm(IVocabularyImmutable vocabulary,
            String vocabularyTermCode)
    {
        List<IVocabularyTermImmutable> terms = vocabulary.getTerms();
        for (IVocabularyTermImmutable term : terms)
        {
            if (vocabularyTermCode.equalsIgnoreCase(term.getCode()))
            {
                return new VocabularyTerm(((VocabularyTermImmutable) term).getVocabularyTerm());
            }
        }
        throw new IllegalArgumentException("Vocabulary " + vocabulary.getCode() + " has no term "
                + vocabularyTermCode.toUpperCase() + ".");
    }

    @Override
    public void updateVocabularyTerm(IVocabularyTerm term)
    {
        VocabularyTermImmutable t = (VocabularyTermImmutable) term;
        if (t.getVocabularyTerm().getId() != null)
        {
            updatedVocabularyTerms.add(t);
        }
    }

    @Override
    public IVocabularyImmutable getVocabulary(String code)
    {
        return findVocabularyForCode(commonServer.listVocabularies(), code);
    }

    @Override
    public IVocabulary getOrCreateNewVocabulary(String code)
    {
        IVocabularyImmutable vocabulary = getVocabulary(code);
        if (vocabulary != null)
        {
            return new VocabularyWrapper((VocabularyImmutable) vocabulary, createdVocabularyTerms);
        }
        return createNewVocabulary(code);
    }

    @Override
    public List<IVocabularyImmutable> listVocabularies()
    {
        return commonServer.listVocabularies();
    }

    @Override
    public IExternalDataManagementSystem createNewExternalDataManagementSystem(String code)
    {
        ExternalDataManagementSystem edms = new ExternalDataManagementSystem(code);
        createdExternalDataManagementSystems.add(edms);
        return edms;
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(String code)
    {
        return commonServer.getExternalDataManagementSystem(code);
    }

    @Override
    public IExternalDataManagementSystem getOrCreateNewExternalDataManagementSystem(String code)
    {
        final IExternalDataManagementSystemImmutable edms = getExternalDataManagementSystem(code);
        if (edms != null)
        {
            return new ExternalDataManagementSystemWrapper(
                    (ExternalDataManagementSystemImmutable) edms);
        }
        return createNewExternalDataManagementSystem(code);
    }

    @Override
    public List<IExternalDataManagementSystemImmutable> listExternalDataManagementSystems()
    {
        return commonServer.listExternalDataManagementSystems();
    }

    public void commit()
    {
        registerFileFormatTypes(createdFileTypes);
        registerVocabularies(createdVocabularies);
        addVocabularyTerms(createdVocabularyTerms);
        updateVocabularyTerms(updatedVocabularyTerms);
        registerScripts(createdScripts);
        registerExperimentTypes(createdExperimentTypes);
        registerSampleTypes(createdSampleTypes);
        registerDataSetTypes(createdDataSetTypes);
        registerMaterialTypes(createdMaterialTypes);
        registerPropertyTypes(createdPropertyTypes);
        registerPropertyAssignments(createdAssignments);
        registerExternalDataManagementSystems(createdExternalDataManagementSystems);
    }

    private void registerFileFormatTypes(List<FileFormatType> fileFormatTypes)
    {
        for (FileFormatType fileFormatType : fileFormatTypes)
        {
            try
            {
                commonServer.registerFileFormatType(fileFormatType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, fileFormatType);
            }
        }
    }

    private void registerExperimentTypes(List<ExperimentType> experimentTypes)
    {
        for (ExperimentType experimentType : experimentTypes)
        {
            try
            {
                commonServer.registerExperimentType(experimentType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, experimentType);
            }
        }
    }

    private void registerSampleTypes(List<SampleType> sampleTypes)
    {
        for (SampleType sampleType : sampleTypes)
        {
            try
            {
                commonServer.registerSampleType(sampleType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, sampleType);
            }
        }
    }

    private void registerDataSetTypes(List<DataSetType> dataSetTypes)
    {
        for (DataSetType dataSetType : dataSetTypes)
        {
            try
            {
                commonServer.registerDataSetType(dataSetType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, dataSetType);
            }
        }
    }

    private void registerScripts(List<Script> scripts)
    {
        for (Script script : scripts)
        {
            try
            {
                commonServer.registerScript(script);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, script);
            }
        }
    }

    private void registerMaterialTypes(List<MaterialType> materialTypes)
    {
        for (MaterialType materialType : materialTypes)
        {
            try
            {
                commonServer.registerMaterialType(materialType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, materialType);
            }
        }
    }

    private void registerPropertyTypes(List<PropertyType> propertyTypes)
    {
        for (PropertyType propertyType : propertyTypes)
        {
            try
            {
                commonServer.registerPropertyType(propertyType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, propertyType);
            }
        }
    }

    private void registerPropertyAssignments(List<PropertyAssignment> propertyAssigments)
    {
        for (PropertyAssignment assignment : propertyAssigments)
        {
            try
            {
                commonServer.registerPropertyAssignment(assignment);
            } catch (Exception ex)
            {
                transactionErrors.addPropertyAssignmentError(ex, assignment);
            }
        }
    }

    private void registerVocabularies(List<Vocabulary> vocabularies)
    {
        for (Vocabulary vocabulary : vocabularies)
        {
            try
            {
                commonServer.registerVocabulary(vocabulary);
            } catch (Exception ex)
            {
                transactionErrors.addVocabularyRegistrationError(ex, vocabulary);
            }
        }
    }

    private void addVocabularyTerms(Map<Long, List<VocabularyTerm>> terms)
    {
        Set<Entry<Long, List<VocabularyTerm>>> entrySet = terms.entrySet();
        for (Entry<Long, List<VocabularyTerm>> entry : entrySet)
        {
            Long vocaId = entry.getKey();
            List<VocabularyTerm> newTerms = entry.getValue();
            try
            {
                commonServer.addVocabularyTerms(vocaId, newTerms, null);
            } catch (Exception ex)
            {
                transactionErrors.addVocabularyTermsRegistrationError(ex, newTerms);
            }
        }
    }

    private void updateVocabularyTerms(List<VocabularyTermImmutable> terms)
    {
        for (VocabularyTermImmutable term : terms)
        {
            try
            {
                commonServer.update(term);
            } catch (Exception ex)
            {
                transactionErrors.updateVocabularyTermError(ex, term);
            }
        }
    }

    private void registerExternalDataManagementSystems(List<ExternalDataManagementSystem> edmss)
    {
        for (ExternalDataManagementSystem edms : edmss)
        {
            commonServer.createOrUpdateExternalDataManagementSystem(edms);
        }
    }

}
