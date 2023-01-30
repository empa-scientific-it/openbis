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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.GridDisplaySettings;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.IDisplaySettingsDelayedUpdater;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;

/**
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManagerTest extends AssertJUnit
{
    // null if specified configs match the expected model, error message otherwise.
    private static void assertMatches(List<ColumnConfig> columnConfigs,
            ColumnConfig... expectedColumnConfigs)
    {
        if (expectedColumnConfigs.length != columnConfigs.size())
        {
            fail(expectedColumnConfigs.length + " columns expected instead of "
                    + columnConfigs.size());
        }
        for (int i = 0; i < expectedColumnConfigs.length; i++)
        {
            String prefix = "[" + i + "]: ";
            ColumnConfig columnConfig = expectedColumnConfigs[i];
            String colId = columnConfigs.get(i).getId();
            if (columnConfig.getId().equals(colId) == false)
            {
                fail(prefix + "ID " + columnConfig.getId() + " expected instead of " + colId);
            }
            if (columnConfig.isHidden() != columnConfigs.get(i).isHidden())
            {
                fail(prefix + "Hidden flag " + columnConfig.isHidden() + " expected instead of "
                        + columnConfigs.get(i).isHidden());
            }
            if (columnConfig.getWidth() != columnConfigs.get(i).getWidth())
            {
                fail(prefix + "Width " + columnConfig.getWidth() + " expected instead of "
                        + columnConfigs.get(i).getWidth());
            }
        }
    }

    private static final String DISPLAY_TYPE_ID = "id1";

    private Mockery context;

    private IDisplaySettingsDelayedUpdater updater;

    private IDisplaySettingsGetter grid;

    private DisplaySettingsManager manager;

    private DisplaySettings displaySettings;

    private ColumnModelEvent event;

    private WebClientConfiguration webClientConfiguration;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        updater = context.mock(IDisplaySettingsDelayedUpdater.class);
        grid = context.mock(IDisplaySettingsGetter.class);
        displaySettings = new DisplaySettings();
        webClientConfiguration = new WebClientConfiguration();
        manager = new DisplaySettingsManager(displaySettings, updater, webClientConfiguration);
        event = new ColumnModelEvent(null);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForUnknownDisplayTypeID()
    {
        final ColumnModel columnModel = new ColumnModel(new ArrayList<ColumnConfig>());
        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertNull(result);
        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForUnchangedColumnSettings()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        List<ColumnConfig> columnConfigs = Arrays.asList(c1, c2);
        final ColumnModel columnModel = new ColumnModel(columnConfigs);
        List<ColumnSetting> settings =
                Arrays.asList(createColumnSetting(c1), createColumnSetting(c2));
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertNull(result);
        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForChangedHiddenFlag()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        final ColumnSetting c1Setting = createColumnSetting(c1);
        c1Setting.setHidden(true);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, createColumnSetting(c2));
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertMatches(result.getColumnConfigs(), createColumnConfig(c1Setting), c2);
        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForChangedColumnOrder()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        ColumnSetting c1Setting = createColumnSetting(c1);
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c2Setting, c1Setting);
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertMatches(result.getColumnConfigs(), c2, c1);

        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForColumnNoLongerExist()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        // we expect that "c1" column will be in the settings but it will
        // be marked as hidden as it is no longer in the config (LMS-2711)
        final ColumnConfig c1Expected = createColumnConfig("c1", true, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);

        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c2));
        ColumnSetting c1Setting = createColumnSetting(c1);
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, c2Setting);
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertMatches(result.getColumnConfigs(), c1Expected, c2);

        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForNewColumn()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c2Setting);
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertMatches(result.getColumnConfigs(), c2, c1);

        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForChangedColumnWidth()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        final ColumnSetting c1Setting = createColumnSetting(c1);
        c1Setting.setWidth(24);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, createColumnSetting(c2));
        manager.updateColumnSettings(DISPLAY_TYPE_ID, settings, this);

        List<String> filterColumnIds = new ArrayList<String>();
        GridDisplaySettings result =
                manager.tryApplySettings(DISPLAY_TYPE_ID, columnModel, filterColumnIds, null);
        assertMatches(result.getColumnConfigs(), createColumnConfig(c1Setting), c2);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateAfterWidthChangedEvent()
    {
        testUpdateAfterEvent(Events.WidthChange);
    }

    private void testUpdateAfterEvent(EventType eventType)
    {
        ColumnConfig column1 = createColumnConfig("c1", false, 42);
        ColumnConfig column2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(column1, column2));
        final List<String> filterColumnIds = Arrays.asList(column1.getId());
        context.checking(new Expectations()
            {
                {
                    allowing(grid).getColumnModel();
                    will(returnValue(columnModel));

                    allowing(grid).getFilteredColumnIds();
                    will(returnValue(filterColumnIds));

                    allowing(grid).getModifier();
                    will(returnValue(grid));

                    allowing(grid).getSortState();
                    will(returnValue(null));

                    one(updater).executeDelayed(with(any(IDisplaySettingsUpdate.class)),
                            with(any(Integer.class)));
                }
            });

        manager.registerGridSettingsChangesListener(DISPLAY_TYPE_ID, grid);
        columnModel.fireEvent(eventType, event);

        List<ColumnSetting> columnSettings = manager.getColumnSettings(DISPLAY_TYPE_ID);
        assertEquals(2, columnSettings.size());
        ColumnSetting col1 = columnSettings.get(0);
        assertEquals(false, col1.isHidden());
        assertEquals(42, col1.getWidth());
        assertEquals("c1", col1.getColumnID());
        assertTrue(col1.hasFilter());
        ColumnSetting col2 = columnSettings.get(1);
        assertEquals(true, col2.isHidden());
        assertEquals(4711, col2.getWidth());
        assertEquals("c2", col2.getColumnID());
        assertFalse(col2.hasFilter());

        context.assertIsSatisfied();
    }

    private ColumnConfig createColumnConfig(ColumnSetting columnSetting)
    {
        return createColumnConfig(columnSetting.getColumnID(), columnSetting.isHidden(),
                columnSetting.getWidth());
    }

    private ColumnConfig createColumnConfig(String id, boolean hidden, int width)
    {
        ColumnConfig columnConfig = new ColumnConfig(id, "<" + id + ">", width);
        columnConfig.setHidden(hidden);
        return columnConfig;
    }

    private ColumnSetting createColumnSetting(ColumnConfig columnConfig)
    {
        ColumnSetting columnSetting = new ColumnSetting();
        columnSetting.setColumnID(columnConfig.getId());
        columnSetting.setHidden(columnConfig.isHidden());
        columnSetting.setWidth(columnConfig.getWidth());
        return columnSetting;
    }
}
