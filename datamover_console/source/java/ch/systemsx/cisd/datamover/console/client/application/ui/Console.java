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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.datamover.console.client.application.IMessageResources;
import ch.systemsx.cisd.datamover.console.client.application.ViewContext;
import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;

/**
 * Main page.
 *
 * @author Franz-Josef Elmer
 */
public class Console extends Composite
{
    private static final String STYLE_PREFIX = "console-";
    private static final String STYLE_HEADER_PREFIX = STYLE_PREFIX + "header-";
    private static final String STYLE_TABLE_PREFIX = STYLE_PREFIX + "table-";
    
    private final ViewContext viewContext;
    private final VerticalPanel content;
    
    private final AsyncCallback<Void> refreshCallBack;
        
    private Map<String, String> targets;

    public Console(ViewContext viewContext)
    {
        this.viewContext = viewContext;
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName(STYLE_PREFIX + "main");
        panel.add(createHeaderPanel());
        content = new VerticalPanel();
        content.setStyleName(STYLE_PREFIX + "content");
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.add(content);
        initWidget(panel);
        
        viewContext.getService().getTargets(new AbstractAsyncCallback<Map<String, String>>(viewContext)
            {
                public void onSuccess(Map<String, String> list)
                {
                    targets = list;
                    refreshView();
                }
            });
        refreshCallBack = new AbstractAsyncCallback<Void>(viewContext)
            {
                public void onSuccess(Void arg0)
                {
                    refreshView();
                }
            };
        Timer timer = new Timer()
            {
                @Override
                public void run()
                {
                    refreshView();
                }
            };
        viewContext.getPageController().setTimer(timer);
        ApplicationInfo applicationInfo = viewContext.getModel().getApplicationInfo();
        timer.scheduleRepeating(applicationInfo.getRefreshTimeInterval());
    }
    
    private Widget createHeaderPanel()
    {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(10);
        
        Label label = new Label(viewContext.getModel().getUser().getUserFullName());
        label.setStyleName(STYLE_HEADER_PREFIX + "label");
        headerPanel.add(label);
        
        String buttonLabel = viewContext.getMessageResources().getLogoutButtonLabel();
        headerPanel.add(createTableButton(buttonLabel, new ClickListener()
            {
                public void onClick(Widget arg0)
                {
                    viewContext.getService().logout(new AbstractAsyncCallback<Void>(viewContext)
                        {
                            public void onSuccess(Void v)
                            {
                                viewContext.getPageController().reload();
                            }
                        });
                }
            }));
        return headerPanel;
    }
    
    private Button createHeaderButton(String label, ClickListener listener)
    {
        Button button = new Button(label);
        if (listener != null)
        {
            button.addClickListener(listener);
        }
        return button;
    }

    private void refreshView()
    {
        showWaitMessage();
        viewContext.getService().listDatamoverInfos(
                new AbstractAsyncCallback<List<DatamoverInfo>>(viewContext)
                    {
                        public void onSuccess(List<DatamoverInfo> list)
                        {
                            showTable(list);
                        }
                    });
    }

    private void showWaitMessage()
    {
        content.clear();
        content.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
        content.add(new Label(viewContext.getMessageResources().getConsoleWaitMessage()));
    }
    
    private void showTable(List<DatamoverInfo> list)
    {
        content.clear();
        content.add(createStatusLine());
        content.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        content.add(createView(list));
        String buttonLabel = viewContext.getMessageResources().getRefreshButtonLabel();
        content.add(createHeaderButton(buttonLabel, new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    refreshView();
                }
            }));
    }
    
    private Widget createStatusLine()
    {
        return new Label(viewContext.getMessageResources().getConsoleStatusLine(new Date()));
    }

    private Widget createView(List<DatamoverInfo> list)
    {
        Grid grid = new Grid(list.size() + 1, 4);
        grid.setStyleName(STYLE_TABLE_PREFIX + "table");
        IMessageResources messageResources = viewContext.getMessageResources();
        grid.setText(0, 0, messageResources.getDatamoverColumnHeader());
        grid.setText(0, 1, messageResources.getTargetLocationColumnHeader());
        grid.setText(0, 2, messageResources.getStatusColumnHeader());
        grid.setText(0, 3, messageResources.getCommandColumnHeader());
        grid.getRowFormatter().setStyleName(0, STYLE_TABLE_PREFIX + "header");
        for (int i = 0, n = list.size(); i < n; i++)
        {
            DatamoverInfo datamoverInfo = list.get(i);
            createRow(grid, datamoverInfo, i + 1);
            grid.getRowFormatter().setStyleName(i + 1,
                    STYLE_TABLE_PREFIX + (i % 2 == 0 ? "odd-row" : "even-row"));
        }
        return grid;
    }

    private void createRow(Grid grid, final DatamoverInfo datamoverInfo, int rowIndex)
    {
        grid.setText(rowIndex, 0, datamoverInfo.getName());
        DatamoverStatus status = datamoverInfo.getStatus();
        grid.setText(rowIndex, 2, status.toString());
        IMessageResources messageResources = viewContext.getMessageResources();
        if (status == DatamoverStatus.DOWN || status == DatamoverStatus.STALE)
        {
            final ListBox targetListBox = createTargetListBox();
            grid.setWidget(rowIndex, 1, targetListBox);
            String buttonLabel = messageResources.getStartButtonLabel();
            Button button = createTableButton(buttonLabel, new ClickListener()
                {
                    public void onClick(Widget arg0)
                    {
                        startDatamover(datamoverInfo.getName(), targetListBox);
                    }
                });
            grid.setWidget(rowIndex, 3, button);
        } else
        {
            grid.setText(rowIndex, 1, getTargetName(datamoverInfo));
            if (status != DatamoverStatus.SHUTDOWN)
            {
                String buttonLabel = messageResources.getStopButtonLabel();
                Button button = createTableButton(buttonLabel, new ClickListener()
                    {
                        public void onClick(Widget arg0)
                        {
                            stopDatamover(datamoverInfo.getName());
                        }
                    });
                grid.setWidget(rowIndex, 3, button);
            }
        }
    }
    
    private String getTargetName(DatamoverInfo datamoverInfo)
    {
        String targetLocation = datamoverInfo.getTargetLocation();
        for (Map.Entry<String, String> entry : targets.entrySet())
        {
            if (entry.getValue().equals(targetLocation))
            {
                return entry.getKey();
            }
        }
        return targetLocation;
    }
    
    private void startDatamover(String name, ListBox targetListBox)
    {
        String target = getSelectedValueOf(targetListBox);
        viewContext.getService().startDatamover(name, target, refreshCallBack);
    }
    
    private String getSelectedValueOf(ListBox listBox)
    {
        return listBox.getValue(listBox.getSelectedIndex());
    }
    
    private void stopDatamover(String name)
    {
        viewContext.getService().shutdownDatamover(name, refreshCallBack);
    }

    private Button createTableButton(String label, ClickListener clickListener)
    {
        Button button = new Button(label);
        button.setStyleName(STYLE_TABLE_PREFIX + "button");
        button.addClickListener(clickListener);
        return button;
    }
    
    private ListBox createTargetListBox()
    {
        ListBox list = new ListBox();
        for (Map.Entry<String, String> target : targets.entrySet())
        {
            list.addItem(target.getKey(), target.getValue());
        }
        return list;
    }
}
