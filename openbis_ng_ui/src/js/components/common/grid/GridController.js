import _ from 'lodash'
import autoBind from 'auto-bind'
import openbis from '@src/js/services/openbis.js'
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
      columns: [],
      rows: [],
      allRows: [],
      selectedRowId: null,
      sort: null,
      sortDirection: null,
      totalCount: 0
    })
    this.context = context
  }

  async _initColumns(rows) {
    const { columns, loadColumns } = this.context.getProps()
    const { columnsVisibility, columnsSorting } = this.context.getState()

    let newColumns = []
    const newColumnsVisibility = { ...columnsVisibility }
    const newColumnsSorting = [...columnsSorting]

    if (columns) {
      newColumns = columns
    } else if (loadColumns) {
      newColumns = await loadColumns(rows)
    }

    newColumns = newColumns.map(newColumn => {
      if (!newColumn.name) {
        throw new Error('column.name cannot be empty')
      }
      if (!newColumn.label) {
        throw new Error('column.label cannot be empty')
      }
      if (!newColumn.getValue) {
        throw new Error('column.getValue cannot be empty')
      }

      return this._initColumn(newColumn)
    })

    newColumns.forEach((newColumn, newColumnIndex) => {
      let newColumnVisibility = newColumnsVisibility[newColumn.name]

      if (newColumnVisibility === undefined) {
        newColumnsVisibility[newColumn.name] = true
      }

      let newColumnSorting = _.findIndex(
        newColumnsSorting,
        columnName => columnName === newColumn.name
      )

      if (newColumnSorting === -1) {
        newColumnSorting = newColumns
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

    newColumns.forEach(newColumn => {
      newColumn.visible = newColumnsVisibility[newColumn.name]
    })

    this._sortColumns(newColumns, newColumnsSorting)

    return { newColumns, newColumnsVisibility, newColumnsSorting }
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

  _initColumn(column) {
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
      filterable: column.filterable === undefined ? true : column.filterable
    }
  }

  async load() {
    const { columns, loadColumns, rows, loadRows, onLoad } =
      this.context.getProps()

    if ((rows && loadRows) || (!rows && !loadRows)) {
      throw new Error(
        'Incorrect grid configuration. Please set "rows" or "loadRows" property.'
      )
    }

    if ((columns && loadColumns) || (!columns && !loadColumns)) {
      throw new Error(
        'Incorrect grid configuration. Please set "columns" or "loadColumns" property.'
      )
    }

    await this.context.setState(() => ({
      loading: true
    }))

    const { filters, page, pageSize, sort, sortDirection } =
      this.context.getState()

    let newRows,
      newAllRows,
      newColumns,
      newColumnsVisibility,
      newColumnsSorting,
      newPage,
      newTotalCount

    if (rows) {
      ;({ newColumns, newColumnsVisibility, newColumnsSorting } =
        await this._initColumns(rows))
      const filteredRows = this._filter(rows, newColumns, filters)
      const pageCount = Math.max(Math.ceil(filteredRows.length / pageSize), 1)
      const sortedRows = this._sort(
        filteredRows,
        newColumns,
        sort,
        sortDirection
      )

      newPage = Math.min(page, pageCount - 1)
      newRows = this._page(sortedRows, newPage, pageSize)
      newAllRows = rows
      newTotalCount = filteredRows.length
    } else if (loadRows) {
      const result = await loadRows({
        filters,
        page,
        pageSize,
        sort,
        sortDirection
      })

      const newPageCount = Math.max(Math.ceil(result.totalCount / pageSize), 1)
      newPage = Math.min(page, newPageCount - 1)
      newRows = result.rows
      newAllRows = result.rows
      newTotalCount = result.totalCount
      ;({ newColumns, newColumnsVisibility, newColumnsSorting } =
        await this._initColumns(newRows))
    }

    await this.context.setState(() => ({
      loading: false,
      loaded: true,
      columnsVisibility: newColumnsVisibility,
      columnsSorting: newColumnsSorting,
      columns: newColumns,
      rows: newRows,
      allRows: newAllRows,
      page: newPage,
      totalCount: newTotalCount
    }))

    if (onLoad) {
      onLoad()
    }
  }

  _loadSettings() {
    const props = this.context.getProps()

    if (
      !props.settingsId ||
      !props.settingsId.webAppId ||
      !props.settingsId.gridId
    ) {
      return Promise.resolve()
    }

    let id = new openbis.Me()
    let fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(props.settingsId.webAppId).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      let person = map[id]
      let webAppSettings = person.webAppSettings[props.settingsId.webAppId]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[props.settingsId.gridId]
        if (gridSettings) {
          let settings = JSON.parse(gridSettings.value)
          if (settings) {
            return settings
          } else {
            return {}
          }
        }
      }
    })
  }

  _applySettings(settings, state) {
    let newColumns = [...state.columns]
    newColumns.sort((c1, c2) => {
      let index1 = _.findIndex(settings.columns, ['name', c1.name])
      let index2 = _.findIndex(settings.columns, ['name', c2.name])
      return index1 - index2
    })
    newColumns = newColumns.map(column => {
      let setting = _.find(settings.columns, ['name', column.name])
      if (setting) {
        return {
          ...column,
          visible: setting.visible
        }
      } else {
        return column
      }
    })
    this.context.setState(() => ({
      ...settings,
      columns: newColumns
    }))
  }

  _saveSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    if (
      !props.settingsId ||
      !props.settingsId.webAppId ||
      !props.settingsId.gridId
    ) {
      throw new Error(
        'Incorrect grid component usage. Settings id is missing. Please contact a developer.'
      )
    }

    let columns = state.columns.map(column => ({
      name: column.name,
      visible: column.visible
    }))

    let settings = {
      pageSize: state.pageSize,
      sort: state.sort,
      sortDirection: state.sortDirection,
      columns
    }

    let gridSettings = new openbis.WebAppSettingCreation()
    gridSettings.setName(props.settingsId.gridId)
    gridSettings.setValue(JSON.stringify(settings))

    let update = new openbis.PersonUpdate()
    update.setUserId(new openbis.Me())
    update.getWebAppSettings(props.settingsId.webAppId).add(gridSettings)

    openbis.updatePersons([update])
  }

  async updateSelectedRow(newSelectedRowId) {
    const { onSelectedRowChange } = this.context.getProps()
    const { selectedRowId } = this.context.getState()

    if (newSelectedRowId !== selectedRowId) {
      await this.context.setState(() => ({
        selectedRowId: newSelectedRowId
      }))
      if (onSelectedRowChange) {
        onSelectedRowChange(this.getSelectedRow())
      }
    }
  }

  showSelectedRow() {
    setTimeout(async () => {
      const { selectedRowId, allRows, page, pageSize } = this.context.getState()

      if (!selectedRowId) {
        return
      }

      const index = _.findIndex(allRows, ['id', selectedRowId])

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
    }, 1)
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
  }

  async handleColumnOrderChange(sourceIndex, destinationIndex) {
    await this.context.setState(state => {
      const sourceColumn = state.columns[sourceIndex]
      const destinationColumn = state.columns[destinationIndex]

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

      const newColumns = [...state.columns]
      this._sortColumns(newColumns, newColumnsSorting)

      return {
        columns: newColumns,
        columnsSorting: newColumnsSorting
      }
    })

    await this.load()
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
  }

  async handleRowSelect(row) {
    await this.updateSelectedRow(row ? row.id : null)
  }

  _filter(rows, columns, filters) {
    return _.filter([...rows], row => {
      let matchesAll = true
      columns.forEach(column => {
        if (column.visible) {
          let filter = filters[column.name]
          matchesAll = matchesAll && column.matches(row, filter)
        }
      })
      return matchesAll
    })
  }

  _sort(rows, columns, sort, sortDirection) {
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

  _page(rows, page, pageSize) {
    return rows.slice(
      page * pageSize,
      Math.min(rows.length, (page + 1) * pageSize)
    )
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
    const { selectedRowId, rows, allRows } = this.context.getState()

    if (selectedRowId !== null && selectedRowId !== undefined) {
      const selectedRow = _.find(allRows, row => row.id === selectedRowId)
      if (selectedRow !== undefined) {
        const visible =
          _.findIndex(rows, row => row.id === selectedRowId) !== -1
        return {
          id: selectedRow.id,
          visible
        }
      }
    }

    return null
  }

  getTotalCount() {
    const { totalCount } = this.context.getState()
    return totalCount
  }
}
