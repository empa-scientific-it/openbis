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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.FILE_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.PERMLINK;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.TITLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs.VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link AttachmentVersions} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class AttachmentVersionsProvider extends
        AbstractCommonTableModelProvider<AttachmentVersions>
{
    private final TechId holderId;

    private final AttachmentHolderKind holderKind;

    public AttachmentVersionsProvider(ICommonServer commonServer, String sessionToken,
            TechId holderId, AttachmentHolderKind holderKind)
    {
        super(commonServer, sessionToken);
        this.holderId = holderId;
        this.holderKind = holderKind;
    }

    @Override
    protected TypedTableModel<AttachmentVersions> createTableModel()
    {
        List<Attachment> attachments = listAttachments();
        TypedTableModelBuilder<AttachmentVersions> builder =
                new TypedTableModelBuilder<AttachmentVersions>();
        builder.addColumn(FILE_NAME).withDefaultWidth(200);
        builder.addColumn(PERMLINK);
        builder.addColumn(VERSION);
        builder.addColumn(TITLE).withDefaultWidth(200);
        builder.addColumn(DESCRIPTION).withDefaultWidth(300);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        for (AttachmentVersions versions : convert(attachments))
        {
            builder.addRow(versions);
            builder.column(FILE_NAME).addString(versions.getCurrent().getFileName());
            builder.column(PERMLINK).addString(versions.getLatestVersionPermlink());
            builder.column(VERSION).addInteger((long) versions.getCurrent().getVersion());
            builder.column(TITLE).addString(versions.getCurrent().getTitle());
            builder.column(DESCRIPTION).addString(versions.getCurrent().getDescription());
            builder.column(REGISTRATOR).addPerson(versions.getCurrent().getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(versions.getCurrent().getRegistrationDate());
        }
        return builder.getModel();
    }

    private List<Attachment> listAttachments()
    {
        List<Attachment> attachments = null;
        switch (holderKind)
        {
            case EXPERIMENT:
                attachments = commonServer.listExperimentAttachments(sessionToken, holderId);
                break;
            case SAMPLE:
                attachments = commonServer.listSampleAttachments(sessionToken, holderId);
                break;
            case PROJECT:
                attachments = commonServer.listProjectAttachments(sessionToken, holderId);
                break;
        }
        return attachments;
    }

    private List<AttachmentVersions> convert(final List<Attachment> attachments)
    {
        Map<String, List<Attachment>> map = new HashMap<String, List<Attachment>>();
        for (Attachment a : attachments)
        {
            if (false == map.containsKey(a.getFileName()))
            {
                map.put(a.getFileName(), new ArrayList<Attachment>());
            }
            map.get(a.getFileName()).add(a);
        }
        final List<AttachmentVersions> result = new ArrayList<AttachmentVersions>(map.size());
        for (List<Attachment> versions : map.values())
        {
            result.add(new AttachmentVersions(versions));
        }
        return result;
    }

}
