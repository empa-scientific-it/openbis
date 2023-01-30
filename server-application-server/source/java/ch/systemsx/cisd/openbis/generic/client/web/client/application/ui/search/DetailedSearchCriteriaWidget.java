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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.KeyboardEvents;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Abstract widget for management of detailed search criteria (main criteria or sub criteria).
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
abstract public class DetailedSearchCriteriaWidget extends VerticalPanel
{
    public static final String FIRST_ID_SUFFIX = "_first";

    private final List<DetailedSearchCriterionWidget> criteriaWidgets;

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    private final EntityKind entityKind;

    public DetailedSearchCriteriaWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        this.viewContext = viewContext;
        this.entityKind = entityKind;
        setLayoutOnChange(true);
        criteriaWidgets = new ArrayList<DetailedSearchCriterionWidget>();
    }

    protected abstract String getCriteriaLabel();

    protected abstract SearchCriteriaConnection getConnection();

    protected abstract void setConnection(SearchCriteriaConnection connection);

    protected void addInitialWidgets()
    {
        addCriterion(new DetailedSearchCriterionWidget(viewContext, this, FIRST_ID_SUFFIX,
                this.entityKind));
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
        addInitialWidgets();
    }

    private void enableRemovalIfOneExists(final boolean enable)
    {
        if (criteriaWidgets.size() == 1)
        {
            criteriaWidgets.get(0).enableRemoveButton(enable);
        }
    }

    /**
     * Adds given {@link DetailedSearchCriterionWidget} to the panel.
     */
    void addCriterion(DetailedSearchCriterionWidget criterion)
    {
        enableRemovalIfOneExists(true);
        criteriaWidgets.add(criterion);
        add(criterion);
        enableRemovalIfOneExists(false);
        layout();
        criterion.focus();
    }

    /**
     * Removes given {@link DetailedSearchCriterionWidget} from the panel, unless it is the only one that left. In this case the state of chosen
     * {@link DetailedSearchCriterionWidget} is set to initial value (reset).
     */
    void removeCriterion(DetailedSearchCriterionWidget w)
    {
        if (criteriaWidgets.size() > 1)
        {
            criteriaWidgets.remove(w);
            remove(w);
            enableRemovalIfOneExists(false);

            focusLastWidget();
        } else
        {
            w.reset();
        }
    }

    /**
     * Set focus on the last widget
     */
    private void focusLastWidget()
    {
        DetailedSearchCriterionWidget lastWidget = criteriaWidgets.get(criteriaWidgets.size() - 1);
        lastWidget.focus();
    }

    @Override
    public void focus()
    {
        super.focus();
        focusLastWidget();
    }

    public List<PropertyType> getAvailablePropertyTypes()
    {
        return criteriaWidgets.get(0).getAvailablePropertyTypes();
    }

    /**
     * @return <b>search criteria</b> extracted from criteria widgets and "match" radio buttons<br>
     *         <b>NOTE:</b> criterion list of resulting criteria may be empty
     */
    public DetailedSearchCriteria extractCriteria(boolean useWildcardSearchMode)
    {
        List<DetailedSearchCriterion> criteria = new ArrayList<DetailedSearchCriterion>();
        for (DetailedSearchCriterionWidget cw : criteriaWidgets)
        {
            DetailedSearchCriterion value = cw.tryGetValue();
            if (value != null)
            {
                criteria.add(value);
            }
        }
        final DetailedSearchCriteria result = new DetailedSearchCriteria();
        result.setUseWildcardSearchMode(useWildcardSearchMode);
        result.setConnection(getConnection());
        result.setCriteria(criteria);
        return result;
    }

    /** description of the search criteria for the user */
    public String getCriteriaDescription()
    {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (DetailedSearchCriterionWidget cw : criteriaWidgets)
        {
            String desc = cw.tryGetDescription();
            if (desc != null)
            {
                if (first == false)
                {
                    sb.append(", ");
                } else
                {
                    first = false;
                }
                sb.append(desc);
            }
        }
        return sb.toString();
    }

    public boolean isCriteriaFilled()
    {
        for (DetailedSearchCriterionWidget cw : criteriaWidgets)
        {
            DetailedSearchCriterion value = cw.tryGetValue();
            if (value != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Resets "match criteria" radio buttons to initial values, removes unnecessary criteria widgets and resets the remaining ones.
     */
    public void reset()
    {
        List<DetailedSearchCriterionWidget> list =
                new ArrayList<DetailedSearchCriterionWidget>(criteriaWidgets);
        for (DetailedSearchCriterionWidget cw : list)
        {
            removeCriterion(cw);
        }
        layout();
    }

    /**
     * Set the initial search criteria to the argument. This should be called after creation but before the user has had a chance to use the window,
     * otherwise user input may be overwritten.
     */
    public void setInitialSearchCritera(DetailedSearchCriteria searchCriteria)
    {
        List<DetailedSearchCriterion> criterionList = searchCriteria.getCriteria();
        int index = 0, size = criterionList.size();
        // Populate the existing search criterion widgets
        for (DetailedSearchCriterionWidget widget : criteriaWidgets)
        {
            DetailedSearchCriterion criterion = criterionList.get(index++);
            widget.setSearchCriterion(criterion);
        }

        // Create any additional widgets required
        for (; index < size; ++index)
        {
            DetailedSearchCriterion criterion = criterionList.get(index);
            DetailedSearchCriterionWidget widget =
                    new DetailedSearchCriterionWidget(viewContext, this, FIRST_ID_SUFFIX,
                            entityKind);
            widget.setSearchCriterion(criterion);
            addCriterion(widget);
        }

        setConnection(searchCriteria.getConnection());
    }

    void onEnterKey()
    {
        fireEvent(KeyboardEvents.Enter);
    }

}
