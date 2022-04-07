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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GridCustomColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * Test cases for corresponding {@link CachedResultSetManager} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = CachedResultSetManager.class)
public final class CachedResultSetManagerTest extends AssertJUnit
{
    private static final IColumnDefinition<DataHolder> DEF1 = createColDef("col1", "-", 0);

    private static final IColumnDefinition<DataHolder> DEF2 = createColDef("col2", "-", 1);

    private static final List<IColumnDefinition<DataHolder>> COL_DEFS =
            createExampleColumnDefinitions();

    private static final Long KEY = Long.valueOf(42);

    private static final String GRID_DISPLAY_ID = "example-grid";

    private static final String SESSION_TOKEN = "SESSION_TOKEN";

    private static final Person REGISTRATOR = new Person();

    private static final class ResultSetConfigBuilder
    {
        private final Map<String, IColumnDefinition<DataHolder>> cols;

        private final DefaultResultSetConfig<Long, DataHolder> resultSetConfig;

        private final List<GridColumnFilterInfo<DataHolder>> columnFilters;

        private CustomFilterInfo<DataHolder> customFilter;

        public ResultSetConfigBuilder(List<IColumnDefinition<DataHolder>> columnDefinitions)
        {
            cols = new HashMap<String, IColumnDefinition<DataHolder>>();
            for (IColumnDefinition<DataHolder> def : columnDefinitions)
            {
                cols.put(def.getIdentifier(), def);
            }
            resultSetConfig = new DefaultResultSetConfig<Long, DataHolder>();
            columnFilters = new ArrayList<GridColumnFilterInfo<DataHolder>>();
        }

        ResultSetConfigBuilder customColumn(String code, DataTypeCode dataType)
        {
            return column(new GridCustomColumnDefinition<DataHolder>(new GridCustomColumnInfo(code,
                    code, "", dataType)));
        }

        ResultSetConfigBuilder column(IColumnDefinition<DataHolder> colDef)
        {
            cols.put(colDef.getIdentifier(), colDef);
            return this;
        }

        IResultSetConfig<Long, DataHolder> get()
        {
            resultSetConfig.setAvailableColumns(new LinkedHashSet<IColumnDefinition<DataHolder>>(
                    cols.values()));
            if (columnFilters.isEmpty() == false)
            {
                resultSetConfig.setFilters(GridFilters.createColumnFilter(columnFilters));
            }
            if (customFilter != null)
            {
                resultSetConfig.setFilters(GridFilters.createCustomFilter(customFilter));
            }
            return resultSetConfig;
        }

        ResultSetConfigBuilder displayID(String displayID)
        {
            resultSetConfig.setGridDisplayId(displayID);
            return this;
        }

        ResultSetConfigBuilder offset(int offset)
        {
            resultSetConfig.setOffset(offset);
            return this;
        }

        ResultSetConfigBuilder limit(int limit)
        {
            resultSetConfig.setLimit(limit);
            return this;
        }

        ResultSetConfigBuilder sortAsc(String columnDefinitionID)
        {
            resultSetConfig.setSortInfo(createSortInfo(columnDefinitionID, SortDir.ASC));
            return this;
        }

        ResultSetConfigBuilder sortDesc(String columnDefinitionID)
        {
            resultSetConfig.setSortInfo(createSortInfo(columnDefinitionID, SortDir.DESC));
            return this;
        }

        ResultSetConfigBuilder computeAndCache()
        {
            resultSetConfig.setCacheConfig(ResultSetFetchConfig.<Long> createComputeAndCache());
            return this;
        }

        ResultSetConfigBuilder clearComputeAndCache(Long key)
        {
            resultSetConfig.setCacheConfig(ResultSetFetchConfig
                    .<Long> createClearComputeAndCache(key));
            return this;
        }

        ResultSetConfigBuilder fetchFromCache(Long key)
        {
            resultSetConfig.setCacheConfig(ResultSetFetchConfig.<Long> createFetchFromCache(key));
            return this;
        }

        ResultSetConfigBuilder fetchFromCacheAndRecompute(Long key)
        {
            resultSetConfig.setCacheConfig(ResultSetFetchConfig
                    .<Long> createFetchFromCacheAndRecompute(key));
            return this;
        }

        ResultSetConfigBuilder visibleColumns(String... ids)
        {
            List<IColumnDefinition<DataHolder>> presentedColumns =
                    new ArrayList<IColumnDefinition<DataHolder>>();
            for (String id : ids)
            {
                presentedColumns.add(getDefinition(id));
            }
            resultSetConfig.setPresentedColumns(presentedColumns);
            return this;
        }

