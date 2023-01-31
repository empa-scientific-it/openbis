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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.IncomingEntity;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Franz-Josef Elmer
 */
public class AttachmentsSynchronizer implements ITaskExecutor<List<IncomingEntity<?>>>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AttachmentsSynchronizer.class);

    private Map<SyncEntityKind, AbstractHandler> handlersByEntityKind;

    public AttachmentsSynchronizer(IApplicationServerApi v3api, String sessionToken,
            IApplicationServerApi v3apiDataSource, String sessionTokenDataSource, Date lastSyncTimestamp,
            AttachmentSynchronizationSummary synchronizationSummary, boolean dryRun, Monitor monitor)
    {
        handlersByEntityKind = new HashMap<>();
        handlersByEntityKind.put(SyncEntityKind.PROJECT, new ProjectsHandler(dryRun));
        handlersByEntityKind.put(SyncEntityKind.EXPERIMENT, new ExperimentsHandler(dryRun));
        handlersByEntityKind.put(SyncEntityKind.SAMPLE, new SamplesHandler(dryRun));
        Collection<AbstractHandler> values = handlersByEntityKind.values();
        for (AbstractHandler handler : values)
        {
            handler.setV3api(v3api);
            handler.setSessionToken(sessionToken);
            handler.setV3apiDataSource(v3apiDataSource);
            handler.setSessionTokenDataSource(sessionTokenDataSource);
            handler.setLastSyncTimestamp(lastSyncTimestamp);
            handler.setSynchronizationSummary(synchronizationSummary);
            handler.setMonitor(monitor);
        }
    }

    @Override
    public Status execute(List<IncomingEntity<?>> entities)
    {
        try
        {
            Map<SyncEntityKind, List<IncomingEntity<?>>> entitiesByEntityKind = segregateEntitiesByEntityKind(entities);
            Set<Entry<SyncEntityKind, List<IncomingEntity<?>>>> entrySet = entitiesByEntityKind.entrySet();
            ;
            for (Entry<SyncEntityKind, List<IncomingEntity<?>>> entry : entrySet)
            {
                handlersByEntityKind.get(entry.getKey()).handle(entry.getValue());
            }
        } catch (Exception e)
        {
            operationLog.error("Attachment synchronization failed", e);
            return Status.createError("Attachment synchronization failed: " + e.getMessage());
        }
        return Status.OK;
    }

    private Map<SyncEntityKind, List<IncomingEntity<?>>> segregateEntitiesByEntityKind(List<IncomingEntity<?>> entities)
    {
        Map<SyncEntityKind, List<IncomingEntity<?>>> map = new HashMap<>();
        for (IncomingEntity<?> entity : entities)
        {
            List<IncomingEntity<?>> list = map.get(entity.getEntityKind());
            if (list == null)
            {
                list = new ArrayList<>();
                map.put(entity.getEntityKind(), list);
            }
            list.add(entity);
        }
        return map;
    }

    private static abstract class AbstractHandler
    {
        protected IApplicationServerApi v3api;

        protected String sessionToken;

        protected IApplicationServerApi v3apiDataSource;

        protected String sessionTokenDataSource;

        protected Date lastSyncTimestamp;

        protected AttachmentSynchronizationSummary synchronizationSummary;

        protected Monitor monitor;

        protected SyncEntityKind entityKind;

        protected boolean dryRun;

        AbstractHandler(boolean dryRun, SyncEntityKind entityKind)
        {
            this.dryRun = dryRun;
            this.entityKind = entityKind;
        }

        public void setV3api(IApplicationServerApi v3api)
        {
            this.v3api = v3api;
        }

        public void setSessionToken(String sessionToken)
        {
            this.sessionToken = sessionToken;
        }

        public void setV3apiDataSource(IApplicationServerApi v3apiDataSource)
        {
            this.v3apiDataSource = v3apiDataSource;
        }

        public void setSessionTokenDataSource(String sessionTokenDataSource)
        {
            this.sessionTokenDataSource = sessionTokenDataSource;
        }

        public void setLastSyncTimestamp(Date lastSyncTimestamp)
        {
            this.lastSyncTimestamp = lastSyncTimestamp;
        }

        public void setSynchronizationSummary(AttachmentSynchronizationSummary synchronizationSummary)
        {
            this.synchronizationSummary = synchronizationSummary;
        }

        public void setMonitor(Monitor monitor)
        {
            this.monitor = monitor;
        }

        private void log(List<IncomingEntity<?>> entities, int numberOfEntitiesWithAttachments, int totalNumberOfEntities, String description)
        {
            List<String> ids = entities.stream().map(IncomingEntity::getIdentifier).collect(Collectors.toList());
            String idsAsString = CollectionUtils.abbreviate(ids, 20);
            monitor.log(String.format("%4d (of %4d) %ss %s. %s",
                    numberOfEntitiesWithAttachments, totalNumberOfEntities, entityKind, description, idsAsString));
        }

        public <AH extends IPermIdHolder & IAttachmentsHolder> void handle(List<IncomingEntity<?>> entities)
        {
            List<IncomingEntity<?>> filteredEntities = entities.stream().filter(e -> e.hasAttachments()).collect(Collectors.toList());
            if (filteredEntities.isEmpty())
            {
                return;
            }
            log(filteredEntities, filteredEntities.size(), entities.size(), "with attachments on data source");
            Map<String, Map<String, Attachment>> existingAttachments = retrievAttachments(v3api, sessionToken, entities);
            log(entities, existingAttachments.size(), entities.size(), "on harvester");
            Map<String, Map<String, Attachment>> attachmentsFromDataSource =
                    retrievAttachments(v3apiDataSource, sessionTokenDataSource, filteredEntities);
            List<AttachmentChange> attachmentChanges = new ArrayList<>();
            for (Entry<String, Map<String, Attachment>> entry : attachmentsFromDataSource.entrySet())
            {
                String permId = entry.getKey();
                Map<String, Attachment> existingAttachmentsByFile = existingAttachments.get(permId);
                if (existingAttachmentsByFile == null)
                {
                    throw new RuntimeException("Severe error: Entity with permId " + permId
                            + " should exist because it has been just registered on the harvester openBIS instance.");
                }
                Map<String, Attachment> attachmentsFromDataSourceByFile = entry.getValue();
                Collection<Attachment> values = attachmentsFromDataSourceByFile.values();
                for (Attachment attachmentFromDataSource : values)
                {
                    String fileName = attachmentFromDataSource.getFileName();
                    Attachment existingAttachment = existingAttachmentsByFile.get(fileName);
                    int version = existingAttachment == null ? 0 : existingAttachment.getVersion();
                    if (attachmentFromDataSource.getVersion() < version)
                    {
                        attachmentChanges.add(new AttachmentChange(attachmentFromDataSource, permId, true, 0));
                        synchronizationSummary.updatedCount.getAndIncrement();
                    } else if (attachmentFromDataSource.getVersion() == version)
                    {
                        if (attachmentFromDataSource.getRegistrationDate().after(lastSyncTimestamp) ||
                                equals(attachmentFromDataSource.getTitle(), existingAttachment.getTitle()) == false
                                || equals(attachmentFromDataSource.getDescription(), existingAttachment.getDescription()) == false)
                        {
                            attachmentChanges.add(new AttachmentChange(attachmentFromDataSource, permId, true, 0));
                            synchronizationSummary.updatedCount.getAndIncrement();
                        }
                    } else
                    {
                        attachmentChanges.add(new AttachmentChange(attachmentFromDataSource, permId, false, version));
                        synchronizationSummary.addedCount.getAndIncrement();
                    }
                }
                for (Attachment existingAttachment : existingAttachmentsByFile.values())
                {
                    if (attachmentsFromDataSourceByFile.get(existingAttachment.getFileName()) == null)
                    {
                        attachmentChanges.add(new AttachmentChange(existingAttachment, permId, true, null));
                        synchronizationSummary.deletedCount.getAndIncrement();
                    }
                }
            }
            handleAttachmentChanges(attachmentChanges);
        }

        private <AH extends IPermIdHolder & IAttachmentsHolder> Map<String, Map<String, Attachment>> retrievAttachments(
                IApplicationServerApi v3api, String sessionToken, List<IncomingEntity<?>> entities)
        {
            List<String> ids = entities.stream().map(IncomingEntity::getPermID).collect(Collectors.toList());
            Map<String, Map<String, Attachment>> attachmentsByPermId = new HashMap<>();
            Collection<AH> attachmentHolders = getAttachments(v3api, sessionToken, ids);
            for (AH attachmentHolder : attachmentHolders)
            {
                attachmentsByPermId.put(attachmentHolder.getPermId().toString(),
                        attachmentHolder.getAttachments().stream()
                                .collect(Collectors.toMap(Attachment::getFileName, Function.identity())));
            }
            return attachmentsByPermId;
        }

        protected abstract <AH extends IPermIdHolder & IAttachmentsHolder> Collection<AH> getAttachments(
                IApplicationServerApi v3api, String sessionToken, List<String> permIds);

        private void handleAttachmentChanges(List<AttachmentChange> attachmentChanges)
        {
            Map<String, AttachmentListUpdateValue> attachmentUpdatesByPermId = new HashMap<>();
            for (AttachmentChange attachmentChange : attachmentChanges)
            {
                String permId = attachmentChange.getPermId();
                AttachmentListUpdateValue attachmentListUpdate = attachmentUpdatesByPermId.get(permId);
                if (attachmentListUpdate == null)
                {
                    attachmentListUpdate = new AttachmentListUpdateValue();
                    attachmentUpdatesByPermId.put(permId, attachmentListUpdate);
                }
                Attachment attachment = attachmentChange.getAttachment();
                if (attachmentChange.isRemove())
                {
                    attachmentListUpdate.remove(new AttachmentFileName(attachment.getFileName()));
                }
                Integer version = attachmentChange.getVersion();
                if (version != null)
                {
                    addAttachments(attachmentListUpdate, attachment, version);
                }
            }
            handleAttachments(attachmentUpdatesByPermId);
        }

        private void addAttachments(AttachmentListUpdateValue attachmentListUpdate, Attachment attachment, int fromVersion)
        {
            List<AttachmentCreation> attachmentCreations = new ArrayList<>();
            for (int i = attachment.getVersion(); i > fromVersion; i--)
            {
                AttachmentCreation attachmentCreation = new AttachmentCreation();
                attachmentCreation.setFileName(attachment.getFileName());
                attachmentCreation.setTitle(attachment.getTitle());
                attachmentCreation.setDescription(attachment.getDescription());
                attachmentCreation.setContent(attachment.getContent());
                attachmentCreations.add(attachmentCreation);
                if (attachment.getFetchOptions().hasPreviousVersion())
                {
                    attachment = attachment.getPreviousVersion();
                }
            }
            Collections.reverse(attachmentCreations);
            attachmentListUpdate.add(attachmentCreations.toArray(new AttachmentCreation[0]));
        }

        protected abstract void handleAttachments(Map<String, AttachmentListUpdateValue> attachmentsByPermId);

        protected void specifiyAttachmentFetchOptions(AttachmentFetchOptions attachmentFetchOptions)
        {
            attachmentFetchOptions.withContent();
            attachmentFetchOptions.withPreviousVersion().withPreviousVersionUsing(attachmentFetchOptions);
            attachmentFetchOptions.withPreviousVersion().withContentUsing(attachmentFetchOptions.withContent());
        }

        private boolean equals(Object o1, Object o2)
        {
            return o1 == null ? o1 == o2 : o1.equals(o2);
        }
    }

    private static final class ProjectsHandler extends AbstractHandler
    {
        public ProjectsHandler(boolean dryRun)
        {
            super(dryRun, SyncEntityKind.PROJECT);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <AH extends IPermIdHolder & IAttachmentsHolder> Collection<AH> getAttachments(
                IApplicationServerApi v3api, String sessionToken, List<String> permIds)
        {
            List<ProjectPermId> ids = permIds.stream().map(ProjectPermId::new).collect(Collectors.toList());
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            specifiyAttachmentFetchOptions(fetchOptions.withAttachments());
            return (Collection<AH>) v3api.getProjects(sessionToken, ids, fetchOptions).values();
        }

        @Override
        protected void handleAttachments(Map<String, AttachmentListUpdateValue> attachmentsByPermId)
        {
            List<ProjectUpdate> updates = new ArrayList<>();
            for (Entry<String, AttachmentListUpdateValue> entry : attachmentsByPermId.entrySet())
            {
                ProjectUpdate projectUpdate = new ProjectUpdate();
                projectUpdate.setProjectId(new ProjectPermId(entry.getKey()));
                projectUpdate.getAttachments().setActions(entry.getValue().getActions());
                updates.add(projectUpdate);
            }
            if (dryRun)
            {
                return;
            }
            v3api.updateProjects(sessionToken, updates);
        }
    }

    private static final class ExperimentsHandler extends AbstractHandler
    {
        public ExperimentsHandler(boolean dryRun)
        {
            super(dryRun, SyncEntityKind.EXPERIMENT);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <AH extends IPermIdHolder & IAttachmentsHolder> Collection<AH> getAttachments(
                IApplicationServerApi v3api, String sessionToken, List<String> permIds)
        {
            List<ExperimentPermId> ids = permIds.stream().map(ExperimentPermId::new).collect(Collectors.toList());
            ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
            specifiyAttachmentFetchOptions(fetchOptions.withAttachments());
            return (Collection<AH>) v3api.getExperiments(sessionToken, ids, fetchOptions).values();
        }

        @Override
        protected void handleAttachments(Map<String, AttachmentListUpdateValue> attachmentsByPermId)
        {
            List<ExperimentUpdate> updates = new ArrayList<>();
            for (Entry<String, AttachmentListUpdateValue> entry : attachmentsByPermId.entrySet())
            {
                ExperimentUpdate experimentUpdate = new ExperimentUpdate();
                experimentUpdate.setExperimentId(new ExperimentPermId(entry.getKey()));
                experimentUpdate.getAttachments().setActions(entry.getValue().getActions());
                updates.add(experimentUpdate);
            }
            if (dryRun)
            {
                return;
            }
            v3api.updateExperiments(sessionToken, updates);
        }
    }

    private static final class SamplesHandler extends AbstractHandler
    {
        public SamplesHandler(boolean dryRun)
        {
            super(dryRun, SyncEntityKind.SAMPLE);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <AH extends IPermIdHolder & IAttachmentsHolder> Collection<AH> getAttachments(
                IApplicationServerApi v3api, String sessionToken, List<String> permIds)
        {
            List<SamplePermId> ids = permIds.stream().map(SamplePermId::new).collect(Collectors.toList());
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            specifiyAttachmentFetchOptions(fetchOptions.withAttachments());
            return (Collection<AH>) v3api.getSamples(sessionToken, ids, fetchOptions).values();
        }

        @Override
        protected void handleAttachments(Map<String, AttachmentListUpdateValue> attachmentsByPermId)
        {
            List<SampleUpdate> updates = new ArrayList<>();
            for (Entry<String, AttachmentListUpdateValue> entry : attachmentsByPermId.entrySet())
            {
                SampleUpdate sampleUpdate = new SampleUpdate();
                sampleUpdate.setSampleId(new SamplePermId(entry.getKey()));
                sampleUpdate.getAttachments().setActions(entry.getValue().getActions());
                updates.add(sampleUpdate);
            }
            if (dryRun)
            {
                return;
            }
            v3api.updateSamples(sessionToken, updates);
        }
    }

    private static final class AttachmentChange
    {
        private Attachment attachment;

        private String permId;

        private boolean remove;

        private Integer version;

        AttachmentChange(Attachment attachment, String permId, boolean remove, Integer version)
        {
            this.attachment = attachment;
            this.permId = permId;
            this.remove = remove;
            this.version = version;
        }

        public Attachment getAttachment()
        {
            return attachment;
        }

        public String getPermId()
        {
            return permId;
        }

        public boolean isRemove()
        {
            return remove;
        }

        public Integer getVersion()
        {
            return version;
        }
    }

}
