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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.TableData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Widget which enables to select {@link SearchCriteriaConnection} type.
 * 
 * @author Izabela Adamczyk
 */
public class MatchCriteriaRadio extends HorizontalPanel
{
    private static final String PREFIX = "match_criteria_radio";

    private static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String OR_RADIO_ID = ID + "_or";

    public static final String AND_RADIO_ID = ID + "_and";

    private final Radio orRadio;

    private final Radio andRadio;

    public MatchCriteriaRadio(String matchAll, String matchAny)
    {
        RadioGroup group = new RadioGroup();
        andRadio = new Radio();
        andRadio.setId(AND_RADIO_ID);
        andRadio.setBoxLabel(matchAll);

        orRadio = new Radio();
        orRadio.setId(OR_RADIO_ID);
        orRadio.setBoxLabel(matchAny);

        group.add(andRadio);
        group.add(orRadio);

        reset();
        final TableData radioData =
                new TableData(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        radioData.setPadding(5);
        add(group, radioData);
    }

    public void reset()
    {
        orRadio.setValue(true);
    }

    String getSelectedLabel()
    {
        return isAndSelected() ? andRadio.getBoxLabel() : orRadio.getBoxLabel();
    }

    SearchCriteriaConnection getSelected()
    {
        if (isAndSelected())
        {
            return SearchCriteriaConnection.MATCH_ALL;
        } else
        {
            return SearchCriteriaConnection.MATCH_ANY;
        }
    }

    private boolean isAndSelected()
    {
        return andRadio.getValue() != null && andRadio.getValue().booleanValue() == true;
    }
}