        ResultSetConfigBuilder columnFilter(String columnDefinitionID, String filterValue)
        {
            assertEquals(null, customFilter);
            columnFilters.add(new GridColumnFilterInfo<DataHolder>(
                    getDefinition(columnDefinitionID), filterValue));
            return this;
        }

        ResultSetConfigBuilder customFilter(String expression, String... bindings)
        {
            assertEquals(0, columnFilters.size());
            Set<ParameterWithValue> parameters = new LinkedHashSet<ParameterWithValue>();
            for (String binding : bindings)
            {
                int index = binding.indexOf('=');
                String parameter = binding.substring(0, index);
                String value = binding.substring(index + 1);
                parameters.add(new ParameterWithValue(parameter, value));
            }
            customFilter = new CustomFilterInfo<DataHolder>();
            customFilter.setExpression(expression);
            customFilter.setParameters(parameters);
            return this;
        }

        ResultSetConfigBuilder longErrorMessage()
        {
            resultSetConfig.setCustomColumnErrorMessageLong(true);
            return this;
        }

        private SortInfo createSortInfo(String columnDefinitionID, SortDir sortingDirection)
        {
            SortInfo sortInfo = new SortInfo();
            sortInfo.setSortField(getDefinition(columnDefinitionID).getIdentifier());
            sortInfo.setSortDir(sortingDirection);
            return sortInfo;
        }

        private IColumnDefinition<DataHolder> getDefinition(String id)
        {
            IColumnDefinition<DataHolder> def = cols.get(id);
            if (def == null)
            {
                def =
                        new GridCustomColumnDefinition<DataHolder>(new GridCustomColumnInfo(id, id,
                                "", DataTypeCode.VARCHAR));
                cols.put(id, def);
            }
            return def;
        }
    }

    public static <T> GridRowModels<T> createGridRowModels(List<T> entities)
    {
        ArrayList<GridCustomColumnInfo> customColumnsMetadata =
                new ArrayList<GridCustomColumnInfo>();
        ArrayList<ColumnDistinctValues> columnDistinctValues =
                new ArrayList<ColumnDistinctValues>();
        GridRowModels<T> rowModels =
                new GridRowModels<T>(CachedResultSetManagerTest.asRowModel(entities), null,
                        customColumnsMetadata, columnDistinctValues);
        return rowModels;
    }

    private static final class ColumnCalculatorProxy implements
            IColumnCalculator
    {
        private List<String> recordedColumnCodes = new ArrayList<String>();

        @Override
        public <T> List<PrimitiveValue> evalCustomColumn(List<T> data,
                GridCustomColumn customColumn, Set<IColumnDefinition<T>> availableColumns,
                boolean errorMessagesAreLong)
        {
            recordedColumnCodes.add(customColumn.getCode());
            return GridExpressionUtils.evalCustomColumn(
                    TableDataProviderFactory.createDataProvider(data,
                            new ArrayList<IColumnDefinition<T>>(availableColumns)),
                    customColumn,
                    errorMessagesAreLong);
        }

        @Override
        public String toString()
        {
            return recordedColumnCodes.toString();
        }
    }

    private IOriginalDataProvider<DataHolder> originalDataProvider;

    private IResultSetManager<Long> resultSetManager;

    private Mockery context;

    private IResultSetKeyGenerator<Long> keyGenerator;

    private ICustomColumnsProvider customColumnsProvider;

    private ColumnCalculatorProxy columnCalculator;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        originalDataProvider = context.mock(IOriginalDataProvider.class);
        keyGenerator = context.mock(IResultSetKeyGenerator.class);
        customColumnsProvider = context.mock(ICustomColumnsProvider.class);
        columnCalculator = new ColumnCalculatorProxy();
        String managerConfig = "<ehcache name='" + UUID.randomUUID() + "'></ehcache>";
        net.sf.ehcache.CacheManager cacheManager = new net.sf.ehcache.CacheManager(new ByteArrayInputStream(managerConfig.getBytes()));
        TableDataCache<Long, Object> tableDataCache = new TableDataCache<Long, Object>(cacheManager);
        tableDataCache.initCache();

