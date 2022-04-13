import _ from 'lodash'
import autoBind from 'auto-bind'
import FileSaver from 'file-saver'
import CsvStringify from 'csv-stringify'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridPagingOptions from '@src/js/components/common/grid/GridPagingOptions.js'
import GridSortingOptions from '@src/js/components/common/grid/GridSortingOptions.js'
import compare from '@src/js/common/compare.js'

const LOCAL_GRID_RELOAD_PERIOD = 200
const REMOTE_GRID_RELOAD_PERIOD = 500

export default class GridController {
  constructor() {
    autoBind(this)
    this.cache = {}
  }

  init(context) {
    const props = context.getProps()

    let filterMode = GridFilterOptions.GLOBAL_FILTER
    if (props.filterModes) {
      filterMode = this._getEnumValue(filterMode, props.filterModes)
      if (!filterMode) {
        filterMode = props.filterModes.length > 0 ? props.filterModes[0] : null
      }
    }

    let sortings = []

    if (props.sort) {
      sortings.push({
        columnName: props.sort,
        sortDirection: props.sortDirection
          ? props.sortDirection
          : GridSortingOptions.ASC
      })
    }

    context.initState({
      loaded: false,
      loading: false,
      filterMode: filterMode,
      filters: {},
      globalFilter: {
        operator: GridFilterOptions.OPERATOR_AND,
        text: null
      },
      page: 0,
      pageSize: 10,
      columnsVisibility: {},
      columnsSorting: [],
      allColumns: [],
      local: null,
      rows: [],
      filteredRows: [],
      sortedRows: [],
      allRows: [],
      selectedRow: null,
      multiselectedRows: {},
      heights: {},
      sortings: sortings,
      totalCount: 0,
      exportOptions: {
        columns: GridExportOptions.VISIBLE_COLUMNS,
        rows: GridExportOptions.CURRENT_PAGE,
        values: GridExportOptions.RICH_TEXT
      }
    })
    this.context = context
  }

  async load() {
    const props = this.context.getProps()

    if ((props.rows && props.loadRows) || (!props.rows && !props.loadRows)) {
      throw new Error(
        'Incorrect grid configuration. Please set "rows" or "loadRows" property.'
      )
    }

    if (
      (props.columns && props.loadColumns) ||
      (!props.columns && !props.loadColumns)
    ) {
      throw new Error(
        'Incorrect grid configuration. Please set "columns" or "loadColumns" property.'
      )
    }

    await this.context.setState(() => ({
      loading: true
    }))

    const state = this.context.getState()
    const newState = {
      ...state,
      heights: {},
      loading: false,
      loaded: true
    }

    let settings = null

    if (!state.loaded) {
      settings = await this._loadSettings()
      _.merge(newState, settings)
    }

    let result = {}

    if (props.rows) {
      result.rows = props.rows
      result.totalCount = props.rows.length
      result.local = true
    } else if (props.loadRows) {
      const columns = {}

      newState.allColumns.forEach(column => {
        columns[column.name] = column
      })

      const loadedResult = await props.loadRows({
        columns: columns,
        filterMode: newState.filterMode,
        filters: newState.filters,
        globalFilter: newState.globalFilter,
        page: newState.page,
        pageSize: newState.pageSize,
        sortings: newState.sortings
      })
      if (_.isArray(loadedResult)) {
        result.rows = loadedResult
        result.totalCount = loadedResult.length
        result.local = true
      } else {
        result.rows = loadedResult.rows
        result.totalCount = loadedResult.totalCount
        result.local = false
      }
    }

    newState.local = result.local

    if (result.local) {
      const { newAllColumns, newColumnsVisibility, newColumnsSorting } =
        await this._loadColumns(
          result.rows,
          newState.columnsVisibility,
          newState.columnsSorting
        )

      newState.allColumns = newAllColumns
      newState.columnsVisibility = newColumnsVisibility
      newState.columnsSorting = newColumnsSorting

      newState.allRows = result.rows
      newState.filteredRows = this._filterRows(
        newState.allRows,
        newState.allColumns,
        newState.columnsVisibility,
        newState.filterMode,
        newState.filters,
        newState.globalFilter
      )
      newState.sortedRows = this._sortRows(
        newState.filteredRows,
        newState.allColumns,
        newState.sortings
      )
      newState.totalCount = newState.filteredRows.length

      const pageCount = Math.max(
        Math.ceil(newState.totalCount / newState.pageSize),
        1
      )

      newState.page = Math.min(newState.page, pageCount - 1)
      newState.rows = this._pageRows(
        newState.sortedRows,
        newState.page,
        newState.pageSize
      )
    } else {
      newState.allRows = result.rows
      newState.filteredRows = result.rows
      newState.sortedRows = result.rows
      newState.rows = result.rows
      newState.totalCount = result.totalCount

      const pageCount = Math.max(
        Math.ceil(result.totalCount / newState.pageSize),
        1
      )
      newState.page = Math.min(newState.page, pageCount - 1)

      const { newAllColumns, newColumnsVisibility, newColumnsSorting } =
        await this._loadColumns(
          newState.rows,
          newState.columnsVisibility,
          newState.columnsSorting
        )

      newState.allColumns = newAllColumns
      newState.columnsVisibility = newColumnsVisibility
      newState.columnsSorting = newColumnsSorting
    }

    // do not update filters (this would override filter changes that a user could do while grid was loading)
    delete newState.filters
    delete newState.globalFilter

    await this.context.setState(newState)

    if (!state.loaded) {
      this.selectRow(props.selectedRowId)
      this.multiselectRows(props.multiselectedRowIds)
    } else {
      this.selectRow(newState.selectedRow ? newState.selectedRow.id : null)
      this.multiselectRows(Object.keys(newState.multiselectedRows))
    }
  }

