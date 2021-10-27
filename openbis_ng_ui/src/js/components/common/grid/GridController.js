import _ from 'lodash'
import autoBind from 'auto-bind'
import compare from '@src/js/common/compare.js'

export default class GridController {
  constructor() {
    autoBind(this)
  }

  init(context) {
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
      sort: null,
      sortDirection: null,
      totalCount: 0
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

      if (!state.loaded && !settings) {
        newState.allColumns.forEach(column => {
          if (column.sort) {
            newState.sort = column.name
            newState.sortDirection = column.sort
          }
        })
      }

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
    const { columns, loadColumns } = this.context.getProps()

    let newAllColumns = []
    const newColumnsVisibility = { ...columnsVisibility }
    const newColumnsSorting = [...columnsSorting]

    if (columns) {
      newAllColumns = columns
    } else if (loadColumns) {
      newAllColumns = await loadColumns(rows)
    }

    newAllColumns = newAllColumns.map(newColumn => {
      if (!newColumn.name) {
        throw new Error('column.name cannot be empty')
      }
      return this._loadColumn(newColumn)
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
      configurable:
        column.configurable === undefined ? true : column.configurable
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

    if (loadSettings) {
      return await loadSettings()
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
        columnsSorting: state.columnsSorting
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
          let sign = sortDirection === 'asc' ? 1 : -1
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
          sortDirection: state.sortDirection === 'asc' ? 'desc' : 'asc'
        }
      } else {
        return {
          sort: column.name,
          sortDirection: 'asc'
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

  async handleSelectAllRowsChange() {
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

  async handleExecuteAction(action) {
    if (action && action.execute) {
      const { multiselectedRows } = this.context.getState()
      action.execute({ multiselectedRows })
    }
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
