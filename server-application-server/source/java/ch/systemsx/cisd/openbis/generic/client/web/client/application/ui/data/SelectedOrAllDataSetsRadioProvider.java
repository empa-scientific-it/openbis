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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Piotr Buczek
 */
public class SelectedOrAllDataSetsRadioProvider
{
    private Radio computeOnSelectedRadio;

    private Radio computeOnAllRadio;

    private final ISelectedDataSetsProvider data;

    public SelectedOrAllDataSetsRadioProvider(ISelectedDataSetsProvider data)
    {
        this.data = data;
    }

    public boolean getComputeOnSelected()
    {
        if (computeOnSelectedRadio == null)
        {
            return false;
        } else
        {
            return computeOnSelectedRadio.getValue();
        }
    }

    public final RadioGroup createComputationDataSetsRadio()
    {
        final RadioGroup result = new RadioGroup();
        result.setFieldLabel("Data Sets");
        result.setSelectionRequired(true);
        result.setOrientation(Orientation.HORIZONTAL);
        computeOnAllRadio = createRadio("all");
        computeOnSelectedRadio =
                createRadio("selected (" + data.getSelectedDataSets().size() + ")");
        result.add(computeOnSelectedRadio);
        result.add(computeOnAllRadio);
        result.setValue(computeOnSelectedRadio);
        result.setAutoHeight(true);
        return result;
    }

    private final Radio createRadio(final String label)
    {
        Radio result = new Radio();
        result.setBoxLabel(label);
        return result;
    }

    public interface ISelectedDataSetsProvider
    {
        public List<AbstractExternalData> getSelectedDataSets();
    }

}