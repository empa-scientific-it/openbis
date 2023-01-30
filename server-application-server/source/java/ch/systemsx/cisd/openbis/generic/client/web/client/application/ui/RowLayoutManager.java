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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.SplitBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Helper class for doing {@link RowLayout} of a {@link LayoutContainer} with instances of {@link ContentPanel} in a dynamic way. Collapsed row
 * components lead to a resize of the non-collapsed components if the row size is defined by a positive value &lt;= 1.
 * 
 * @author Franz-Josef Elmer
 */
public class RowLayoutManager
{
    private static interface IManipulator
    {
        double getFor(RowData rowData);

        void setFor(RowData rowData, double newValue);

        int getFrameSizeFor(ContentPanel panel);

        int getSize(Container<?> container);

        int getPositionFrom(DragEvent dragEvent);
    }

    private static final class DynamicRowData
    {
        private RowData rowData;

        private double originalValue;

        private ContentPanel panel;
    }

    private static final IManipulator HEIGHT_MANIPULATOR = new IManipulator()
        {
            @Override
            public void setFor(RowData rowData, double newValue)
            {
                rowData.setHeight(newValue);
            }

            @Override
            public double getFor(RowData rowData)
            {
                return rowData.getHeight();
            }

            @Override
            public int getFrameSizeFor(ContentPanel panel)
            {
                return panel.isRendered() ? panel.getFrameHeight() : 0;
            }

            @Override
            public int getSize(Container<?> container)
            {
                return container.getHeight();
            }

            @Override
            public int getPositionFrom(DragEvent dragEvent)
            {
                return dragEvent.getY();
            }
        };

    private static final IManipulator WIDTH_MANIPULATOR = new IManipulator()
        {
            @Override
            public void setFor(RowData rowData, double newValue)
            {
                rowData.setWidth(newValue);
            }

            @Override
            public double getFor(RowData rowData)
            {
                return rowData.getWidth();
            }

            @Override
            public int getFrameSizeFor(ContentPanel panel)
            {
                return panel.isRendered() ? panel.getFrameWidth() : 0;
            }

            @Override
            public int getSize(Container<?> container)
            {
                return container.getWidth();
            }

            @Override
            public int getPositionFrom(DragEvent dragEvent)
            {
                return dragEvent.getX();
            }

        };

    private final LayoutContainer container;

    private final IManipulator manipulator;

    private final List<DynamicRowData> dynamicRowDatas = new ArrayList<DynamicRowData>();

    /**
     * Creates an instance for the specified container and apply the specified layout.
     */
    public RowLayoutManager(LayoutContainer container, RowLayout rowLayout)
    {
        this.container = container;
        container.setLayout(rowLayout);
        Orientation orientation = rowLayout.getOrientation();
        if (orientation.equals(Orientation.VERTICAL))
        {
            manipulator = HEIGHT_MANIPULATOR;
        } else
        {
            manipulator = WIDTH_MANIPULATOR;
        }
    }

    /**
     * Adds the specified panel to the container in accordance to the specified row data.
     */
    public void addToContainer(final ContentPanel contentPanel, RowData rowData)
    {
        if (container.getItemCount() > 0)
        {
            Listener<BaseEvent> listener = createListener(contentPanel, dynamicRowDatas.size());
            contentPanel.addListener(Events.Render, listener);
        }
        contentPanel.setAnimCollapse(false);
        container.add(contentPanel, rowData);
        double value = manipulator.getFor(rowData);
        if (value < 0 || value > 1)
        {
            // ignore fixed value sized panels
            return;
        }
        DynamicRowData dynamicRowData = new DynamicRowData();
        dynamicRowData.rowData = rowData;
        dynamicRowData.originalValue = value;
        dynamicRowData.panel = contentPanel;
        dynamicRowDatas.add(dynamicRowData);
        adjustRelative();
        Listener<BaseEvent> listener = new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent event)
                {
                    adjustRelative();
                }
            };
        contentPanel.addListener(Events.Collapse, listener);
        contentPanel.addListener(Events.Expand, listener);
    }

    private void adjustRelative()
    {
        double sum = 0;
        for (DynamicRowData data : dynamicRowDatas)
        {
            if (data.panel.isCollapsed() == false)
            {
                sum += data.originalValue;
            }
        }
        for (DynamicRowData data : dynamicRowDatas)
        {
            double value;
            if (data.panel.isCollapsed())
            {
                value = manipulator.getFrameSizeFor(data.panel);
            } else
            {
                value = data.originalValue / sum;
            }
            manipulator.setFor(data.rowData, value);
        }
        container.layout(true);
    }

    private Listener<BaseEvent> createListener(final ContentPanel contentPanel, final int indexInDynamicRowDataArray)
    {
        return new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    SplitBar splitBar = new SplitBar(LayoutRegion.NORTH, contentPanel);
                    Draggable draggable = splitBar.getDraggable();
                    draggable.addDragListener(new DragListener()
                        {

                            private int position;

                            @Override
                            public void dragEnd(DragEvent de)
                            {
                                handleSplitMovement(contentPanel, manipulator.getPositionFrom(de)
                                        - position, indexInDynamicRowDataArray);
                            }

                            @Override
                            public void dragStart(DragEvent de)
                            {
                                position = manipulator.getPositionFrom(de);
                            }
                        });
                    draggable.setMoveAfterProxyDrag(false);
                    splitBar.setAutoSize(false);
                }
            };
    }

    private void handleSplitMovement(final ContentPanel contentPanel, int diff, int indexInDynamicRowDataArray)
    {
        int indexOfResizablePanelAbove = -1;
        for (int i = indexInDynamicRowDataArray - 1; i >= 0; i--)
        {
            if (dynamicRowDatas.get(i).panel.isCollapsed() == false)
            {
                indexOfResizablePanelAbove = i;
                break;
            }
        }
        if (indexOfResizablePanelAbove < 0)
        {
            return;
        }
        int indexOfResizablePanelBelow = -1;
        for (int i = indexInDynamicRowDataArray; i < dynamicRowDatas.size(); i++)
        {
            if (dynamicRowDatas.get(i).panel.isCollapsed() == false)
            {
                indexOfResizablePanelBelow = i;
                break;
            }
        }
        if (indexOfResizablePanelBelow < 0)
        {
            return;
        }
        int sum = 0;
        for (int i = 0, n = container.getItemCount(); i < n; i++)
        {
            Component item = container.getItem(i);
            if (item instanceof ContentPanel)
            {
                ContentPanel panel = (ContentPanel) item;
                sum += manipulator.getFrameSizeFor(panel);
            }
        }
        int h = manipulator.getSize(container) - sum;
        DynamicRowData above = dynamicRowDatas.get(indexOfResizablePanelAbove);
        DynamicRowData below = dynamicRowDatas.get(indexOfResizablePanelBelow);
        double diffRatio = (double) diff / h;
        diffRatio = Math.max(diffRatio, DELTA - above.originalValue);
        diffRatio = Math.min(diffRatio, 1 - DELTA - above.originalValue);
        diffRatio = Math.min(diffRatio, below.originalValue - DELTA);
        diffRatio = Math.max(diffRatio, below.originalValue - 1 + DELTA);

        above.originalValue += diffRatio;
        below.originalValue -= diffRatio;
        adjustRelative();
    }

    private static final double DELTA = 0.02;
}