import _ from 'lodash'
import autoBind from 'auto-bind'
import FileSaver from 'file-saver'
import CsvStringify from 'csv-stringify'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridPagingOptions from '@src/js/components/common/grid/GridPagingOptions.js'
import GridSortingOptions from '@src/js/components/common/grid/GridSortingOptions.js'
import compare from '@src/js/common/compare.js'

export default class GridController {
  constructor() {
    autoBind(this)
  }

  init(context) {
    const props = context.getProps()

    context.initState({
      loaded: false,
      loading: false,
      filters: {},
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
      sort: props.sort,
      sortDirection: props.sortDirection
        ? props.sortDirection
        : GridSortingOptions.ASC,
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
      loading: false,
      loaded: true
    }

    let settings = null

    if (!state.loaded) {
      settings = await this._loadSettings()
      if (settings) {
        newState.pageSize = settings.pageSize || newState.pageSize
        newState.sort = settings.sort || newState.sort
        newState.sortDirection =
          settings.sortDirection || newState.sortDirection
        newState.columnsVisibility =
          settings.columnsVisibility || newState.columnsVisibility
        newState.columnsSorting =
          settings.columnsSorting || newState.columnsSorting
        newState.exportOptions =
          settings.exportOptions || newState.exportOptions
      }
    }

    let result = {}

    if (props.rows) {
      result.rows = props.rows
      result.totalCount = props.rows.length
      result.local = true
    } else if (props.loadRows) {
      const loadedResult = await props.loadRows({
        filters: newState.filters,
        page: newState.page,
        pageSize: newState.pageSize,
        sort: newState.sort,
        sortDirection: newState.sortDirection
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
        newState.filters
      )
      newState.sortedRows = this._sortRows(
        newState.filteredRows,
        newState.allColumns,
        newState.sort,
        newState.sortDirection
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
    return {
      ...column,
      name: column.name,
      label: column.label,
      getValue: column.getValue,
      matches: (row, filter) => {
        function defaultMatches(value, filter) {
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

        const value = column.getValue({ row, column })

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
      compare: (row1, row2) => {
        const defaultCompare = compare
        const value1 = column.getValue({ row: row1, column })
        const value2 = column.getValue({ row: row2, column })
        const { sortDirection } = this.context.getState()

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
      wrappable: column.wrappable === undefined ? true : column.wrappable,
      configurable:
        column.configurable === undefined ? true : column.configurable,
      exportable: column.exportable === undefined ? true : column.exportable
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
    const { loadSettings } = this.context.getProps()

    function getObjectValue(value) {
      return _.isObject(value) ? value : null
    }

    function getArrayValue(value) {
      return _.isArray(value) ? value : null
    }

    function getStringValue(value) {
      return _.isString(value) ? value : null
    }

    function getEnumValue(value, allowedValues) {
      return _.includes(allowedValues, value) ? value : null
    }

    if (loadSettings) {
      const settings = await loadSettings()

      if (!settings || !_.isObject(settings)) {
        return null
      }

      settings.pageSize = getEnumValue(
        settings.pageSize,
        GridPagingOptions.PAGE_SIZE_OPTIONS
      )
      settings.sort = getStringValue(settings.sort)
      settings.sortDirection = getEnumValue(
        settings.sortDirection,
        GridSortingOptions.SORTING_DIRECTION_OPTIONS
      )
      settings.columnsVisibility = getObjectValue(settings.columnsVisibility)
      settings.columnsSorting = getArrayValue(settings.columnsSorting)
      settings.exportOptions = getObjectValue(settings.exportOptions)

      if (settings.exportOptions) {
        const exportOptions = settings.exportOptions

        exportOptions.columns = getEnumValue(
          exportOptions.columns,
          GridExportOptions.COLUMNS_OPTIONS
        )
        exportOptions.rows = getEnumValue(
          exportOptions.rows,
          GridExportOptions.ROWS_OPTIONS
        )
        exportOptions.values = getEnumValue(
          exportOptions.values,
          GridExportOptions.VALUES_OPTIONS
        )

        if (
          exportOptions.columns === null ||
          exportOptions.rows === null ||
          exportOptions.values === null
        ) {
          settings.exportOptions = null
        }
      }

      return settings
    } else {
      return null
    }
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const state = this.context.getState()

      let settings = {
        pageSize: state.pageSize,
        sort: state.sort,
        sortDirection: state.sortDirection,
        columnsVisibility: state.columnsVisibility,
        columnsSorting: state.columnsSorting,
        exportOptions: state.exportOptions
      }

      onSettingsChange(settings)
    }
  }

  _filterRows(rows, columns, columnsVisibility, filters) {
    return _.filter([...rows], row => {
      let matchesAll = true
      columns.forEach(column => {
        let visible = columnsVisibility[column.name]
        if (visible) {
          let filter = filters[column.name]
          if (
            filter !== null &&
            filter !== undefined &&
            filter.trim().length > 0
          ) {
            matchesAll = matchesAll && column.matches(row, filter)
          }
        }
      })
      return matchesAll
    })
  }

  _sortRows(rows, columns, sort, sortDirection) {
    if (sort) {
      const column = _.find(columns, ['name', sort])
      if (column) {
        return rows.sort((t1, t2) => {
          let sign = sortDirection === GridSortingOptions.ASC ? 1 : -1
          return sign * column.compare(t1, t2)
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

  async handleFilterChange(column, filter) {
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

    this.loadTimerId = setTimeout(async () => {
      await this.load()
    }, 500)
  }

  async handleColumnVisibleChange(name) {
    const { allColumns } = this.context.getState()

    const column = _.find(allColumns, column => column.name === name)
    if (!column || !column.configurable) {
      return
    }

    await this.context.setState(state => {
      const newColumnsVisibility = { ...state.columnsVisibility }
      newColumnsVisibility[name] = !newColumnsVisibility[name]

      if (newColumnsVisibility[name]) {
        return {
          columnsVisibility: newColumnsVisibility
        }
      } else {
        const newFilters = { ...state.filters }
        delete newFilters[name]
        return {
          columnsVisibility: newColumnsVisibility,
          filters: newFilters
        }
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

  async handleSortChange(column) {
    if (!column.sortable) {
      return
    }

    await this.context.setState(state => {
      if (column.name === state.sort) {
        return {
          sortDirection:
            state.sortDirection === GridSortingOptions.ASC
              ? GridSortingOptions.DESC
              : GridSortingOptions.ASC
        }
      } else {
        return {
          sort: column.name,
          sortDirection: GridSortingOptions.ASC
        }
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
          var rowValue = column.getValue({ row, column })
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
      if (props.rows) {
        data = state.filteredRows
      } else if (props.loadRows) {
        const loadedResult = await props.loadRows({
          filters: state.filters,
          page: 0,
          pageSize: 1000000,
          sort: state.sort,
          sortDirection: state.sortDirection
        })
        if (_.isArray(loadedResult)) {
          data = loadedResult
        } else {
          data = loadedResult.rows
        }
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

  getAllColumns() {
    const { allColumns, columnsSorting } = this.context.getState()

    let columns = [...allColumns]
    this._sortColumns(columns, columnsSorting)
    return columns
  }

  getVisibleColumns() {
    const { allColumns, columnsSorting, columnsVisibility } =
      this.context.getState()

    let columns = [...allColumns]
    columns = columns.filter(column => columnsVisibility[column.name])
    this._sortColumns(columns, columnsSorting)
    return columns
  }

  getPage() {
    const { page } = this.context.getState()
    return page
  }

  getPageSize() {
    const { pageSize } = this.context.getState()
    return pageSize
  }

  getSort() {
    const { sort } = this.context.getState()
    return sort
  }

  getSortDirection() {
    const { sortDirection } = this.context.getState()
    return sortDirection
  }

  getFilters() {
    const { filters } = this.context.getState()
    return filters
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
}