        resultSetManager = new CachedResultSetManager<Long>(tableDataCache, keyGenerator,
                customColumnsProvider, columnCalculator);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetResultFailedForNullArguments()
    {
        try
        {
            resultSetManager.getResultSet(null, null, null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Unspecified result configuration", e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testOffset()
    {
        prepareDataAndCustomColumnDefinitions(20);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);

        builder.fetchFromCache(KEY);
        getAndCheckRows(18, 2, builder.offset(2).limit(-1));
        getAndCheckRows(1, 19, builder.offset(19).limit(-1));
        getAndCheckRows(1, 19, builder.offset(20).limit(-1));
        getAndCheckRows(1, 19, builder.offset(21).limit(-1));
        getAndCheckRows(20, 0, builder.offset(-1).limit(-1));

        context.assertIsSatisfied();
    }

    @Test
    public void testOffsetAndLimit()
    {
        prepareDataAndCustomColumnDefinitions(20, 10);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);

        getAndCheckRows(10, 0, builder.offset(-1).limit(10));

        builder.fetchFromCache(KEY);
        getAndCheckRows(10, 9, builder.offset(9).limit(10));
        getAndCheckRows(10, 10, builder.offset(10).limit(10));
        getAndCheckRows(9, 11, builder.offset(11).limit(10));
        getAndCheckRows(1, 19, builder.offset(19).limit(10));
        getAndCheckRows(1, 19, builder.offset(20).limit(10));
        getAndCheckRows(1, 19, builder.offset(21).limit(10));

        context.assertIsSatisfied();
    }

    private void getAndCheckRows(int expectedSize, int expectedOffset,
            ResultSetConfigBuilder builder)
    {
        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals("size", expectedSize, list.size());
        for (int i = 0; i < list.size(); i++)
        {
            int n = expectedOffset + i;
            assertEquals(i + "th row", n + "-a" + n % 2, list.get(i).getOriginalObject().getData());
        }
    }

    @Test
    public final void testGetResultWithNull()
    {
        prepareDataAndCustomColumnDefinitions(0);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals(0, resultSet.getList().size());
        assertEquals(0, resultSet.getTotalLength());
        assertEquals(KEY, resultSet.getResultSetKey());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRemoveResultSet()
    {
        prepareComputeDataExpectations();
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        builder.fetchFromCache(KEY);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        resultSetManager.removeResultSet(KEY);

        // if data are not in the cache, we compute them again
        prepareComputeDataExpectations();
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        context.assertIsSatisfied();
    }

    private void prepareComputeDataExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(keyGenerator).createKey();
                    will(returnValue(KEY));

                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(Arrays.asList()));

                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));
                }
            });
    }

    @Test
    public final void testRemoveResultSetFailedForNullArgument()
    {
        try
        {
            resultSetManager.removeResultSet(null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Unspecified data key holder.", e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testSorting()
    {
        prepareDataAndCustomColumnDefinitions(3);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.sortAsc("col2");
        IResultSet<Long, DataHolder> data =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals(3, data.getTotalLength());
        GridRowModels<DataHolder> list = data.getList();
        assertEquals(3, list.size());
        assertEquals("0-a0", getData(list, 0));
        assertEquals("2-a0", getData(list, 1));
        assertEquals("1-a1", getData(list, 2));

        builder.fetchFromCache(KEY).sortDesc("col1");
        data = resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals(3, data.getTotalLength());
        list = data.getList();
        assertEquals(3, list.size());
        assertEquals("2-a0", getData(list, 0));
        assertEquals("1-a1", getData(list, 1));
        assertEquals("0-a0", getData(list, 2));

        context.assertIsSatisfied();
    }

    @Test
    public void testComputeAndCache()
    {
        prepareDataAndCustomColumnDefinitions(3);
        IResultSet<Long, DataHolder> data = getDataFirstTime();

        assertEquals(KEY, data.getResultSetKey());
        assertEquals(3, data.getTotalLength());
        GridRowModels<DataHolder> list = data.getList();
        assertEquals(3, list.size());
        assertEquals("0-a0", getData(list, 0));
        assertEquals("1-a1", getData(list, 1));
        assertEquals("2-a0", getData(list, 2));
        assertEquals(0, list.getColumnDistinctValues().size());
        assertEquals(0, list.getCustomColumnsMetadata().size());

        context.assertIsSatisfied();
    }

    @Test
    public void testColumnFilters()
    {
        prepareDataAndCustomColumnDefinitions(40);
        getDataFirstTime();
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.fetchFromCache(KEY).columnFilter("col1", "1").columnFilter("col2", "0");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals(KEY, resultSet.getResultSetKey());
        assertEquals(5, resultSet.getTotalLength());
        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(5, list.size());
        assertEquals("10-a0", getData(list, 0));
        assertEquals("12-a0", getData(list, 1));
        assertEquals("14-a0", getData(list, 2));
        assertEquals("16-a0", getData(list, 3));
        assertEquals("18-a0", getData(list, 4));
        context.assertIsSatisfied();
    }

    @Test
    public void testCustomFilter()
    {
        prepareDataAndCustomColumnDefinitions(40);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.customFilter(
                "toInt(row.col('col1')) >= ${min} and toInt(row.col('col1')) <= ${max}", "min=10",
                "max=12");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals(KEY, resultSet.getResultSetKey());
        assertEquals(3, resultSet.getTotalLength());
        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(3, list.size());
        assertEquals("10-a0", getData(list, 0));
        assertEquals("11-a1", getData(list, 1));
        assertEquals("12-a0", getData(list, 2));
        context.assertIsSatisfied();
    }

    @Test
    public void testDistinctValues()
    {
        prepareDataAndCustomColumnDefinitions(CachedResultSetManager.MAX_DISTINCT_COLUMN_VALUES_SIZE + 1);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.columnFilter("col1", "").columnFilter("col2", "");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        List<ColumnDistinctValues> list = resultSet.getList().getColumnDistinctValues();
        assertEquals(1, list.size());
        ColumnDistinctValues distinctValues = list.get(0);
        assertEquals("col2", distinctValues.getColumnIdentifier());
        assertEquals("[a0, a1]", distinctValues.getDistinctValues().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumnProviding()
    {
        final GridCustomColumn c1 = customColumn("$c1", "1");
        final GridCustomColumn c2 = customColumn("$c2", "2");
        prepareDataAndCustomColumnDefinitions(0, c1, c2);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID);

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        List<GridCustomColumnInfo> metaData = list.getCustomColumnsMetadata();
        assertEquals(2, metaData.size());
        assertEquals("$c1", metaData.get(0).getCode());
        assertEquals("$c2", metaData.get(1).getCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumn()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        prepareDataAndCustomColumnDefinitions(3, c1);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID).visibleColumns("$c1");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(3, list.size());
        assertEquals("0-a0 0", render(list.get(0)));
        assertEquals("1-a1 2", render(list.get(1)));
        assertEquals("2-a0 4", render(list.get(2)));
        assertEquals(DataTypeCode.INTEGER, list.getCustomColumnsMetadata().get(0).getDataType());
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumnWithExpressionBasedOnColumnProperties()
    {
        final GridCustomColumn c1 =
                customColumn("$c1", "toInt(row.colDefs('a')[0].property('a'))/6");
        prepareDataAndCustomColumnDefinitions(3, c1);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID).visibleColumns("$c1");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(3, list.size());
        assertEquals("0-a0 7", render(list.get(0)));
        assertEquals("1-a1 7", render(list.get(1)));
        assertEquals("2-a0 7", render(list.get(2)));
        assertEquals(DataTypeCode.INTEGER, list.getCustomColumnsMetadata().get(0).getDataType());
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSortCustomColumn()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        prepareDataAndCustomColumnDefinitions(3, c1);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID).visibleColumns("$c1").sortDesc("$c1");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(3, list.size());
        assertEquals("2-a0 4", render(list.get(0)));
        assertEquals("1-a1 2", render(list.get(1)));
        assertEquals("0-a0 0", render(list.get(2)));
        assertEquals(DataTypeCode.INTEGER, list.getCustomColumnsMetadata().get(0).getDataType());
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testColumnFilterBasedOnCustomColumn()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        prepareDataAndCustomColumnDefinitions(3, c1);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID).columnFilter("$c1", "2");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(1, list.size());
        assertEquals("1-a1 2", render(list.get(0)));
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomFilterBasedOnCustomColumn()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        prepareDataAndCustomColumnDefinitions(3, c1);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.visibleColumns("$c1").visibleColumns(); // creating an available column
        builder.customFilter("toInt(row.col('$c1')) < ${threshold}", "threshold=3");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(2, list.size());
        assertEquals("0-a0 0", render(list.get(0)));
        assertEquals("1-a1 2", render(list.get(1)));
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomFilterBasedOnNonVisibleCustomColumnWithNotUsedSecondCustomColumn()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        final GridCustomColumn c2 = customColumn("$c2", "toInt(row.col('col1')) * 3");
        prepareDataAndCustomColumnDefinitions(3, c1, c2);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.customColumn("$c1", DataTypeCode.INTEGER);
        builder.customColumn("$c2", DataTypeCode.INTEGER);
        builder.visibleColumns(); // creating an available column
        builder.customFilter("toInt(row.col('$c1')) < ${threshold}", "threshold=3");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(2, list.size());
        assertEquals("0-a0 0", render(list.get(0)));
        assertEquals("1-a1 2", render(list.get(1)));
        assertEquals("[$c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumnCalculationAndCaching()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        final GridCustomColumn c2 = customColumn("$c2", "42");
        prepareDataAndCustomColumnDefinitions(3, c1, c2);
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);

        builder.visibleColumns("$c1");
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        builder.fetchFromCache(KEY);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        builder.visibleColumns("$c2");
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        builder.visibleColumns("$c1");
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals("[$c1, $c2, $c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumnCachingForChangedExpression()
    {
        final GridCustomColumn c1 = customColumn("$c1", "toInt(row.col('col1')) * 2");
        final GridCustomColumn c1a = customColumn("$c1", "42");
        context.checking(new Expectations()
            {
                {
                    one(keyGenerator).createKey();
                    will(returnValue(KEY));

                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(createDataList()));

                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));

                    one(customColumnsProvider).getGridCustomColumn(SESSION_TOKEN, GRID_DISPLAY_ID);
                    will(returnValue(Arrays.asList(c1)));

                    one(customColumnsProvider).getGridCustomColumn(SESSION_TOKEN, GRID_DISPLAY_ID);
                    will(returnValue(Arrays.asList(c1a)));
                }
            });
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        builder.visibleColumns("$c1");
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        builder.fetchFromCache(KEY);
        resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        assertEquals("[$c1, $c1]", columnCalculator.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCustomColumnError()
    {
        final GridCustomColumn c1 = customColumn("$c1", "blabla");
        final GridCustomColumn c2 = customColumn("$c2", "row.col('blabla')");
        prepareDataAndCustomColumnDefinitions(2, c1, c2);
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS);
        builder.displayID(GRID_DISPLAY_ID).visibleColumns("$c1");

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        GridRowModels<DataHolder> list = resultSet.getList();
        assertEquals(2, list.size());
        assertEquals("0-a0 Error. Please contact 'null <null>', who defined this column.",
                render(list.get(0)));
        assertEquals("1-a1 Error. Please contact 'null <null>', who defined this column.",
                render(list.get(1)));
        assertEquals(DataTypeCode.VARCHAR, list.getCustomColumnsMetadata().get(0).getDataType());

        builder.fetchFromCache(KEY).limit(1).visibleColumns("$c2").longErrorMessage();
        resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);

        list = resultSet.getList();
        assertEquals(1, list.size());
        assertEquals(
                "0-a0 Error: (Error occurred in line 1 of the script when evaluating 'row.col('blabla')': "
                        + "java.lang.IllegalArgumentException: "
                        + "java.lang.IllegalArgumentException: Unknown column ID: blabla).",
                render(list.get(0)));

        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleCaching()
    {
        final List<DataHolder> data = createDataList();
        context.checking(new Expectations()
            {
                {
                    one(keyGenerator).createKey();
                    will(returnValue(KEY));

                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(data));

                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));
                }
            });
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS).computeAndCache();

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("alpha", getData(resultSet, 0));
        assertEquals("beta", getData(resultSet, 1));

        builder.fetchFromCache(KEY).offset(1);
        resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("beta", getData(resultSet, 0));

        context.assertIsSatisfied();
    }

    private List<DataHolder> createDataList()
    {
        return createDataList("alpha", "beta");
    }

    private static List<DataHolder> createDataList(String... values)
    {
        List<DataHolder> list = new ArrayList<DataHolder>();
        for (String value : values)
        {
            list.add(new DataHolder(value));
        }
        return list;
    }

    @Test
    public void testCachingSequenceSequenceComputeFetchClearComputeFetch()
    {
        context.checking(new Expectations()
            {
                {
                    one(keyGenerator).createKey();
                    will(returnValue(KEY));

                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(createDataList()));

                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));
                }
            });
        ResultSetConfigBuilder builder = new ResultSetConfigBuilder(COL_DEFS).computeAndCache();

        IResultSet<Long, DataHolder> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("alpha", getData(resultSet, 0));
        assertEquals("beta", getData(resultSet, 1));

        builder.fetchFromCache(KEY).offset(1);
        resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("beta", getData(resultSet, 0));

        context.checking(new Expectations()
            {
                {
                    one(keyGenerator).createKey();
                    will(returnValue(KEY));

                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(createDataList("a", "b")));

                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));
                }
            });
        builder.clearComputeAndCache(KEY).sortDesc("col1");
        resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("a", getData(resultSet, 0));

        builder.fetchFromCacheAndRecompute(KEY).offset(0);
        resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
        assertEquals("b", getData(resultSet, 0));

        context.assertIsSatisfied();
    }

    private static String getData(IResultSet<Long, DataHolder> resultSet, int index)
    {
        GridRowModels<DataHolder> list = resultSet.getList();
        return getData(list, index);
    }

    private static String getData(GridRowModels<DataHolder> list, int index)
    {
        return list.get(index).getOriginalObject().getData();
    }

    private static class DataHolder
    {
        private final String data;

        public DataHolder(String data)
        {
            this.data = data;
        }

        public String getData()
        {
            return data;
        }
    }

    private void prepareDataAndCustomColumnDefinitions(final int size,
            final GridCustomColumn... columns)
    {
        prepareDataAndCustomColumnDefinitions(size, Integer.MAX_VALUE, columns);
    }

    private void prepareDataAndCustomColumnDefinitions(final int size, final int maxSize,
            final GridCustomColumn... columns)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(keyGenerator).createKey();
                    will(returnValue(KEY));

                    DataHolder[] rows = new DataHolder[size];
                    for (int i = 0; i < rows.length; i++)
                    {
                        rows[i] = new DataHolder(i + "-a" + i % 2);
                    }
                    one(originalDataProvider).getOriginalData(Integer.MAX_VALUE);
                    will(returnValue(Arrays.asList(rows)));
                    one(originalDataProvider).getHeaders();
                    will(returnValue(Arrays.asList()));

                    allowing(customColumnsProvider).getGridCustomColumn(SESSION_TOKEN,
                            GRID_DISPLAY_ID);
                    will(returnValue(Arrays.asList(columns)));
                }
            });
    }

    private String render(GridRowModel<DataHolder> model)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(model.getOriginalObject().getData());
        for (PrimitiveValue value : model.getCalculatedColumnValues().values())
        {
            builder.append(" ").append(value);
        }
        return builder.toString();
    }

    private GridCustomColumn customColumn(String code, String expression)
    {
        GridCustomColumn column = new GridCustomColumn();
        column.setCode(code);
        column.setName(code.toLowerCase());
        column.setExpression(expression);
        column.setRegistrator(REGISTRATOR);
        return column;
    }

    private IResultSet<Long, DataHolder> getDataFirstTime()
    {
        ResultSetConfigBuilder builder =
                new ResultSetConfigBuilder(COL_DEFS).displayID(GRID_DISPLAY_ID);
        return resultSetManager.getResultSet(SESSION_TOKEN, builder.get(), originalDataProvider);
    }

    public static <T> List<GridRowModel<T>> asRowModel(List<T> entities)
    {
        List<GridRowModel<T>> list = new ArrayList<GridRowModel<T>>();
        for (T entity : entities)
        {
            list.add(GridRowModel.createWithoutCustomColumns(entity));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static List<IColumnDefinition<DataHolder>> createExampleColumnDefinitions()
    {
        return Arrays.asList(DEF1, DEF2);
    }

    private static IColumnDefinition<DataHolder> createColDef(final String identifier,
            final String separator, final int tokenIndex)
    {
        return new IColumnDefinition<DataHolder>()
            {
                @Override
                public String getValue(GridRowModel<DataHolder> rowModel)
                {
                    return rowModel.getOriginalObject().getData().split(separator)[tokenIndex];
                }

                @Override
                public String getIdentifier()
                {
                    return identifier;
                }

                @Override
                public Comparable<?> tryGetComparableValue(GridRowModel<DataHolder> rowModel)
                {
                    return getValue(rowModel);
                }

                @Override
                public String getHeader()
                {
                    return null; // unused
                }

                @Override
                public DataTypeCode tryToGetDataType()
                {
                    return null;
                }

                @Override
                public String tryToGetProperty(String key)
                {
                    return "a".equals(key) ? "42" : null;
                }

                @Override
                public boolean isCustom()
                {
                    return false;
                }

            };
    }

}
