/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CheckDataProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleCodeGeneratorByType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
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
    private ISampleAuthorizationExecutor authorizationExecutor;

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
    private ISetSampleContainerExecutor setSampleContainerExecutor;

    @Autowired
    private ISetSampleComponentsExecutor setSampleComponentsExecutor;

    @Autowired
    private ISetSampleParentsExecutor setSampleParentsExecutor;

    @Autowired
    private ISetSampleChildrenExecutor setSampleChildrenExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    protected List<SamplePE> createEntities(final IOperationContext context, CollectionBatch<SampleCreation> batch)
    {
        final Map<IEntityTypeId, EntityTypePE> types = getTypes(context, batch);

        checkData(context, batch, types);

        final Map<String, Deque<String>> codesByPrefix = generateCodes(context, batch, types);
        final List<SamplePE> samples = new LinkedList<SamplePE>();
        final PersonPE person = context.getSession().tryGetPerson();
        final Date timeStamp = daoFactory.getTransactionTimestamp();

        new CollectionBatchProcessor<SampleCreation>(context, batch)
            {
                @Override
                public void process(SampleCreation creation)
                {
                    SamplePE sample = new SamplePE();
                    // Create code if is not present
                    if (StringUtils.isEmpty(creation.getCode()))
                    {
                        SampleTypePE type = (SampleTypePE) types.get(creation.getTypeId());
                        creation.setCode(codesByPrefix.get(type.getGeneratedCodePrefix()).removeFirst());
                    }
                    sample.setCode(creation.getCode());
                    String createdPermId = daoFactory.getPermIdDAO().createPermId();
                    sample.setPermId(createdPermId);
                    sample.setRegistrator(person);
                    RelationshipUtils.updateModificationDateAndModifier(sample, person, timeStamp);
                    sample.setMetaData(creation.getMetaData());
                    samples.add(sample);
                }

                @Override
                public IProgress createProgress(SampleCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return samples;
    }

    private Map<IEntityTypeId, EntityTypePE> getTypes(IOperationContext context, CollectionBatch<SampleCreation> batch)
    {
        Collection<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

        for (SampleCreation creation : batch.getObjects())
        {
            typeIds.add(creation.getTypeId());
        }

        return mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, typeIds);
    }

    private void checkData(final IOperationContext context, final CollectionBatch<SampleCreation> batch, final Map<IEntityTypeId, EntityTypePE> types)
    {
        Properties serviceProperties = configurer.getResolvedProps();

        boolean sharedSamplesEnabled = PropertyUtils.getBoolean(serviceProperties, Constants.SHARED_SAMPLES_ENABLED_KEY, true);
        boolean spaceSamplesEnabled = PropertyUtils.getBoolean(serviceProperties, Constants.SPACE_SAMPLES_ENABLED_KEY, true);

        new CollectionBatchProcessor<SampleCreation>(context, batch)
            {
                @Override
                public void process(SampleCreation creation)
                {
                    if (sharedSamplesEnabled == false &&
                            creation.getSpaceId() == null) {
                        throw new UserFailureException("Space id cannot be null, shared samples are disabled.");
                    }

                    if (spaceSamplesEnabled == false &&
                            creation.getSpaceId() != null &&
                            creation.getProjectId() == null &&
                            creation.getExperimentId() == null) {
                        throw new UserFailureException("Both Project id and Experiment id cannot be null, space samples are disabled.");
                    }

                    SampleTypePE type = (SampleTypePE) types.get(creation.getTypeId());

                    if (creation.getTypeId() == null)
                    {
                        throw new UserFailureException("Type id cannot be null.");
                    } else if (type == null)
                    {
                        throw new ObjectNotFoundException(creation.getTypeId());
                    } else if (StringUtils.isEmpty(creation.getCode()) && false == type.isAutoGeneratedCode()
                            && false == creation.isAutoGeneratedCode())
                    {
                        throw new UserFailureException("Code cannot be empty for a non auto generated code.");
                    } else
                    {
                        SampleIdentifierFactory.assertValidCode(creation.getCode());
                    }
                }

                @Override
                public IProgress createProgress(SampleCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CheckDataProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    private Map<String, Deque<String>> generateCodes(IOperationContext context, CollectionBatch<SampleCreation> batch,
            Map<IEntityTypeId, EntityTypePE> types)
    {
        Properties serviceProperties = configurer.getResolvedProps();
        boolean createContinuousSampleCodes = PropertyUtils.getBoolean(serviceProperties, Constants.CREATE_CONTINUOUS_SAMPLES_CODES_KEY, false);
        Map<String, Integer> numCodesByPrefix = new HashMap<String, Integer>();

        // Count how many codes should be generated with each prefix
        for (SampleCreation creation : batch.getObjects())
        {
            SampleTypePE type = (SampleTypePE) types.get(creation.getTypeId());

            if (StringUtils.isEmpty(creation.getCode()) && (type.isAutoGeneratedCode() || creation.isAutoGeneratedCode()))
            {
                Integer codesForPrefix = numCodesByPrefix.get(type.getGeneratedCodePrefix());
                if (codesForPrefix == null)
                {
                    codesForPrefix = 1;
                } else
                {
                    codesForPrefix++;
                }
                numCodesByPrefix.put(type.getGeneratedCodePrefix(), codesForPrefix);
            }
        }

        // Create a code generator
        EntityCodeGenerator codeGenerator;

        if (createContinuousSampleCodes)
        {
            codeGenerator = new SampleCodeGeneratorByType(daoFactory);
        } else
        {
            codeGenerator = new EntityCodeGenerator(daoFactory);
        }

        // Generate Codes for prefixes (Works in case of codes by prefix and standard)
        final Map<String, Deque<String>> codesByPrefix = new HashMap<String, Deque<String>>();

        for (String prefix : numCodesByPrefix.keySet())
        {
            int numOfCodesForPrefix = numCodesByPrefix.get(prefix);
            List<String> newCodes =
                    codeGenerator.generateCodes(prefix, ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE, numOfCodesForPrefix);
            Deque<String> newCodesQueue = new LinkedList<String>();
            newCodesQueue.addAll(newCodes);
            codesByPrefix.put(prefix, newCodesQueue);
        }

        return codesByPrefix;
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
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, SamplePE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SampleCreation, SamplePE> batch)
    {
        setSampleSpaceExecutor.set(context, batch);
        setSampleExperimentExecutor.set(context, batch);
        setSampleProjectExecutor.set(context, batch);
        setSampleTypeExecutor.set(context, batch);
        updateEntityPropertyExecutor.update(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SampleCreation, SamplePE> batch)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<SampleCreation, SamplePE> entry : batch.getObjects().entrySet())
        {
            SampleCreation creation = entry.getKey();
            SamplePE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
            tagMap.put(entity, creation.getTagIds());
        }

        createAttachmentExecutor.create(context, attachmentMap);
        addTagToEntityExecutor.add(context, tagMap);

        setSampleChildrenExecutor.set(context, batch);
        setSampleParentsExecutor.set(context, batch);
        setSampleComponentsExecutor.set(context, batch);
        setSampleContainerExecutor.set(context, batch);
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

    @Override
    protected IObjectId getId(SamplePE entity)
    {
        return new SampleIdentifier(entity.getIdentifier());
    }

}
