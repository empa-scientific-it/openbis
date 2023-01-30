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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * {@link ITestCommand} which allows to modify column settings of a {@link Grid}.
 * 
 * @author Franz-Josef Elmer
 */
public class ChangeTableColumnSettingsCommand extends AbstractDefaultTestCommand
{
    private static abstract class Event
    {
        private final EventType eventType;

        protected final String columnID;

        Event(EventType eventType, String columnID)
        {
            this.eventType = eventType;
            this.columnID = columnID;
        }

        public final EventType getEventType()
        {
            return eventType;
        }

        protected ColumnModelEvent createColumnModelEvent(ColumnModel columnModel)
        {
            int index = columnModel.getIndexById(columnID);
            if (index < 0)
            {
                throw new IllegalArgumentException("Unknown column '" + columnID + "'.");
            }
            ColumnModelEvent event = new ColumnModelEvent(columnModel, index);
            return event;
        }

        protected ColumnConfig getColumnConfig(ColumnModel columnModel)
        {
            ColumnConfig columnConfig = columnModel.getColumnById(columnID);
            if (columnConfig == null)
            {
                throw new IllegalArgumentException("Unknown column '" + columnID + "'.");
            }
            return columnConfig;
        }

        public abstract BaseEvent changeModelAndCreateEvent(ColumnModel columnModel);
    }

    private static final class HiddenChangeEvent extends Event
    {
        private final boolean hidden;

        HiddenChangeEvent(String columnID, boolean hidden)
        {
            super(Events.HiddenChange, columnID);
            this.hidden = hidden;
        }

        @Override
        public BaseEvent changeModelAndCreateEvent(ColumnModel columnModel)
        {
            getColumnConfig(columnModel).setHidden(hidden);
            ColumnModelEvent event = createColumnModelEvent(columnModel);
            event.setHidden(hidden);
            return event;
        }
    }

    private static final class WidthChangeEvent extends Event
    {
        private final int width;

        WidthChangeEvent(String columnID, int width)
        {
            super(Events.WidthChange, columnID);
            this.width = width;
        }

        @Override
        public BaseEvent changeModelAndCreateEvent(ColumnModel columnModel)
        {
            getColumnConfig(columnModel).setWidth(width);
            ColumnModelEvent event = createColumnModelEvent(columnModel);
            event.setWidth(width);
            return event;
        }
    }

    private final String tableID;

    private final List<Event> events = new ArrayList<Event>();

    /**
     * Creates an instance for the specified table or grid ID.
     */
    public ChangeTableColumnSettingsCommand(final String tableID)
    {
        super();
        this.tableID = tableID;
    }

    /**
     * Adds an event which sets the hiding flag of the specified column.
     */
    public ChangeTableColumnSettingsCommand hiddenChangeEvent(String columnID, boolean hidden)
    {
        events.add(new HiddenChangeEvent(columnID, hidden));
        return this;
    }

    /**
     * Adds an event which sets the width of the specified column.
     */
    public ChangeTableColumnSettingsCommand widthChangeEvent(String columnID, int width)
    {
        events.add(new WidthChangeEvent(columnID, width));
        return this;
    }

    /**
     * Executes the column changing events in the order they had been added.
     */
    @Override
    public void execute()
    {
        ColumnModel columnModel = GWTTestUtil.getGridWithID(tableID).getColumnModel();
        for (Event event : events)
        {
            columnModel.fireEvent(event.getEventType(), event
                    .changeModelAndCreateEvent(columnModel));
        }
    }
}
