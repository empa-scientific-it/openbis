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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;

/**
 * Stores last database modification state and provides a method to update the state from the server. Informs a specified modification observer about
 * relevant modifications. For more details see {@link LastModificationState}.
 * <p>
 * This updater is associated with {@link DefaultTabItem} where either a {@link DatabaseModificationAwareComponent} or an {@link IDisposableComponent}
 * is the {@link IDatabaseModificationObserver} instance which will be informed if relevant modifications have been detected.
 * 
 * @author Tomasz Pylak
 */
class LastModificationStateUpdater extends AbstractAsyncCallback<LastModificationState>
{
    private final ICommonClientServiceAsync commonService;

    /*
     * The last database objects modification state seen by this tab. Can be null only for a short period after the first tab activation when we are
     * waiting for the server response.
     */
    private LastModificationState lastSeenModificationStateOrNull;

    private final IDatabaseModificationObserver modificationObserver;

    public LastModificationStateUpdater(IViewContext<?> viewContext,
            IDatabaseModificationObserver modificationObserver)
    {
        super(viewContext, false);
        this.commonService = viewContext.getCommonService();
        this.modificationObserver = modificationObserver;
        this.lastSeenModificationStateOrNull = null;
        this.ignore();
    }

    public void update()
    {
        reuse();
        commonService.getLastModificationState(this);
    }

    @Override
    protected void process(LastModificationState freshState)
    {
        refreshIfNecessary(freshState);
        updateState(freshState);
    }

    private void refreshIfNecessary(LastModificationState freshState)
    {
        if (lastSeenModificationStateOrNull == null)
        {
            // this is the first activation, no refresh is needed
            return;
        }
        DatabaseModificationKind[] relevantModifications =
                modificationObserver.getRelevantModifications();
        Set<DatabaseModificationKind> unseenModifications =
                extractUnseenModifications(relevantModifications, freshState,
                        lastSeenModificationStateOrNull);
        if (unseenModifications.size() > 0)
        {
            modificationObserver.update(unseenModifications);
        }
    }

    // returns all new relevant modifications
    private static Set<DatabaseModificationKind> extractUnseenModifications(
            DatabaseModificationKind[] relevantModifications, LastModificationState freshState,
            LastModificationState lastSeenModificationState)
    {
        Set<DatabaseModificationKind> unseenModifications = new HashSet<DatabaseModificationKind>();
        for (DatabaseModificationKind modification : relevantModifications)
        {
            long lastSeen = lastSeenModificationState.getLastModificationTime(modification);
            long lastModification = freshState.getLastModificationTime(modification);
            if (lastSeen < lastModification)
            {
                unseenModifications.add(modification);
            }
        }
        return unseenModifications;
    }

    private void updateState(LastModificationState freshState)
    {
        this.lastSeenModificationStateOrNull = freshState;
    }
}