  async _loadColumns(rows, columnsVisibility, columnsSorting) {
    const props = this.context.getProps()
    const state = this.context.getState()

    let newAllColumns = []
    const newColumnsVisibility = { ...columnsVisibility }
    const newColumnsSorting = [...columnsSorting]

    if (props.columns) {
      newAllColumns = props.columns
    } else if (props.loadColumns) {
      newAllColumns = await props.loadColumns(rows)
    }

    newAllColumns = newAllColumns.map(newColumn => {
      if (!newColumn.name) {
        throw new Error('column.name cannot be empty')
      }
      if (newColumn.exportable && !newColumn.getValue) {
        throw new Error(
          'column.name cannot be exportable without getValue implementation'
        )
      }
      return this._loadColumn(newColumn)
    })

    // If there is a filter value defined for a column and this column does not exist
    // in the new columns list then take it over from the previous columns list.
    // This may happen e.g. when a user is filtering by a dynamic column
    // and enters a filter value that does not match any row. Without this trick the column
    // would disappear and the user would not be able to clear the filter value.

    Object.keys(state.filters).forEach(columnName => {
      const newColumn = _.find(
        newAllColumns,
        newColumn => newColumn.name === columnName
      )

      if (!newColumn) {
        const existingColumn = _.find(
          state.allColumns,
          column => column.name === columnName
        )
        newAllColumns.push(existingColumn)
      }
    })

    newAllColumns.forEach((newColumn, newColumnIndex) => {
      let newColumnVisibility = newColumnsVisibility[newColumn.name]

      if (newColumnVisibility === undefined || !newColumn.configurable) {
        newColumnsVisibility[newColumn.name] = newColumn.visible
      }

      let newColumnSorting = _.findIndex(
        newColumnsSorting,
        columnName => columnName === newColumn.name
      )

      if (newColumnSorting === -1) {
        // If a column does not have a sorting value yet, then set its sorting to
        // the max sorting of the columns that were before it in the columns list

        newColumnSorting = newAllColumns
          .slice(0, newColumnIndex)
          .reduce((maxSorting, column) => {
            const sorting = _.findIndex(
              newColumnsSorting,
              columnName => columnName === column.name
            )
            return Math.max(sorting, maxSorting)
          }, -1)
        newColumnsSorting.splice(newColumnSorting + 1, 0, newColumn.name)
      }
    })

    return { newAllColumns, newColumnsVisibility, newColumnsSorting }
  }

