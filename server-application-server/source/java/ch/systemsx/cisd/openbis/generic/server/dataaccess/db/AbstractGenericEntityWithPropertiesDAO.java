/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Abstract super class of DAOs for entities that are indexed and contain properties.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityWithPropertiesDAO<T extends IEntityInformationWithPropertiesHolder>
        extends AbstractGenericEntityDAO<T>
{
    protected static final String ATTACHMENT_CONTENT_IDS_PARAM = "attachmentContentIds";

    protected static final String DESCRIPTION_PARAM = "description";

    protected static final String ENTITY_IDS_PARAM = "entityIds";

    protected static final String ENTITY_TYPE_PARAM = "entityType";

    protected static final String EVENT_TYPE_PARAM = "eventType";

    protected static final String IDENTIFIERS_PARAM = "identifiers";

    protected static final String REGISTRATOR_ID_PARAM = "registratorId";

    protected static final String REASON_PARAM = "reason";

    protected static final String CONTENT_PARAM = "content";

    private final PersistencyResources persistencyResources;

    protected AbstractGenericEntityWithPropertiesDAO(
            final PersistencyResources persistencyResources, final Class<T> entityClass,
            final EntityHistoryCreator historyCreator)
    {
        super(persistencyResources.getSessionFactory(), entityClass, historyCreator);
        this.persistencyResources = persistencyResources;
    }

    protected IDynamicPropertyEvaluationScheduler getIndexUpdateScheduler()
    {
        return persistencyResources.getDynamicPropertyEvaluationScheduler();
    }

    protected IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluatorScheduler()
    {
        return persistencyResources.getDynamicPropertyEvaluationScheduler();
    }

    protected DataSource getDataSource()
    {
        return persistencyResources.getContext().getDataSource();
    }

    @Override
    public void delete(T entity) throws DataAccessException
    {
        super.delete(entity);
        scheduleRemoveFromFullTextIndex(Collections.singletonList(entity.getId()));
    }

    protected void scheduleRemoveFromFullTextIndex(List<Long> ids)
    {
        getIndexUpdateScheduler().scheduleUpdate(DynamicPropertyEvaluationOperation.delete(getEntityClass(), ids));
    }

    protected void scheduleDynamicPropertiesEvaluation(List<T> entities)
    {
        scheduleDynamicPropertiesEvaluation(getDynamicPropertyEvaluatorScheduler(),
                getEntityClass(), entities);
    }

    protected void scheduleDynamicPropertiesEvaluationByIds(List<Long> ids)
    {
        scheduleDynamicPropertiesEvaluationForIds(getDynamicPropertyEvaluatorScheduler(),
                getEntityClass(), ids);
    }

    protected void executePermanentDeleteAction(final EntityType entityType,
            final List<TechId> entityTechIds, final PersonPE registrator, final String reason,
            final String sqlSelectPermIds, final String sqlDeleteProperties,
            final String sqlDeleteAttachments, final String sqlDeleteEntities,
            final String sqlInsertEvent, final String sqlSelectPropertyHistory,
            final String sqlSelectRelationshipHistory, final String sqlSelectAttributes,
            List<? extends AttachmentHolderPE> attachmentHolders,
            AttachmentHolderKind attachmentHolderKind)
    {
        List<Long> entityIds = TechId.asLongs(entityTechIds);
        DeletePermanentlyBatchOperation deleteOperation =
                new DeletePermanentlyBatchOperation(entityType, entityIds, registrator, reason,
                        sqlSelectPermIds, sqlDeleteProperties,
                        sqlDeleteAttachments, sqlDeleteEntities,
                        sqlInsertEvent, sqlSelectPropertyHistory, sqlSelectRelationshipHistory,
                        sqlSelectAttributes, attachmentHolders, attachmentHolderKind);
        BatchOperationExecutor.executeInBatches(deleteOperation);

        // FIXME remove this when we remove the switch to disable trash
        scheduleRemoveFromFullTextIndex(entityIds);
    }

    abstract Logger getLogger();

    protected class DeletePermanentlyBatchOperation implements IBatchOperation<Long>
    {

        private final EntityType entityType;

        private final List<Long> allEntityIds;

        private final PersonPE registrator;

        private final String reason;

        private final String sqlSelectPermIds;

        private final String sqlDeleteProperties;

        private final String sqlDeleteAttachments;

        private final String sqlDeleteEntities;

        private final String sqlInsertEvent;

        private final String sqlSelectPropertyHistory;

        private final String sqlSelectRelationshipHistory;

        private final String sqlSelectAttributes;

        private List<? extends AttachmentHolderPE> attachmentHolders;

        private AttachmentHolderKind attachmentHolderKind;

        DeletePermanentlyBatchOperation(EntityType entityType, List<Long> allEntityIds,
                PersonPE registrator, String reason, String sqlSelectPermIds,
                String sqlDeleteProperties, String sqlDeleteAttachments,
                String sqlDeleteEntities, String sqlInsertEvent, String selectPropertyHistory,
                String selectRelationshipHistory, String sqlSelectAttributes,
                List<? extends AttachmentHolderPE> attachmentHolders,
                AttachmentHolderKind attachmentHolderKind)
        {
            this.entityType = entityType;
            this.allEntityIds = allEntityIds;
            this.registrator = registrator;
            this.reason = reason;
            this.sqlSelectPermIds = sqlSelectPermIds;
            this.sqlDeleteProperties = sqlDeleteProperties;
            this.sqlDeleteAttachments = sqlDeleteAttachments;
            this.sqlDeleteEntities = sqlDeleteEntities;
            this.sqlInsertEvent = sqlInsertEvent;
            this.sqlSelectPropertyHistory = selectPropertyHistory;
            this.sqlSelectRelationshipHistory = selectRelationshipHistory;
            this.sqlSelectAttributes = sqlSelectAttributes;
            this.attachmentHolders = attachmentHolders;
            this.attachmentHolderKind = attachmentHolderKind;
        }

        @Override
        public List<Long> getAllEntities()
        {
            return allEntityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityType.name();
        }

        @Override
        public String getOperationName()
        {
            return "permanently deleting";
        }

        @Override
        public void execute(final List<Long> batchEntityIds)
        {
            executeStatelessAction(createPermanentDeleteAction(batchEntityIds));
        }

        private StatelessHibernateCallback createPermanentDeleteAction(
                final List<Long> entityIdsToDelete)
        {
            return new PermanentDeletionAction(entityIdsToDelete);
        }

        private class PermanentDeletionAction implements StatelessHibernateCallback
        {
            private final List<Long> entityIdsToDelete;

            public PermanentDeletionAction(List<Long> entityIdsToDelete)
            {
                this.entityIdsToDelete = entityIdsToDelete;
            }

            @Override
            public Object doInStatelessSession(StatelessSession session)
            {
                final SQLQuery sqlQuerySelectPermIds = session.createSQLQuery(sqlSelectPermIds);
                final SQLQuery sqlQueryDeleteProperties =
                        session.createSQLQuery(sqlDeleteProperties);
                final SQLQuery sqlQueryDeleteEntities = session.createSQLQuery(sqlDeleteEntities);
                final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                final SQLQuery sqlQueryDeleteAttachments =
                        session.createSQLQuery(sqlDeleteAttachments);

                final List<String> permIds =
                        selectPermIds(sqlQuerySelectPermIds, entityIdsToDelete);
                if (permIds.isEmpty())
                {
                    return null;
                }

                String content = historyCreator.apply(session, entityIdsToDelete, sqlSelectPropertyHistory,
                        sqlSelectRelationshipHistory, sqlSelectAttributes, attachmentHolders,
                        attachmentHolderKind, registrator);

                deleteProperties(sqlQueryDeleteProperties, entityIdsToDelete);
                deleteAttachmentsWithContents(sqlQueryDeleteAttachments, entityIdsToDelete);
                deleteMainEntities(sqlQueryDeleteEntities, entityIdsToDelete);
                insertEvent(sqlQueryInsertEvent, permIds, content);
                return null;
            }

            private List<String> selectPermIds(final SQLQuery sqlQuerySelectPermIds,
                    final List<Long> entityIds)
            {
                sqlQuerySelectPermIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                final List<String> permIdsOrNull = cast(sqlQuerySelectPermIds.list());
                return permIdsOrNull == null ? Collections.<String> emptyList() : permIdsOrNull;
            }

            private void deleteProperties(final SQLQuery sqlQueryDeleteProperties,
                    List<Long> entityIds)
            {
                sqlQueryDeleteProperties.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQueryDeleteProperties.executeUpdate();
            }

            private void deleteAttachmentsWithContents(
                    final SQLQuery sqlQueryDeleteAttachments, List<Long> entityIds)
            {
                sqlQueryDeleteAttachments.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQueryDeleteAttachments.executeUpdate();
            }

            private void deleteMainEntities(final SQLQuery sqlQueryDeleteEntities,
                    List<Long> entityIds)
            {
                sqlQueryDeleteEntities.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQueryDeleteEntities.executeUpdate();
            }

            private void insertEvent(final SQLQuery sqlQueryInsertEvent, final List<String> permIds, String content)
            {
                final String description =
                        CollectionUtils.abbreviate(permIds, 3, CollectionStyle.NO_BOUNDARY);
                sqlQueryInsertEvent.setParameter(DESCRIPTION_PARAM, description);
                sqlQueryInsertEvent.setParameter(EVENT_TYPE_PARAM, EventType.DELETION.name());
                sqlQueryInsertEvent.setParameter(REASON_PARAM, reason);
                sqlQueryInsertEvent.setParameter(REGISTRATOR_ID_PARAM, registrator.getId());
                sqlQueryInsertEvent.setParameter(ENTITY_TYPE_PARAM, entityType.name());

                final String allPermIdsAsString =
                        CollectionUtils.abbreviate(permIds, -1, CollectionStyle.NO_BOUNDARY);
                sqlQueryInsertEvent.setParameter(IDENTIFIERS_PARAM, allPermIdsAsString);
                sqlQueryInsertEvent.setParameter(CONTENT_PARAM, content);
                sqlQueryInsertEvent.executeUpdate();
            }
        }

    }

    protected static class SQLBuilder
    {

        protected static String createSelectPermIdsSQL(final String entitiesTable)
        {
            return "SELECT perm_id FROM " + entitiesTable + " WHERE id " + inEntityIds();
        }

        protected static String createDeletePropertiesSQL(final String propertiesTable,
                String ownerColumnName)
        {
            return "DELETE FROM " + propertiesTable + " WHERE " + ownerColumnName + " "
                    + inEntityIds();
        }

        protected static String createDeleteAttachmentsSQL(final String ownerColumnName)
        {
            return "DELETE FROM " + TableNames.ATTACHMENTS_TABLE + " WHERE " + ownerColumnName
                    + " " + inEntityIds();
        }

        protected static String createDeleteEnitiesSQL(final String entitiesTable)
        {
            return "DELETE FROM " + entitiesTable + " WHERE id " + inEntityIds();
        }

        protected static String createInsertEventSQL()
        {
            return String
                    .format("INSERT INTO %s (id, event_type, description, reason, pers_id_registerer, entity_type, identifiers, content) "
                            + "VALUES (nextval('%s'), :%s, :%s, :%s, :%s, :%s, :%s, :%s)",
                            TableNames.EVENTS_TABLE, SequenceNames.EVENT_SEQUENCE,
                            EVENT_TYPE_PARAM, DESCRIPTION_PARAM, REASON_PARAM,
                            REGISTRATOR_ID_PARAM, ENTITY_TYPE_PARAM, IDENTIFIERS_PARAM, CONTENT_PARAM);
        }

        protected static String inEntityIds()
        {
            return in(ENTITY_IDS_PARAM);
        }

        protected static String in(String paramName)
        {
            return "IN (:" + paramName + ")";
        }

    }
}
