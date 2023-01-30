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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.Criterion;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.MatchCriteriaRadio;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link DetailedSearchCriteriaWidget}.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public final class FillSearchCriteria extends AbstractDefaultTestCommand
{
    private final List<Criterion> criteria;

    private boolean criteriaDefined;

    private Boolean matchAll;

    public static final FillSearchCriteria searchForDataSetWithCode(final String code)
    {
        FillSearchCriteria result = new FillSearchCriteria();
        result.addAttributeCriterion(DataSetAttributeSearchFieldKind.CODE, code);
        return result;
    }

    public FillSearchCriteria()
    {
        criteria = new ArrayList<Criterion>();
        criteriaDefined = false;
        matchAll();
    }

    public FillSearchCriteria matchAll()
    {
        matchAll = true;
        return this;
    }

    public FillSearchCriteria matchAny()
    {
        matchAll = false;
        return this;
    }

    public FillSearchCriteria addAttributeCriterion(IAttributeSearchFieldKind attribute,
            String value)
    {
        return addCriterion(attribute.getDescription(), value);
    }

    public FillSearchCriteria addPropertyCriterion(String name, String value)
    {
        final String field = DetailedSearchFieldKind.PROPERTY.getDescription() + " '" + name + "'";
        return addCriterion(field, value);
    }

    private FillSearchCriteria addCriterion(String name, String value)
    {
        criteria.add(new Criterion(name, value));
        criteriaDefined = true;
        return this;
    }

    @Override
    public final void execute()
    {
        assert criteriaDefined : "At least one search criterion should be specified";
        if (matchAll != null)
        {
            if (matchAll)

            {
                final Radio andRadio =
                        (Radio) GWTTestUtil.getWidgetWithID(MatchCriteriaRadio.AND_RADIO_ID);
                andRadio.setValue(true);
            } else

            {
                final Radio anyRadio =
                        (Radio) GWTTestUtil.getWidgetWithID(MatchCriteriaRadio.OR_RADIO_ID);
                anyRadio.setValue(true);
            }
        }
        for (int i = 0; i < criteria.size(); i++)
        {
            final DetailedSearchFieldsSelectionWidget selector =
                    (DetailedSearchFieldsSelectionWidget) GWTTestUtil
                            .getWidgetWithID(DropDownList.ID
                                    + DetailedSearchFieldsSelectionWidget.SUFFIX
                                    + DetailedSearchCriteriaWidget.FIRST_ID_SUFFIX + getSuffix(i));
            GWTUtils.setSelectedItem(selector, ModelDataPropertyNames.CODE, criteria.get(i)
                    .getName());
            GWTTestUtil.setTextField(DetailedSearchCriterionWidget.VALUE_FIELD_ID_PREFIX
                    + DetailedSearchCriteriaWidget.FIRST_ID_SUFFIX + getSuffix(i), criteria.get(i)
                    .getValue());
            GWTTestUtil.clickButtonWithID(DetailedSearchCriterionWidget.ADD_BUTTON_ID_PREFIX
                    + DetailedSearchCriteriaWidget.FIRST_ID_SUFFIX);
        }
        GWTTestUtil.clickButtonWithID(DetailedSearchWindow.SEARCH_BUTTON_ID);
    }

    private final String getSuffix(int i)
    {
        if (i == 0)
        {
            return "";
        } else
        {
            return "_" + (i - 1);
        }
    }

}
