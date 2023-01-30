/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Panel of a details view showing a component which should be disposed at the end (i.e. browser grid).
 * 
 * @author Franz-Josef Elmer
 */
abstract public class DisposableTabContent extends TabContent
{
    abstract protected IDisposableComponent createDisposableContent();

    // null if the section has not been shown yet
    // or content is created asynchronously (then subclass should call updateContent())
    protected IDisposableComponent disposableComponentOrNull = null;

    /**
     * Creates section with specified header.
     */
    public DisposableTabContent(String header, IViewContext<?> viewContext, IIdHolder ownerIdOrNull)
    {
        super(header, viewContext, ownerIdOrNull);
    }

    public IDatabaseModificationObserver tryGetDatabaseModificationObserver()
    {
        return disposableComponentOrNull;
    }

    public IDisposableComponent tryGetDisposableComponentOrNull()
    {
        return disposableComponentOrNull;
    }

    @Override
    public void disposeComponents()
    {
        if (disposableComponentOrNull != null)
        {
            disposableComponentOrNull.dispose();
        }
    }

    @Override
    protected void showContent()
    {
        IDisposableComponent contentOrNull = createDisposableContent();
        if (contentOrNull != null)
        {
            updateContent(contentOrNull, false);
        }
    }

    protected void updateContent(IDisposableComponent content, boolean syncNeeded)
    {
        if (content != null)
        {
            disposableComponentOrNull = content;
            add(content.getComponent());
            if (syncNeeded)
            {
                syncSize();
            }
        }
    }

    protected void replaceContent(IDisposableComponent content)
    {
        if (content != null)
        {
            removeAll();
            disposeComponents();
            updateContent(content, true);
        }
    }

}
