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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractEntityDataSetsSection;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.EntityConnectionTypeProvider;

/**
 * {@link TabContent} containing experiment samples.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentSamplesSection extends DisposableTabContent
{
    private static final String PREFIX = "experiment-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final BasicEntityType experimentType;

    private final TechId experimentId;

    private final CheckBox showOnlyDirectlyConnectedCheckBox;

    public ExperimentSamplesSection(IViewContext<?> viewContext, String header,
            BasicEntityType experimentType, IIdAndCodeHolder experimentId)
    {
        super(header, viewContext, experimentId);
        this.experimentType = experimentType;
        this.experimentId = new TechId(experimentId.getId());
        setIds(DisplayTypeIDGenerator.EXPERIMENT_SAMPLES_SECTION);
        this.showOnlyDirectlyConnectedCheckBox = createShowOnlyDirectlyConnectedCheckBox();
    }

    private CheckBox createShowOnlyDirectlyConnectedCheckBox()
    {
        CheckBox result = new CheckBox();
        result.setId(getId()
                + AbstractEntityDataSetsSection.SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX);
        result.setBoxLabel(viewContext.getMessage(Dict.SHOW_ONLY_DIRECTLY_CONNECTED));
        result.setValue(true);
        return result;
    }

    // @Private
    static String createGridId(TechId experimentId)
    {
        return SampleBrowserGrid.createGridId(createBrowserId(experimentId));
    }

    private static String createBrowserId(TechId experimentId)
    {
        return ID_PREFIX + experimentId + "-browser";
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        IDisposableComponent disposableComponent =
                SampleBrowserGrid.createGridForExperimentSamples(
                        viewContext.getCommonViewContext(), experimentId,
                        createBrowserId(experimentId), experimentType,
                        new EntityConnectionTypeProvider(showOnlyDirectlyConnectedCheckBox));
        // SampleBrowserGrid sampleBrowserGrid =
        // (SampleBrowserGrid) disposableComponent.getComponent();
        getHeader().addTool(showOnlyDirectlyConnectedCheckBox);
        return disposableComponent;
    }

}
