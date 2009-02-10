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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria.CriteriaConnection;

/**
 * Widget which enables to select {@link CriteriaConnection} type.
 * 
 * @author Izabela Adamczyk
 */
public class MatchCriteriaRadio extends HorizontalPanel
{

    private final Radio orRadio;

    private final Radio andRadio;

    public MatchCriteriaRadio(String matchAll, String matchAny)
    {
        RadioGroup group = new RadioGroup();
        andRadio = new Radio();
        andRadio.setBoxLabel(matchAll);

        orRadio = new Radio();
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

    SearchCriteria.CriteriaConnection getSelected()
    {
        if (isAndSelected())
        {
            return SearchCriteria.CriteriaConnection.AND;
        } else
        {
            return SearchCriteria.CriteriaConnection.OR;
        }
    }

    private boolean isAndSelected()
    {
        return andRadio.getValue() != null && andRadio.getValue().booleanValue() == true;
    }
}