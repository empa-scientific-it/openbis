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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

public class ProcessingDisplayCallback extends AbstractAsyncCallback<Void>
{

    public ProcessingDisplayCallback(IViewContext<?> viewContext)
    {
        super(viewContext);
    }

    @Override
    public final void process(final Void result)
    {
        MessageBox.info("Processing", "Processing has been scheduled successfully.", null);
    }

    @Override
    public void finishOnFailure(Throwable caught)
    {
        super.finishOnFailure(caught);
    }
}