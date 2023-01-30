/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;

/**
 * {@link ModelData} for {@link Attachment} (versions).
 * 
 * @author Izabela Adamczyk
 */
public class AttachmentVersionModel extends SimplifiedBaseModelData
{
    public static final String VERSION = "version";

    public static final String VERSION_FILE_NAME = "versionsFileName";

    public static final String PERMLINK = "permlink";

    private static final long serialVersionUID = 1L;

    public AttachmentVersionModel()
    {
    }

    private AttachmentVersionModel(final Attachment attachament)
    {
        set(VERSION_FILE_NAME, createDescription(attachament));
        set(VERSION, attachament.getVersion());
        set(PERMLINK, attachament.getPermlink());
        set(ModelDataPropertyNames.REGISTRATOR,
                PersonRenderer.createPersonAnchor(attachament.getRegistrator()));
        set(ModelDataPropertyNames.REGISTRATION_DATE, attachament.getRegistrationDate());
        set(ModelDataPropertyNames.OBJECT, attachament);
    }

    private String createDescription(final Attachment att)
    {
        return att.getFileName() + " (" + att.getVersion() + ")";
    }

    public final static List<AttachmentVersionModel> convert(final List<Attachment> attachments)
    {
        final ArrayList<AttachmentVersionModel> result = new ArrayList<AttachmentVersionModel>();
        for (final Attachment a : attachments)
        {
            result.add(new AttachmentVersionModel(a));
        }
        return result;
    }
}
