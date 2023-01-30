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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.module;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.Dict;

/**
 * Demo experiment statistics.
 * 
 * @author Izabela Adamczyk
 */
public class StatisticsWidget extends ContentPanel implements IDatabaseModificationObserver
{
    protected static final String ID = GenericConstants.ID_PREFIX + "demo-module" + "-statistics";

    private final IViewContext<IDemoClientServiceAsync> viewContext;

    private Html numberOfExperiments;

    private StatisticsWidget(IViewContext<IDemoClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setHeading(viewContext.getMessage(Dict.STATISTICS_DEMO_TAB_HEADER));
        add(numberOfExperiments = new Html());
    }

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IDemoClientServiceAsync> viewContext)
    {
        StatisticsWidget viewer = new StatisticsWidget(viewContext);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void refresh()
    {
        viewContext.getService().getNumberOfExperiments(
                new ExperimentStatisticsCallback(viewContext, numberOfExperiments));
    }

    private static final class ExperimentStatisticsCallback extends AbstractAsyncCallback<Integer>
    {
        private final Html target;

        private ExperimentStatisticsCallback(
                final IViewContext<IDemoClientServiceAsync> viewContext, final Html target)
        {
            super(viewContext);
            this.target = target;
        }

        @Override
        protected final void process(final Integer result)
        {
            target.setHtml("Number of experiments: " + result.intValue());
        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refresh();
    }

}
