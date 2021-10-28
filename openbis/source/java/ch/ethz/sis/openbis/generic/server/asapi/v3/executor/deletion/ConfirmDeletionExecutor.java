/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.business.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletedDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class ConfirmDeletionExecutor implements IConfirmDeletionExecutor
{
    private static final Comparator<IDeletionId> DELETION_ID_COMPARATOR = new Comparator<IDeletionId>()
        {
            @Override
            public int compare(IDeletionId o1, IDeletionId o2)
            {
                long id1 = ((DeletionTechId) o1).getTechId();
                long id2 = ((DeletionTechId) o2).getTechId();
                return id1 < id2 ? -1 : (id1 > id2 ? 1 : 0);
            }
        };

    @Autowired
    private IMapDeletionByIdExecutor mapDeletionByIdExecutor;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDeletionAuthorizationExecutor authorizationExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public void confirm(IOperationContext context, List<? extends IDeletionId> deletionIds, boolean forceDeletion, 
            boolean forceDeletionOfDependentDeletions)
    {
        if (context == null)
        {
            throw new UserFailureException("Context cannot be null.");
        }
        if (deletionIds == null)
        {
            throw new UserFailureException("Deletion ids cannot be null.");
        }

        // We do not want to fail with nulls but rather ignore them. Ignoring the nulls allows us to
        // pass the result of the deleteXXX method directly to confirmDeletions without any checks
        // (the deleteXXX methods return null when an object to be deleted does not exist, e.g. it had been already deleted)

        List<IDeletionId> deletionIdsWithoutNulls = new ArrayList<IDeletionId>();

        for (IDeletionId deletionId : deletionIds)
        {
            if (deletionId != null)
            {
                deletionIdsWithoutNulls.add(deletionId);
            }
        }

        try
        {
            if (forceDeletion)
            {
                authorizationExecutor.canConfirmForced(context, deletionIdsWithoutNulls);
            } else
            {
                authorizationExecutor.canConfirm(context, deletionIdsWithoutNulls);
            }
        } catch (AuthorizationFailureException ex)
        {
            throw new UnauthorizedObjectAccessException(deletionIdsWithoutNulls);
        }
        List<TechId> dependentDeletions = getDependentDelitions(deletionIdsWithoutNulls);
        if (dependentDeletions.isEmpty() == false)
        {
            if (forceDeletionOfDependentDeletions)
            {
                deletionIdsWithoutNulls.addAll(TechId.asLongs(dependentDeletions).stream()
                        .map(DeletionTechId::new).collect(Collectors.toList()));
            } else
            {
                throw DeletionUtils.createException(context.getSession(), businessObjectFactory,
                        TechId.asLongs(dependentDeletions));
            }
        }

        Collections.sort(deletionIdsWithoutNulls, DELETION_ID_COMPARATOR);
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        Map<IDeletionId, DeletionPE> deletionMap = mapDeletionByIdExecutor.map(context, deletionIdsWithoutNulls);

        List<Long> deletionTechIds = new LinkedList<Long>();
        for (DeletionPE deletion : deletionMap.values())
        {
            deletionTechIds.add(deletion.getId());
        }

        IDeletionTable table = businessObjectFactory.createDeletionTable(context.getSession());
        table.load(deletionTechIds, true);
        List<Deletion> deletions = table.getDeletions();

        DeletionValidator validator = new DeletionValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        for (Deletion deletion : deletions)
        {
            if (false == validator.doValidation(context.getSession().tryGetPerson(), deletion))
            {
                throw new UnauthorizedObjectAccessException(new DeletionTechId(deletion.getId()));
            }
        }

        // NOTE: we can't do bulk deletions to preserve original reasons
        for (IDeletionId deletionId : deletionIdsWithoutNulls)
        {
            DeletionPE deletion = deletionMap.get(deletionId);

            if (deletion == null)
            {
                throw new ObjectNotFoundException(deletionId);
            }

            deleteDataSets(context, deletion, forceDeletion);
            deleteSamples(context, deletion);
            deleteExperiments(context, deletion);

            // WORKAROUND to get the fresh deletion and fix org.hibernate.NonUniqueObjectException
            DeletionPE freshDeletion = deletionDAO.getByTechId(TechId.create(deletion));
            deletionDAO.delete(freshDeletion);
        }

    }

    private List<TechId> getDependentDelitions(List<? extends IDeletionId> deletionIds)
    {
        List<TechId> techIds = new ArrayList<>();
        for (IDeletionId deletionId : deletionIds)
        {
            if (deletionId instanceof DeletionTechId)
            {
                techIds.add(new TechId(((DeletionTechId) deletionId).getTechId()));
            } else
            {
                throw new UserFailureException("Unsupported type of deletion id: " + deletionId.getClass());
            }
        }
        
        return daoFactory.getDeletionDAO().listAllDependentDeletions(techIds);
    }

    private void deleteDataSets(IOperationContext context, DeletionPE deletion, boolean forceDeletion)
    {
        try
        {
            IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

            List<TechId> deletionTechIds = Collections.singletonList(new TechId(deletion.getId()));
            List<String> dataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionTechIds);

            IDeletedDataSetTable deletedDataSetTable =
                    businessObjectFactory.createDeletedDataSetTable(context.getSession());
            deletedDataSetTable.loadByDataSetCodes(dataSetCodes);
            deletedDataSetTable.permanentlyDeleteLoadedDataSets(deletion.getReason(), forceDeletion);
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "data set", EntityKind.DATA_SET);
            return;
        }
    }

    private void deleteSamples(IOperationContext context, DeletionPE deletion)
    {
        try
        {
            ISampleDAO sampleDAO = daoFactory.getSampleDAO();
            sampleDAO.deletePermanently(deletion, context.getSession().tryGetPerson());
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "sample", EntityKind.SAMPLE);
            return;
        }
    }

    private void deleteExperiments(IOperationContext context, DeletionPE deletion)
    {
        try
        {
            IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
            IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(context.getSession());

            List<TechId> deletionTechIds = Collections.singletonList(new TechId(deletion.getId()));
            List<TechId> experimentTechIds = deletionDAO.findTrashedExperimentIds(deletionTechIds);

            experimentBO.deleteByTechIds(experimentTechIds, deletion.getReason());
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "experiment", EntityKind.EXPERIMENT);
            return;
        }
    }
}
