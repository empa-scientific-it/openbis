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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment.AttachmentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;

/**
 * {@link TabContent} containing a list of {@link AttachmentVersions}.
 * 
 * @author Piotr Buczek
 */
public class AttachmentVersionsSection extends DisposableTabContent
{
    private final IAttachmentHolder attachmentHolder;

    public AttachmentVersionsSection(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IAttachmentHolder attachmentHolder)
    {
        super(viewContext.getMessage(Dict.ATTACHMENTS), viewContext, attachmentHolder);
        this.attachmentHolder = attachmentHolder;
        setIds(DisplayTypeIDGenerator.ATTACHMENT_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return AttachmentBrowser.create(viewContext.getCommonViewContext(), attachmentHolder);
    }
}
