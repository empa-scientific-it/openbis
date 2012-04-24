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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IAbstractType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IEntityType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentTypeImmutable;
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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;
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

    private final List<MaterialType> createdMaterialTypes = new ArrayList<MaterialType>();

    private final List<PropertyType> createdPropertyTypes = new ArrayList<PropertyType>();

    private final List<FileFormatType> createdFileTypes = new ArrayList<FileFormatType>();

    private final List<Vocabulary> createdVocabularies = new ArrayList<Vocabulary>();

    private final List<PropertyAssignment> createdAssignments = new ArrayList<PropertyAssignment>();

    private final MasterDataTransactionErrors transactionErrors = new MasterDataTransactionErrors();

    MasterDataRegistrationTransaction(EncapsulatedCommonServer commonServer)
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

    public IExperimentType createNewExperimentType(String code)
    {
        ExperimentType experimentType = new ExperimentType(code);
        createdExperimentTypes.add(experimentType);
        return experimentType;
    }

    public IExperimentTypeImmutable getExperimentType(String code)
    {
        return findTypeForCode(commonServer.listExperimentTypes(), code);
    }

    public IExperimentType getOrCreateNewExperimentType(String code)
    {
        final IExperimentTypeImmutable experimentType = getExperimentType(code);
        if (experimentType != null)
        {
            return createAdapter(IExperimentType.class, experimentType);
        }
        return createNewExperimentType(code);
    }

    public List<IExperimentTypeImmutable> listExperimentTypes()
    {
        return commonServer.listExperimentTypes();
    }

    public ISampleType createNewSampleType(String code)
    {
        SampleType sampleType = new SampleType(code);
        createdSampleTypes.add(sampleType);
        return sampleType;
    }

    public ISampleTypeImmutable getSampleType(String code)
    {
        return findTypeForCode(commonServer.listSampleTypes(), code);
    }

    public ISampleType getOrCreateNewSampleType(String code)
    {
        ISampleTypeImmutable sampleType = getSampleType(code);
        if (sampleType != null)
        {
            return createAdapter(ISampleType.class, sampleType);
        }
        return createNewSampleType(code);
    }

    public List<ISampleTypeImmutable> listSampleTypes()
    {
        return commonServer.listSampleTypes();
    }

    public IDataSetType createNewDataSetType(String code)
    {
        DataSetType dataSetType = new DataSetType(code);
        createdDataSetTypes.add(dataSetType);
        return dataSetType;
    }

    public IDataSetTypeImmutable getDataSetType(String code)
    {
        return findTypeForCode(commonServer.listDataSetTypes(), code);
    }

    public IDataSetType getOrCreateNewDataSetType(String code)
    {
        IDataSetTypeImmutable dataSetType = getDataSetType(code);
        if (dataSetType != null)
        {
            return createAdapter(IDataSetType.class, dataSetType);
        }
        return createNewDataSetType(code);
    }

    public List<IDataSetTypeImmutable> listDataSetTypes()
    {
        return commonServer.listDataSetTypes();
    }

    public IMaterialType createNewMaterialType(String code)
    {
        MaterialType materialType = new MaterialType(code);
        createdMaterialTypes.add(materialType);
        return materialType;
    }

    public IMaterialTypeImmutable getMaterialType(String code)
    {
        return findTypeForCode(commonServer.listMaterialTypes(), code);
    }

    public IMaterialType getOrCreateNewMaterialType(String code)
    {
        IMaterialTypeImmutable materialType = getMaterialType(code);
        if (materialType != null)
        {
            return createAdapter(IMaterialType.class, materialType);
        }
        return createNewMaterialType(code);
    }

    public List<IMaterialTypeImmutable> listMaterialTypes()
    {
        return commonServer.listMaterialTypes();
    }

    public IFileFormatType createNewFileFormatType(String code)
    {
        FileFormatType fileFormatType = new FileFormatType(code);
        createdFileTypes.add(fileFormatType);
        return fileFormatType;
    }

    public IFileFormatTypeImmutable getFileFormatType(String code)
    {
        return findTypeForCode(commonServer.listFileFormatTypes(), code);
    }

    public IFileFormatType getOrCreateNewFileFormatType(String code)
    {
        IFileFormatTypeImmutable fileFormatType = getFileFormatType(code);
        if (fileFormatType != null)
        {
            return createAdapter(IFileFormatType.class, fileFormatType);
        }
        return createNewFileFormatType(code);
    }

    public List<IFileFormatTypeImmutable> listFileFormatTypes()
    {
        return commonServer.listFileFormatTypes();
    }

    public IPropertyType createNewPropertyType(String code, DataType dataType)
    {
        PropertyType propertyType = new PropertyType(code, dataType);
        createdPropertyTypes.add(propertyType);
        return propertyType;
    }

    public IPropertyTypeImmutable getPropertyType(String code)
    {
        List<IPropertyTypeImmutable> propertyTypes = commonServer.listPropertyTypes();
        for (IPropertyTypeImmutable propertyType : propertyTypes)
        {
            String fullCode = (propertyType.isInternalNamespace() ? "$" : "") + code;
            if (propertyType.getCode().equalsIgnoreCase(fullCode))
            {
                return propertyType;
            }
        }
        return null;
    }

    public IPropertyType getOrCreateNewPropertyType(String code, DataType dataType)
    {
        IPropertyTypeImmutable propertyType = getPropertyType(code);
        if (propertyType != null)
        {
            return createAdapter(IPropertyType.class, propertyType);
        }
        return createNewPropertyType(code, dataType);
    }

    public List<IPropertyTypeImmutable> listPropertyTypes()
    {
        return commonServer.listPropertyTypes();
    }

    public IPropertyAssignment assignPropertyType(IEntityType entityType,
            IPropertyTypeImmutable propertyType)
    {
        EntityKind entityKind = EntityKind.valueOf(entityType.getEntityKind().name());
        IPropertyAssignmentImmutable assigment = findAssignment(entityType, propertyType);
        if (assigment != null)
        {
            return createAdapter(IPropertyAssignment.class, assigment);
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
            String fullCode = (vocabulary.isInternalNamespace() ? "$" : "") + code;
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
                && propertyType.isInternalNamespace())
        {
            propTypeCode = CodeConverter.tryToBusinessLayer(propTypeCode, true);
        }
        PropertyAssignment assignment =
                new PropertyAssignment(entityKind, type.getCode(), propTypeCode);
        createdAssignments.add(assignment);
        return assignment;

    }

    public List<IPropertyAssignmentImmutable> listPropertyAssignments()
    {
        return commonServer.listPropertyAssignments();
    }

    public IVocabularyTerm createNewVocabularyTerm(String code)
    {
        return new VocabularyTerm(code);
    }

    public IVocabulary createNewVocabulary(String code)
    {
        Vocabulary vocabulary = new Vocabulary(code);
        createdVocabularies.add(vocabulary);
        return vocabulary;
    }

    public IVocabularyImmutable getVocabulary(String code)
    {
        return findVocabularyForCode(commonServer.listVocabularies(), code);
    }

    public IVocabulary getOrCreateNewVocabulary(String code)
    {
        IVocabularyImmutable vocabulary = getVocabulary(code);
        if (vocabulary != null)
        {
            return createAdapter(IVocabulary.class, vocabulary);
        }
        return createNewVocabulary(code);
    }

    public List<IVocabularyImmutable> listVocabularies()
    {
        return commonServer.listVocabularies();
    }

    void commit()
    {
        registerFileFormatTypes(createdFileTypes);
        registerVocabularies(createdVocabularies);
        registerExperimentTypes(createdExperimentTypes);
        registerSampleTypes(createdSampleTypes);
        registerDataSetTypes(createdDataSetTypes);
        registerMaterialTypes(createdMaterialTypes);
        registerPropertyTypes(createdPropertyTypes);
        registerPropertyAssignments(createdAssignments);
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

    @SuppressWarnings("unchecked")
    private <T> T createAdapter(final Class<T> interfaze, final Object object)
    {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]
            { interfaze }, new InvocationHandler()
            {

                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    try
                    {
                        String name = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Method objectMethod = object.getClass().getMethod(name, parameterTypes);
                        return objectMethod.invoke(object, args);
                    } catch (NoSuchMethodException ex)
                    {
                        return null;
                    }
                }
            });
    }
}
