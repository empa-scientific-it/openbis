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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;

/**
 * Listener for a Grid which delegates click on a cell or link in a cell to a registered {@link ICellListener}.
 * 
 * @author Franz-Josef Elmer
 */
public final class ColumnListener<T, M extends BaseEntityModel<T>> implements
        Listener<GridEvent<?>>
{
    private final Map<String, ICellListener<T>> linkListeners =
            new HashMap<String, ICellListener<T>>();

    private final Grid<M> grid;

    /**
     * Creates an instance for the specified grid.
     */
    public ColumnListener(Grid<M> grid)
    {
        this.grid = grid;
        grid.addListener(Events.CellClick, this);
    }

    /**
     * Registers the specified link listener for the specified column.
     */
    public void registerLinkClickListener(String columnID, ICellListener<T> listener)
    {
        linkListeners.put(columnID.toLowerCase(), listener);
    }

    /**
     * Invokes {@link ICellListener#handle(Object,boolean)} with the base object of the row specified by the row index of the event. The cell listener
     * is determined by the column index. This method does nothing if no cell listener has been registered for column determined by the event.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void handleEvent(GridEvent<?> be)
    {
        ICellListener<T> listener = getCellListener(be);
        if (listener != null)
        {
            ListStore<?> store = be.getGrid().getStore();
            listener.handle(((BaseEntityModel<T>) store.getAt(be.getRowIndex())).getBaseObject(),
                    WidgetUtils.ifSpecialKeyPressed(be.getEvent()));
        }
    }

    /** @return appropriate cell or link listener for given <var>event</var> */
    private ICellListener<T> getCellListener(GridEvent<?> event)
    {
        String columnID =
                grid.getColumnModel().getColumn(event.getColIndex()).getId().toLowerCase();

        return isLinkTarget(event) ? linkListeners.get(columnID) : null;
    }

    private static String LINK_TAG_NAME = "A";

    /** @return <code>true</code> if the target element for given <var>event</var> is a link */
    public static boolean isLinkTarget(GridEvent<?> event)
    {
        // check for null needed because of fake events in system tests
        return event.getTarget() != null && event.getTarget().getTagName().equals(LINK_TAG_NAME);
    }

}
