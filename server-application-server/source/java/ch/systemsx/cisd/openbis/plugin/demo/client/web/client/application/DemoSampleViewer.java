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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;

/**
 * The <i>demo</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class DemoSampleViewer extends AbstractViewer<Sample>
{
    private static final String PREFIX = "demo-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final TechId sampleId;

    public DemoSampleViewer(final IViewContext<IDemoClientServiceAsync> viewContext,
            final TechId sampleId)
    {
        super(viewContext, createId(sampleId));
        this.sampleId = sampleId;
        reloadAllData();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected IViewContext<IDemoClientServiceAsync> getViewContext()
    {
        return (IViewContext<IDemoClientServiceAsync>) viewContext;
    }

    public static final String createId(final TechId sampleId)
    {
        return ID_PREFIX + sampleId;
    }

    private final Widget createUI(final SampleParentWithDerived sampleGeneration)
    {
        return GenericSampleViewer.createPropertyGrid(getViewContext(), sampleId);
    }

    /**
     * Load the sample information.
     */
    @Override
    protected void reloadAllData()
    {
        SampleInfoCallback callback = new SampleInfoCallback(getViewContext(), this);
        getViewContext().getService()
                .getSampleGenerationInfo(sampleId, getBaseIndexURL(), callback);
    }

    //
    // Helper classes
    //

    private final class SampleInfoCallback extends AbstractAsyncCallback<SampleParentWithDerived>
    {
        private final DemoSampleViewer sampleViewer;

        private SampleInfoCallback(final IViewContext<IDemoClientServiceAsync> viewContext,
                DemoSampleViewer sampleViewer)
        {
            super(viewContext);
            this.sampleViewer = sampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleParentWithDerived} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleParentWithDerived result)
        {
            sampleViewer.updateOriginalData(result.getParent());
            sampleViewer.removeAll();
            sampleViewer.add(sampleViewer.createUI(result));
            sampleViewer.layout();
        }
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_SAMPLE);
    }

}
