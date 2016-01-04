/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IAddTagToEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleCodeGeneratorByType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateSampleExecutor extends AbstractCreateEntityExecutor<SampleCreation, SamplePE, SamplePermId> implements ICreateSampleExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private ISetSampleTypeExecutor setSampleTypeExecutor;

    @Autowired
    private ISetSampleSpaceExecutor setSampleSpaceExecutor;
    
    @Autowired
    private ISetSampleProjectExecutor setSampleProjectExecutor;

    @Autowired
    private ISetSampleExperimentExecutor setSampleExperimentExecutor;

    @Autowired
    private ISetSampleRelatedSamplesExecutor setSampleRelatedSamplesExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifySampleExecutor verifySampleExecutor;
    
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    protected List<SamplePE> createEntities(IOperationContext context, Collection<SampleCreation> creations)
    {
        // Get Types
        Collection<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

        for (SampleCreation creation : creations)
        {
            typeIds.add(creation.getTypeId());
        }

        Map<IEntityTypeId, EntityTypePE> types = mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, typeIds);
        
        Properties serviceProperties = configurer.getResolvedProps();
        boolean createContinuousSampleCodes = PropertyUtils.getBoolean(serviceProperties, Constants.CREATE_CONTINUOUS_SAMPLES_CODES_KEY, false);

        EntityCodeGenerator codeGenerator;
        if (createContinuousSampleCodes)
        {
            codeGenerator = new SampleCodeGeneratorByType(daoFactory);
        } else
        {
            codeGenerator = new EntityCodeGenerator(daoFactory);
        }

        // Validate Sample creations using types
        Map<String, Integer> numCodesByPrefix = new HashMap<String, Integer>();
        for (SampleCreation creation : creations)
        {
            SampleTypePE type = (SampleTypePE) types.get(creation.getTypeId());
            checkData(context, creation, type);
            if (StringUtils.isEmpty(creation.getCode()) && (type.isAutoGeneratedCode() || creation.isAutoGeneratedCode()))
            {
                Integer codesForPrefix = numCodesByPrefix.get(type.getGeneratedCodePrefix());
                if(codesForPrefix == null) {
                    codesForPrefix = 1;
                } else {
                    codesForPrefix++;
                }
                numCodesByPrefix.put(type.getGeneratedCodePrefix(), codesForPrefix);
            }
        }

        // Generate Codes for prefixes (Works in case of codes by prefix and standard)
        Map<String, Deque<String>> codesByPrefix = new HashMap<String, Deque<String>>();
        for (String prefix : numCodesByPrefix.keySet())
        {
            int numOfCodesForPrefix = numCodesByPrefix.get(prefix);
            List<String> newCodes =
                    codeGenerator.generateCodes(prefix, ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE, numOfCodesForPrefix);
            Deque<String> newCodesQueue = new LinkedList<String>();
            newCodesQueue.addAll(newCodes);
            codesByPrefix.put(prefix, newCodesQueue);
        }

        // Create SamplePE objects
        List<SamplePE> samples = new LinkedList<SamplePE>();
        for (SampleCreation creation : creations)
        {
            SamplePE sample = new SamplePE();
            //Create code if is not present
            if(StringUtils.isEmpty(creation.getCode())) {
                SampleTypePE type = (SampleTypePE) types.get(creation.getTypeId());
                creation.setCode(codesByPrefix.get(type.getGeneratedCodePrefix()).removeFirst());
            }
            sample.setCode(creation.getCode());
            String createdPermId = daoFactory.getPermIdDAO().createPermId();
            sample.setPermId(createdPermId);
            sample.setRegistrator(context.getSession().tryGetPerson());
            RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());
            samples.add(sample);
        }

        return samples;
    }

    private void checkData(IOperationContext context, SampleCreation creation, SampleTypePE type)
    {
        if(creation.getTypeId() == null) {
            throw new UserFailureException("Type id cannot be null.");
        } else if(type == null) {
            throw new ObjectNotFoundException(creation.getTypeId());
        } else if (false == StringUtils.isEmpty(creation.getCode()) && (type.isAutoGeneratedCode() || creation.isAutoGeneratedCode())) {
            throw new UserFailureException("Code should be empty when auto generated code is selected.");
        } else if(StringUtils.isEmpty(creation.getCode()) && false == type.isAutoGeneratedCode() && false == creation.isAutoGeneratedCode()) {
            throw new UserFailureException("Code cannot be empty for a non auto generated code.");
        } else {
            SampleIdentifierFactory.assertValidCode(creation.getCode());
        }
    }

    @Override
    protected SamplePermId createPermId(IOperationContext context, SamplePE entity)
    {
        return new SamplePermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, SampleCreation creation)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, SamplePE entity)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(new SampleIdentifier(entity.getIdentifier()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<SamplePE> entities)
    {
        verifySampleExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<SampleCreation, SamplePE> entitiesMap)
    {
        setSampleSpaceExecutor.set(context, entitiesMap);
        setSampleProjectExecutor.set(context, entitiesMap);
        setSampleExperimentExecutor.set(context, entitiesMap);
        setSampleTypeExecutor.set(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<SampleCreation, SamplePE> entry : entitiesMap.entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<SampleCreation, SamplePE> entitiesMap)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<SampleCreation, SamplePE> entry : entitiesMap.entrySet())
        {
            SampleCreation creation = entry.getKey();
            SamplePE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
            tagMap.put(entity, creation.getTagIds());
        }

        createAttachmentExecutor.create(context, attachmentMap);
        addTagToEntityExecutor.add(context, tagMap);
        setSampleRelatedSamplesExecutor.set(context, entitiesMap);
    }

    @Override
    protected List<SamplePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSampleDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SamplePE> entities, boolean clearCache)
    {
        daoFactory.getSampleDAO().createOrUpdateSamples(entities, context.getSession().tryGetPerson(), clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.SAMPLE.getLabel(), EntityKind.SAMPLE);
    }

}
