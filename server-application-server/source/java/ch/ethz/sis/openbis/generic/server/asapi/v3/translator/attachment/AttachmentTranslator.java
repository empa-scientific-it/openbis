/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.attachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class AttachmentTranslator extends AbstractCachingTranslator<Long, Attachment, AttachmentFetchOptions>
        implements IAttachmentTranslator
{
    private static Comparator<AttachmentBaseRecord> VERSION_COMPARATOR = new Comparator<AttachmentBaseRecord>()
        {
            @Override
            public int compare(AttachmentBaseRecord r1, AttachmentBaseRecord r2)
            {
                return r2.version - r1.version;
            }
        };

    @Autowired
    private IAttachmentBaseTranslator baseTranslator;

    @Autowired
    private IAttachmentRegistratorTranslator registratorTranslator;

    @Autowired
    private IAttachmentContentTranslator contentTranslator;

    @Override
    protected Attachment createObject(TranslationContext context, Long input, AttachmentFetchOptions fetchOptions)
    {
        Attachment result = new Attachment();
        result.setFetchOptions(new AttachmentFetchOptions());
        return result;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> attachmentIds, AttachmentFetchOptions fetchOptions)
    {
        Map<Long, ObjectHolder<AttachmentBaseRecord>> records = baseTranslator.translate(context, attachmentIds, null);
        List<List<AttachmentBaseRecord>> groupedRecords = groupAndSortByVersion(records.values());
        AttachmentsBuildingHelper buildingHelper = new AttachmentsBuildingHelper();
        for (List<AttachmentBaseRecord> recordsSortecByVersion : groupedRecords)
        {
            buildingHelper.handleAttachmentsOfDifferentVersions(recordsSortecByVersion, fetchOptions);
        }
        buildingHelper.translateRelatedObjects(context);
        return buildingHelper;
    }

    private List<List<AttachmentBaseRecord>> groupAndSortByVersion(Collection<ObjectHolder<AttachmentBaseRecord>> records)
    {
        Map<String, List<AttachmentBaseRecord>> mapByFileName = new HashMap<>();
        for (ObjectHolder<AttachmentBaseRecord> recordHolder : records)
        {
            AttachmentBaseRecord record = recordHolder.getObject();
            String key = createKey(record);
            List<AttachmentBaseRecord> list = mapByFileName.get(key);
            if (list == null)
            {
                list = new ArrayList<>();
                mapByFileName.put(key, list);
            }
            list.add(record);
        }
        Collection<List<AttachmentBaseRecord>> lists = mapByFileName.values();
        for (List<AttachmentBaseRecord> list : lists)
        {
            Collections.sort(list, VERSION_COMPARATOR);
        }
        return new ArrayList<>(lists);
    }

    private String createKey(AttachmentBaseRecord record)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("p:").append(record.projectCode).append(record.spaceCode);
        builder.append("s:").append(record.samplePermId);
        builder.append("e:").append(record.experimentPermId);
        builder.append("f:").append(record.fileName);
        return builder.toString();
    }

    @Override
    protected void updateObject(TranslationContext context, Long attachmentId, Attachment result,
            Object objectRelations, AttachmentFetchOptions fetchOptions)
    {
        AttachmentsBuildingHelper buildingHelper = (AttachmentsBuildingHelper) objectRelations;
        AttachmentBaseRecord baseRecord = buildingHelper.getBaseRecord(attachmentId);
        if (baseRecord == null)
        {
            return;
        }
        result.setFileName(baseRecord.fileName);
        result.setTitle(baseRecord.title);
        result.setDescription(baseRecord.description);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setVersion(baseRecord.version);
        String baseIndexURL = context.getSession().getBaseIndexURL();
        result.setPermlink(createPermlink(baseRecord, baseIndexURL, false));
        result.setLatestVersionPermlink(createPermlink(baseRecord, baseIndexURL, true));
        buildingHelper.addRegistratorAndContent(result, attachmentId);
    }

    private String createPermlink(AttachmentBaseRecord baseRecord, String baseIndexURL,
            boolean latestVersionPermlink)
    {
        String fileName = baseRecord.fileName;
        Integer version = latestVersionPermlink ? null : baseRecord.version;
        if (baseRecord.projectCode != null)
        {
            return PermlinkUtilities.createProjectAttachmentPermlinkURL(baseIndexURL, fileName, version,
                    baseRecord.projectCode, baseRecord.spaceCode);
        }
        if (baseRecord.samplePermId != null)
        {
            return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName, version,
                    AttachmentHolderKind.SAMPLE, baseRecord.samplePermId);
        }
        return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName, version,
                AttachmentHolderKind.EXPERIMENT, baseRecord.experimentPermId);
    }

    private final class AttachmentsBuildingHelper
    {
        private Map<Long, AttachmentBaseRecord> records = new HashMap<>();

        private Map<PersonFetchOptions, List<Long>> attachmentsWithRegistrator = new HashMap<>();

        private Set<Long> attachmentsWithContent = new HashSet<>();

        private Map<Long, AttachmentFetchOptions> fetchOptionsByAttachmentId = new HashMap<>();

        private Map<Long, ObjectHolder<Person>> registratorsByAttachmentId = new HashMap<>();

        private Map<Long, ObjectHolder<byte[]>> contentsByAttachmentId;

        private Map<Long, Long> nextVersionIdByAttachmentId = new HashMap<>();

        private Map<Long, Attachment> attachments = new HashMap<>();

        AttachmentBaseRecord getBaseRecord(Long attachmentId)
        {
            return records.get(attachmentId);
        }

        private <T> T getObject(Map<Long, ObjectHolder<T>> objectsById, Long attachmentId)
        {
            ObjectHolder<T> objectHolder = objectsById.get(attachmentId);
            return objectHolder == null ? null : objectHolder.getObject();
        }

        void addRegistratorAndContent(Attachment attachment, Long attachmentId)
        {
            attachments.put(attachmentId, attachment);
            AttachmentFetchOptions fetchOptions = fetchOptionsByAttachmentId.get(attachmentId);
            attachment.setFetchOptions(fetchOptions);
            if (fetchOptions.hasRegistrator())
            {
                attachment.setRegistrator(getObject(registratorsByAttachmentId, attachmentId));
                attachment.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
            }
            if (fetchOptions.hasContent())
            {
                attachment.setContent(getObject(contentsByAttachmentId, attachmentId));
                attachment.getFetchOptions().withContent();
            }
            Set<Entry<Long, Long>> entrySet = nextVersionIdByAttachmentId.entrySet();
            for (Entry<Long, Long> entry : entrySet)
            {
                Attachment version = attachments.get(entry.getValue());
                Attachment previousVersion = attachments.get(entry.getKey());
                if (version != null && previousVersion != null)
                {
                    version.setPreviousVersion(previousVersion);
                    version.getFetchOptions().withPreviousVersion();
                }
            }
        }

        void handleAttachmentsOfDifferentVersions(List<AttachmentBaseRecord> recordsSortedByVersion,
                AttachmentFetchOptions fetchOptions)
        {
            AttachmentFetchOptions currentFetchOptions = fetchOptions;
            Long nextVersionId = null;
            for (AttachmentBaseRecord record : recordsSortedByVersion)
            {
                Long attachmentId = record.id;
                records.put(attachmentId, record);
                if (nextVersionId != null)
                {
                    nextVersionIdByAttachmentId.put(attachmentId, nextVersionId);
                }
                fetchOptionsByAttachmentId.put(attachmentId, currentFetchOptions);
                if (currentFetchOptions.hasRegistrator())
                {
                    addRequestForRegistrator(attachmentId, currentFetchOptions.withRegistrator());
                }
                if (currentFetchOptions.hasContent())
                {
                    attachmentsWithContent.add(attachmentId);
                }
                if (currentFetchOptions.hasPreviousVersion() == false)
                {
                    break;
                }
                currentFetchOptions = currentFetchOptions.withPreviousVersion();
                nextVersionId = attachmentId;
            }
        }

        void translateRelatedObjects(TranslationContext context)
        {
            Set<Entry<PersonFetchOptions, List<Long>>> entrySet = attachmentsWithRegistrator.entrySet();
            for (Entry<PersonFetchOptions, List<Long>> entry : entrySet)
            {
                PersonFetchOptions fetchOptions = entry.getKey();
                List<Long> attachmentIds = entry.getValue();
                registratorsByAttachmentId.putAll(registratorTranslator.translate(context, attachmentIds, fetchOptions));
            }
            contentsByAttachmentId = contentTranslator.translate(context, attachmentsWithContent, null);
        }

        private void addRequestForRegistrator(Long attachmentId, PersonFetchOptions fetchOptions)
        {
            List<Long> list = attachmentsWithRegistrator.get(fetchOptions);
            if (list == null)
            {
                list = new ArrayList<>();
                attachmentsWithRegistrator.put(fetchOptions, list);
            }
            list.add(attachmentId);
        }

    }

}
