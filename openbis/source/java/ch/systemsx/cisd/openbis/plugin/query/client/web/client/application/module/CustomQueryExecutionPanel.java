/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;

/**
 * @author Piotr Buczek
 */
public class CustomQueryExecutionPanel extends ContentPanel implements
        IDatabaseModificationObserver
{

    public static final String ID =
            GenericConstants.ID_PREFIX + "query-module" + "_custom-query-executor";

    @SuppressWarnings("unused")
    private final IViewContext<IQueryClientServiceAsync> viewContext;

    public CustomQueryExecutionPanel(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setHeading("test"); // TODO
    }

    public static DatabaseModificationAwareComponent create(
            IViewContext<IQueryClientServiceAsync> viewContext)
    {
        CustomQueryExecutionPanel panel = new CustomQueryExecutionPanel(viewContext);
        return new DatabaseModificationAwareComponent(panel, panel);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        // TODO Auto-generated method stub
    }

}
