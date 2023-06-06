/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateDataSetExecutor extends AbstractCreateEntityExecutor<DataSetCreation, DataPE, DataSetPermId> implements ICreateDataSetExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDataSetAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private ISetDataSetPhysicalDataExecutor setDataSetPhysicalDataExecutor;

    @Autowired
    private ISetDataSetLinkedDataExecutor setDataSetLinkedDataExecutor;

    @Autowired
    private ISetDataSetDataStoreExecutor setDataSetDataStoreExecutor;

    @Autowired
    private ISetDataSetExperimentExecutor setDataSetExperimentExecutor;

    @Autowired
    private ISetDataSetSampleExecutor setDataSetSampleExecutor;

    @Autowired
    private ISetDataSetContainerExecutor setDataSetContainerExecutor;

    @Autowired
    private ISetDataSetComponentsExecutor setDataSetComponentsExecutor;

    @Autowired
    private ISetDataSetParentsExecutor setDataSetParentsExecutor;

    @Autowired
    private ISetDataSetChildrenExecutor setDataSetChildrenExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Override
    protected List<DataPE> createEntities(final IOperationContext context, CollectionBatch<DataSetCreation> batch)
    {
        final Map<IEntityTypeId, EntityTypePE> types = getTypes(context, batch);

        checkData(context, batch, types);

        final IPermIdDAO codeGenerator = daoFactory.getPermIdDAO();
        final List<DataPE> dataSets = new LinkedList<DataPE>();

        new CollectionBatchProcessor<DataSetCreation>(context, batch)
            {
                @Override
                public void process(DataSetCreation creation)
                {
                    DataSetTypePE type = (DataSetTypePE) types.get(creation.getTypeId());

                    // Create code if is not present
                    if (StringUtils.isEmpty(creation.getCode()))
                    {
                        creation.setCode(codeGenerator.createPermId());
                    }

                    DataSetKind kind = determineDataSetKind(creation);
                    DataPE dataSet = null;

                    if (DataSetKind.PHYSICAL.equals(kind))
                    {
                        dataSet = new ExternalDataPE();
                    } else if (DataSetKind.CONTAINER.equals(kind))
                    {
                        dataSet = new DataPE();
                    } else if (DataSetKind.LINK.equals(kind))
                    {
                        dataSet = new LinkDataPE();
                    }

                    dataSet.setCode(creation.getCode());
                    dataSet.setDataSetKind(kind.name());
                    dataSet.setDataSetType(type);
                    dataSet.setDerived(false == creation.isMeasured());
                    dataSet.setDataProducerCode(creation.getDataProducer());
                    dataSet.setProductionDate(creation.getDataProductionDate());
                    dataSet.setMetaData(creation.getMetaData());

                    PersonPE person = context.getSession().tryGetPerson();
                    dataSet.setRegistrator(person);
                    Date timeStamp = daoFactory.getTransactionTimestamp();
                    RelationshipUtils.updateModificationDateAndModifier(dataSet, person, timeStamp);

                    dataSets.add(dataSet);
                }

                /**
                 * Historically, the dataset kind was part of the data set type. Old clients might still not set the kind.
                 * In that case we make a guess. If linkedData is set, we assume LINK. Otherwise we assume PHYSICAL since it is the 
                 * most likely option.
                 */
                private DataSetKind determineDataSetKind(DataSetCreation creation)
				{
                	if (creation.getDataSetKind() != null)
                	{
                		return creation.getDataSetKind();
                	} else if (creation.getLinkedData() != null)
                	{
                		return DataSetKind.LINK;
                	} else
                	{
                		return DataSetKind.PHYSICAL;
                	}
				}

				@Override
                public IProgress createProgress(DataSetCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return dataSets;
    }

    private Map<IEntityTypeId, EntityTypePE> getTypes(IOperationContext context, CollectionBatch<DataSetCreation> batch)
    {
        Collection<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

        for (DataSetCreation creation : batch.getObjects())
        {
            typeIds.add(creation.getTypeId());
        }

        return mapEntityTypeByIdExecutor.map(context, EntityKind.DATA_SET, typeIds);
    }

    private void checkData(final IOperationContext context, final CollectionBatch<DataSetCreation> batch,
            final Map<IEntityTypeId, EntityTypePE> types)
    {
        new CollectionBatchProcessor<DataSetCreation>(context, batch)
            {
                @Override
                public void process(DataSetCreation creation)
                {
                    EntityTypePE type = types.get(creation.getTypeId());

                    if (type == null)
                    {
                        throw new ObjectNotFoundException(creation.getTypeId());
                    } else if (StringUtils.isEmpty(creation.getCode()) && false == creation.isAutoGeneratedCode())
                    {
                        throw new UserFailureException("Code cannot be empty for a non auto generated code.");
                    } else if (false == StringUtils.isEmpty(creation.getCode()) && creation.isAutoGeneratedCode())
                    {
                        throw new UserFailureException("Code should be empty when auto generated code is selected.");
                    }
                }

                @Override
                public IProgress createProgress(DataSetCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CheckDataProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void checkData(IOperationContext context, DataSetCreation creation)
    {
        if (creation.getTypeId() == null)
        {
            throw new UserFailureException("Type id cannot be null.");
        }
        if (creation.getExperimentId() == null && creation.getSampleId() == null)
        {
            throw new UserFailureException("Experiment id and sample id cannot be both null.");
        }
    }

    @Override
    protected DataSetPermId createPermId(IOperationContext context, DataPE entity)
    {
        return new DataSetPermId(entity.getPermId());
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, DataPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<DataSetCreation, DataPE> batch)
    {
        setDataSetPhysicalDataExecutor.set(context, batch);
        setDataSetLinkedDataExecutor.set(context, batch);
        setDataSetDataStoreExecutor.set(context, batch);
        setDataSetSampleExecutor.set(context, batch);
        setDataSetExperimentExecutor.set(context, batch);
        updateEntityPropertyExecutor.update(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<DataSetCreation, DataPE> batch)
    {
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<DataSetCreation, DataPE> entry : batch.getObjects().entrySet())
        {
            DataSetCreation creation = entry.getKey();
            DataPE entity = entry.getValue();
            tagMap.put(entity, creation.getTagIds());
        }

        addTagToEntityExecutor.add(context, tagMap);

        setDataSetChildrenExecutor.set(context, batch);
        setDataSetParentsExecutor.set(context, batch);
        setDataSetComponentsExecutor.set(context, batch);
        setDataSetContainerExecutor.set(context, batch);
    }

    @Override
    protected List<DataPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getDataDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<DataPE> entities, boolean clearCache)
    {
        daoFactory.getDataDAO().createDataSets(entities, context.getSession().tryGetPerson());
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.DATA_SET.getLabel(), EntityKind.DATA_SET);
    }

    @Override
    protected IObjectId getId(DataPE entity)
    {
        return new DataSetPermId(entity.getPermId());
    }

}