  _loadColumn(column) {
    const defaultMatches = function (value, filter) {
      if (filter) {
        return value !== null && value !== undefined
          ? String(value)
              .trim()
              .toUpperCase()
              .includes(filter.trim().toUpperCase())
          : false
      } else {
        return true
      }
    }

    const defaultCompare = compare

    return {
      ...column,
      name: column.name,
      label: column.label,
      getValue: column.getValue,
      matches: (row, filter) => {
        const value = column.getValue({ row, column, operation: 'match' })

        if (column.matchesValue) {
          return column.matchesValue({
            value,
            row,
            column,
            filter,
            defaultMatches
          })
        } else {
          return defaultMatches(value, filter)
        }
      },
      compare: (row1, row2, sortDirection) => {
        const value1 = column.getValue({
          row: row1,
          column,
          operation: 'compare'
        })
        const value2 = column.getValue({
          row: row2,
          column,
          operation: 'compare'
        })

        if (column.compareValue) {
          return column.compareValue({
            value1,
            value2,
            row1,
            row2,
            column,
            sortDirection,
            defaultCompare
          })
        } else {
          return defaultCompare(value1, value2)
        }
      },
      sortable: column.sortable === undefined ? true : column.sortable,
      filterable: column.filterable === undefined ? true : column.filterable,
      visible: column.visible === undefined ? true : column.visible,
      configurable:
        column.configurable === undefined ? true : column.configurable,
      exportable: column.exportable === undefined ? true : column.exportable,
      nowrap: column.nowrap === undefined ? false : column.nowrap,
      truncate: column.truncate === undefined ? false : column.truncate,
      metadata: column.metadata === undefined ? {} : column.metadata
    }
  }

  _sortColumns(columns, columnsSorting) {
    columns.sort((c1, c2) => {
      const c1Index = _.findIndex(
        columnsSorting,
        columnName => columnName === c1.name
      )
      const c2Index = _.findIndex(
        columnsSorting,
        columnName => columnName === c2.name
      )
      return c1Index - c2Index
    })
  }

  async _loadSettings() {
    const props = this.context.getProps()

    if (!props.loadSettings) {
      return {}
    }

    const loaded = await props.loadSettings()

    if (!loaded || !_.isObject(loaded)) {
      return {}
    }

    const settings = {}

    settings.filterMode = this._getEnumValue(
      loaded.filterMode,
      GridFilterOptions.FILTER_MODE_OPTIONS
    )

    if (props.filterModes) {
      settings.filterMode = this._getEnumValue(
        settings.filterMode,
        props.filterModes
      )
    }

    if (_.isObject(loaded.globalFilter)) {
      const globalFilter = {}

      globalFilter.operator = this._getEnumValue(
        loaded.globalFilter.operator,
        GridFilterOptions.OPERATOR_OPTIONS
      )

      if (globalFilter.operator !== undefined) {
        settings.globalFilter = globalFilter
      }
    }

    settings.pageSize = this._getEnumValue(
      loaded.pageSize,
      GridPagingOptions.PAGE_SIZE_OPTIONS
    )

    if (_.isArray(loaded.sortings)) {
      const sortings = []
      loaded.sortings.forEach(loadedSorting => {
        if (_.isObject(loadedSorting)) {
          const sorting = {}
          sorting.columnName = this._getStringValue(loadedSorting.columnName)
          sorting.sortDirection = this._getEnumValue(
            loadedSorting.sortDirection,
            GridSortingOptions.SORTING_DIRECTION_OPTIONS
          )
          if (
            sorting.columnName !== undefined &&
            sorting.sortDirection !== undefined
          ) {
            sortings.push(sorting)
          }
        }
      })
      if (sortings.length > 0) {
        settings.sortings = sortings
      }
    }

    if (settings.sortings === undefined) {
      const sort = this._getStringValue(loaded.sort)
      const sortDirection = this._getEnumValue(
        loaded.sortDirection,
        GridSortingOptions.SORTING_DIRECTION_OPTIONS
      )
      if (sort !== undefined && sortDirection !== undefined) {
        settings.sortings = [
          {
            columnName: sort,
            sortDirection: sortDirection
          }
        ]
      }
    }

    settings.columnsVisibility = this._getObjectValue(loaded.columnsVisibility)
    settings.columnsSorting = this._getArrayValue(loaded.columnsSorting)

    if (_.isObject(loaded.exportOptions)) {
      const exportOptions = {}

      exportOptions.columns = this._getEnumValue(
        loaded.exportOptions.columns,
        GridExportOptions.COLUMNS_OPTIONS
      )
      exportOptions.rows = this._getEnumValue(
        loaded.exportOptions.rows,
        GridExportOptions.ROWS_OPTIONS
      )
      exportOptions.values = this._getEnumValue(
        loaded.exportOptions.values,
        GridExportOptions.VALUES_OPTIONS
      )

      if (
        exportOptions.columns !== undefined &&
        exportOptions.rows !== undefined &&
        exportOptions.values !== undefined
      ) {
        settings.exportOptions = exportOptions
      }
    }

    return settings
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const state = this.context.getState()

      let settings = {
        filterMode: state.filterMode,
        globalFilter: {
          operator: state.globalFilter.operator
        },
        pageSize: state.pageSize,
        sortings: state.sortings,
        columnsVisibility: state.columnsVisibility,
        columnsSorting: state.columnsSorting,
        exportOptions: state.exportOptions
      }

      onSettingsChange(settings)
    }
  }

  _filterRows(
    rows,
    columns,
    columnsVisibility,
    filterMode,
    filters,
    globalFilter
  ) {
    if (filterMode === GridFilterOptions.GLOBAL_FILTER) {
      if (this._isEmpty(globalFilter.text)) {
        return rows
      }

      const tokens = this._split(globalFilter.text)

      return _.filter([...rows], row => {
        let rowMatches = null

        if (globalFilter.operator === GridFilterOptions.OPERATOR_AND) {
          rowMatches = true
        } else if (globalFilter.operator === GridFilterOptions.OPERATOR_OR) {
          rowMatches = false
        }

        tokens: for (let t = 0; t < tokens.length; t++) {
          let token = tokens[t]
          let rowMatchesToken = false

          columns: for (let c = 0; c < columns.length; c++) {
            let column = columns[c]
            let visible = columnsVisibility[column.name]

            if (visible) {
              rowMatchesToken = column.matches(row, token)
              if (rowMatchesToken) {
                break columns
              }
            }
          }

          if (globalFilter.operator === GridFilterOptions.OPERATOR_AND) {
            rowMatches = rowMatches && rowMatchesToken
            if (!rowMatches) {
              break tokens
            }
          } else if (globalFilter.operator === GridFilterOptions.OPERATOR_OR) {
            rowMatches = rowMatches || rowMatchesToken
            if (rowMatches) {
              break tokens
            }
          }
        }

        return rowMatches
      })
    } else if (filterMode === GridFilterOptions.COLUMN_FILTERS) {
      return _.filter([...rows], row => {
        let matchesAll = true
        columns.forEach(column => {
          let visible = columnsVisibility[column.name]
          if (visible) {
            let filter = filters[column.name]
            if (!this._isEmpty(filter)) {
              matchesAll = matchesAll && column.matches(row, filter)
            }
          }
        })
        return matchesAll
      })
    } else {
      return rows
    }
  }

  _sortRows(rows, columns, sortings) {
    if (sortings && sortings.length > 0) {
      const columnSortings = []

      sortings.forEach(sorting => {
        const column = _.find(columns, ['name', sorting.columnName])
        if (column) {
          columnSortings.push({
            column,
            sorting
          })
        }
      })

      if (columnSortings.length > 0) {
        return rows.sort((t1, t2) => {
          let result = 0
          let index = 0
          while (index < columnSortings.length && result === 0) {
            const { column, sorting } = columnSortings[index]
            const sign =
              sorting.sortDirection === GridSortingOptions.ASC ? 1 : -1
            result = sign * column.compare(t1, t2, sorting.sortDirection)
            index++
          }
          return result
        })
      }
    }

    return rows
  }

  _pageRows(rows, page, pageSize) {
    return rows.slice(
      page * pageSize,
      Math.min(rows.length, (page + 1) * pageSize)
    )
  }

  async selectRow(newSelectedRowId) {
    const { selectable, onSelectedRowChange } = this.context.getProps()
    const { allRows, rows, selectedRow } = this.context.getState()

    if (!selectable) {
      return
    }

    let newSelectedRow = null

    if (newSelectedRowId !== null && newSelectedRowId !== undefined) {
      const data = _.find(allRows, row => row.id === newSelectedRowId)
      const visible =
        _.findIndex(rows, row => row.id === newSelectedRowId) !== -1

      newSelectedRow = {
        id: newSelectedRowId,
        data,
        visible
      }
    }

    if (!_.isEqual(selectedRow, newSelectedRow)) {
      await this.context.setState(() => ({
        selectedRow: newSelectedRow
      }))

      if (onSelectedRowChange) {
        onSelectedRowChange(newSelectedRow)
      }
    }
  }

  async multiselectRows(newMultiselectedRowIds) {
    const { multiselectable, onMultiselectedRowsChange } =
      this.context.getProps()
    const { local, allRows, rows, multiselectedRows } = this.context.getState()

    if (!multiselectable) {
      return
    }

    const newMultiselectedRows = {}

    if (newMultiselectedRowIds && newMultiselectedRowIds.length > 0) {
      const allRowsMap = {}
      allRows.forEach(row => {
        allRowsMap[row.id] = row
      })

      const rowsMap = {}
      rows.forEach(row => {
        rowsMap[row.id] = row
      })

      newMultiselectedRowIds.forEach(rowId => {
        if (rowId !== null && rowId !== undefined) {
          const visible = rowsMap[rowId] !== undefined
          let data = allRowsMap[rowId]

          if (data) {
            newMultiselectedRows[rowId] = {
              id: rowId,
              data,
              visible
            }
          } else if (!local) {
            const multiselectedRow = multiselectedRows[rowId]
            if (multiselectedRow) {
              data = multiselectedRow.data
            }
            newMultiselectedRows[rowId] = {
              id: rowId,
              data,
              visible
            }
          }
        }
      })
    }

    await this.context.setState(() => ({
      multiselectedRows: newMultiselectedRows
    }))

    if (onMultiselectedRowsChange) {
      onMultiselectedRowsChange(newMultiselectedRows)
    }
  }

  async showRow(rowId) {
    const { sortedRows, page, pageSize } = this.context.getState()

    if (!rowId) {
      return
    }

    const index = _.findIndex(sortedRows, ['id', rowId])

    if (index === -1) {
      return
    }

    const newPage = Math.floor(index / pageSize)

    if (newPage !== page) {
      await this.context.setState({
        page: newPage
      })
      await this.load()
    }
  }

  async handleFilterModeChange(filterMode) {
    await this.context.setState({
      filterMode
    })
    await this.load()
    await this._saveSettings()
  }

  async handleFilterChange(column, filter) {
    const { local } = this.context.getState()

    await this.context.setState(state => {
      const newFilters = {
        ...state.filters
      }

      if (filter && _.trim(filter).length > 0) {
        newFilters[column] = filter
      } else {
        delete newFilters[column]
      }

      return {
        page: 0,
        filters: newFilters
      }
    })

    if (this.loadTimerId) {
      clearTimeout(this.loadTimerId)
      this.loadTimerId = null
    }

    this.loadTimerId = setTimeout(
      async () => {
        await this.load()
      },
      local ? LOCAL_GRID_RELOAD_PERIOD : REMOTE_GRID_RELOAD_PERIOD
    )
  }

  async handleGlobalFilterChange(newGlobalFilter) {
    const { local, globalFilter } = this.context.getState()

    await this.context.setState(() => ({
      page: 0,
      globalFilter: newGlobalFilter
    }))

    if (this.loadTimerId) {
      clearTimeout(this.loadTimerId)
      this.loadTimerId = null
    }

    this.loadTimerId = setTimeout(
      async () => {
        await this.load()
      },
      local ? LOCAL_GRID_RELOAD_PERIOD : REMOTE_GRID_RELOAD_PERIOD
    )

    if (globalFilter.operator !== newGlobalFilter.operator) {
      await this._saveSettings()
    }
  }

  async handleColumnVisibleChange(visibilityMap) {
    const { allColumns } = this.context.getState()

    allColumns.forEach(column => {
      if (!column.configurable) {
        delete visibilityMap[column.name]
      }
    })

    await this.context.setState(state => {
      const newColumnsVisibility = {
        ...state.columnsVisibility,
        ...visibilityMap
      }
      const newFilters = { ...state.filters }

      Object.keys(visibilityMap).forEach(columnName => {
        const visible = visibilityMap[columnName]
        if (!visible) {
          delete newFilters[columnName]
        }
      })

      return {
        columnsVisibility: newColumnsVisibility,
        filters: newFilters
      }
    })

    await this.load()
    await this._saveSettings()
  }

  async handleColumnOrderChange(sourceIndex, destinationIndex) {
    await this.context.setState(state => {
      const columns = this.getAllColumns()
      const sourceColumn = columns[sourceIndex]
      const destinationColumn = columns[destinationIndex]

      const sourceSorting = _.findIndex(
        state.columnsSorting,
        columnName => columnName === sourceColumn.name
      )
      const destinationSorting = _.findIndex(
        state.columnsSorting,
        columnName => columnName === destinationColumn.name
      )

      const newColumnsSorting = [...state.columnsSorting]
      newColumnsSorting.splice(sourceSorting, 1)
      newColumnsSorting.splice(destinationSorting, 0, sourceColumn.name)

      return {
        columnsSorting: newColumnsSorting
      }
    })

    await this.load()
    await this._saveSettings()
  }

  async handleSortChange(column, append) {
    if (!column.sortable) {
      return
    }

    function createInitialSorting(column) {
      return {
        columnName: column.name,
        sortDirection: GridSortingOptions.ASC
      }
    }

    function createReversedSorting(column, sorting) {
      return {
        columnName: column.name,
        sortDirection:
          sorting.sortDirection === GridSortingOptions.ASC
            ? GridSortingOptions.DESC
            : GridSortingOptions.ASC
      }
    }

    await this.context.setState(state => {
      const newSortings = []

      const index = _.findIndex(
        state.sortings,
        sorting => sorting.columnName === column.name
      )
      const sorting = state.sortings[index]

      if (append) {
        if (index !== -1) {
          newSortings.push(...state.sortings)
          newSortings.splice(index, 1)
        } else {
          newSortings.push(...state.sortings)
          newSortings.push(createInitialSorting(column))
        }
      } else {
        if (index !== -1) {
          newSortings.push(...state.sortings)
          newSortings[index] = createReversedSorting(column, sorting)
        } else {
          newSortings.push(createInitialSorting(column))
        }
      }

      return {
        page: 0,
        sortings: newSortings
      }
    })

    await this.load()
    await this._saveSettings()
  }

  async handlePageChange(page) {
    await this.context.setState(() => ({
      page
    }))
    await this.load()
  }

  async handlePageSizeChange(pageSize) {
    await this.context.setState(() => ({
      page: 0,
      pageSize
    }))
    await this.load()
    await this._saveSettings()
  }

  async handleRowClick(row) {
    const { onRowClick } = this.context.getProps()
    if (onRowClick) {
      onRowClick({
        id: row.id,
        data: row,
        visible: true
      })
    }
  }

  async handleRowSelect(row) {
    await this.selectRow(row ? row.id : null)
  }

  async handleRowMultiselect(row) {
    const { multiselectedRows } = this.context.getState()

    if (row) {
      const newMultiselectedRows = { ...multiselectedRows }

      if (newMultiselectedRows[row.id]) {
        delete newMultiselectedRows[row.id]
      } else {
        newMultiselectedRows[row.id] = true
      }

      await this.multiselectRows(Object.keys(newMultiselectedRows))
    }
  }

  async handleMultiselectAllRowsChange() {
    const { rows, multiselectedRows } = this.context.getState()

    const rowIds = rows.map(row => String(row.id))
    const multiselectedRowIds = Object.keys(multiselectedRows)

    let newMultiselectedRowIds = null
    if (_.difference(rowIds, multiselectedRowIds).length === 0) {
      newMultiselectedRowIds = _.difference(multiselectedRowIds, rowIds)
    } else {
      newMultiselectedRowIds = _.union(multiselectedRowIds, rowIds)
    }
    this.multiselectRows(newMultiselectedRowIds)
  }

  async handleMultiselectionClear() {
    this.multiselectRows([])
  }

  async handleExecuteAction(action) {
    if (action && action.execute) {
      const { multiselectedRows } = this.context.getState()
      action.execute({ multiselectedRows })
    }
  }

  async handleExport() {
    const { exportOptions } = this.context.getState()

    function _stringToUtf16ByteArray(str) {
      var bytes = []
      bytes.push(255, 254)
      for (var i = 0; i < str.length; ++i) {
        var charCode = str.charCodeAt(i)
        bytes.push(charCode & 0xff) //low byte
        bytes.push((charCode & 0xff00) >>> 8) //high byte (might be 0)
      }
      return bytes
    }

    function _exportColumnsFromData(namePrefix, rows, columns) {
      const arrayOfRowArrays = []

      const headers = columns.map(column => column.name)
      arrayOfRowArrays.push(headers)

      rows.forEach(row => {
        var rowAsArray = []
        columns.forEach(column => {
          var rowValue = column.getValue({
            row,
            column,
            operation: 'export',
            exportOptions
          })
          if (!rowValue) {
            rowValue = ''
          } else {
            var specialCharsRemover = document.createElement('textarea')
            specialCharsRemover.innerHTML = rowValue
            rowValue = specialCharsRemover.value //Removes special HTML Chars
            rowValue = String(rowValue).replace(/\r?\n|\r|\t/g, ' ') //Remove carriage returns and tabs

            if (exportOptions.values === GridExportOptions.RICH_TEXT) {
              // do nothing with the value
            } else if (exportOptions.values === GridExportOptions.PLAIN_TEXT) {
              rowValue = String(rowValue).replace(/<(?:.|\n)*?>/gm, '')
            } else {
              throw Error('Unsupported values option: ' + exportOptions.values)
            }
          }
          rowAsArray.push(rowValue)
        })
        arrayOfRowArrays.push(rowAsArray)
      })

      CsvStringify(
        {
          header: false,
          delimiter: '\t',
          quoted: false
        },
        arrayOfRowArrays,
        function (err, tsv) {
          var utf16bytes = _stringToUtf16ByteArray(tsv)
          var utf16bytesArray = new Uint8Array(utf16bytes.length)
          utf16bytesArray.set(utf16bytes, 0)
          var blob = new Blob([utf16bytesArray], {
            type: 'text/tsv;charset=UTF-16LE;'
          })
          FileSaver.saveAs(blob, 'exportedTable' + namePrefix + '.tsv')
        }
      )
    }

    const state = this.context.getState()
    const props = this.context.getProps()

    var data = []
    var columns = []
    var prefix = ''

    if (exportOptions.columns === GridExportOptions.ALL_COLUMNS) {
      columns = this.getAllColumns()
      prefix += 'AllColumns'
    } else if (exportOptions.columns === GridExportOptions.VISIBLE_COLUMNS) {
      columns = this.getVisibleColumns()
      prefix += 'VisibleColumns'
    } else {
      throw Error('Unsupported columns option: ' + exportOptions.columns)
    }

    columns = columns.filter(column => column.exportable)

    if (exportOptions.rows === GridExportOptions.ALL_PAGES) {
      if (state.local) {
        data = state.sortedRows
      } else if (props.loadRows) {
        const loadedResult = await props.loadRows({
          filters: state.filters,
          globalFilter: state.globalFilter,
          page: 0,
          pageSize: 1000000,
          sortings: state.sortings
        })
        data = loadedResult.rows
      }

      prefix += 'AllPages'
      _exportColumnsFromData(prefix, data, columns)
    } else if (exportOptions.rows === GridExportOptions.CURRENT_PAGE) {
      data = state.rows
      prefix += 'CurrentPage'
      _exportColumnsFromData(prefix, data, columns)
    } else if (exportOptions.rows === GridExportOptions.SELECTED_ROWS) {
      data = Object.values(state.multiselectedRows).map(
        selectedRow => selectedRow.data
      )
      prefix += 'SelectedRows'
      _exportColumnsFromData(prefix, data, columns)
    } else {
      throw Error('Unsupported rows option: ' + exportOptions.columns)
    }
  }

  async handleExportOptionsChange(exportOptions) {
    await this.context.setState(() => ({
      exportOptions
    }))
    await this._saveSettings()
  }

  async handleMeasured(cellRef, column, row) {
    if (!this.measureQueue) {
      this.measureQueue = []
    }

    this.measureQueue.push({
      cellRef,
      column,
      row
    })

    if (this.measureTimeoutId) {
      clearTimeout(this.measureTimeoutId)
    }

    this.measureTimeoutId = setTimeout(() => {
      this.context.setState(state => {
        const heights = state.heights
        let newHeights = heights

        this.measureQueue.forEach(measureItem => {
          const rowHeights = heights[measureItem.row.id]
          let newRowHeights = newHeights[measureItem.row.id] || rowHeights

          if (measureItem.cellRef.current) {
            const height = rowHeights
              ? rowHeights[measureItem.column.name]
              : null
            const newHeight = measureItem.cellRef.current.scrollHeight

            if (newHeight !== height) {
              if (newHeights === heights) {
                newHeights = {
                  ...heights
                }
              }
              if (newRowHeights === rowHeights) {
                newRowHeights = {
                  ...rowHeights
                }
                newHeights[measureItem.row.id] = newRowHeights
              }
              newRowHeights[measureItem.column.name] = newHeight
            }
          }
        })
        return {
          heights: newHeights
        }
      })
      this.measureQueue = []
    }, 500)
  }

  getAllColumns() {
    const { allColumns, columnsSorting } = this.context.getState()

    let columns = [...allColumns]
    this._sortColumns(columns, columnsSorting)

    return this._getCachedValue('allColumns', columns)
  }

  getVisibleColumns() {
    const { allColumns, columnsSorting, columnsVisibility } =
      this.context.getState()

    let columns = [...allColumns]
    columns = columns.filter(column => columnsVisibility[column.name])
    this._sortColumns(columns, columnsSorting)

    return this._getCachedValue('visibleColumns', columns)
  }

  getPage() {
    const { page } = this.context.getState()
    return page
  }

  getPageSize() {
    const { pageSize } = this.context.getState()
    return pageSize
  }

  getSortings() {
    const { sortings } = this.context.getState()
    return sortings
  }

  getFilters() {
    const { filters } = this.context.getState()
    return filters
  }

  getGlobalFilter() {
    const { globalFilter } = this.context.getState()
    return globalFilter
  }

  getRows() {
    const { rows } = this.context.getState()
    return rows
  }

  getSelectedRow() {
    const { selectedRow } = this.context.getState()
    return selectedRow
  }

  getMultiselectedRows() {
    const { multiselectedRows } = this.context.getState()
    return multiselectedRows
  }

  getTotalCount() {
    const { totalCount } = this.context.getState()
    return totalCount
  }

  _getCachedValue(key, newValue) {
    if (_.isEqual(this.cache[key], newValue)) {
      return this.cache[key]
    } else {
      this.cache[key] = newValue
      return newValue
    }
  }

  _getObjectValue(value) {
    return _.isObject(value) ? value : undefined
  }

  _getArrayValue(value) {
    return _.isArray(value) ? value : undefined
  }

  _getStringValue(value) {
    return _.isString(value) ? value : undefined
  }

  _getEnumValue(value, allowedValues) {
    return _.includes(allowedValues, value) ? value : undefined
  }

  _isEmpty(value) {
    return (
      value === null ||
      value === undefined ||
      (_.isString(value) && value.trim().length === 0)
    )
  }

  _split(str) {
    return str.split(' ').filter(token => !this._isEmpty(token))
  }
}
