import _ from 'lodash'
import autoBind from 'auto-bind'
import openbis from '@src/js/services/openbis.js'
import compare from '@src/js/common/compare.js'
import ids from '@src/js/common/consts/ids.js'

export default class GridController {
  constructor() {
    autoBind(this)
  }

  init(context) {
    const props = context.getProps()

    const columns = []
    let initialSort = null
    let initialSortDirection = null

    props.columns.forEach(column => {
      if (column.sort) {
        initialSort = column.name
        initialSortDirection = column.sort
      }

      if (!column.name) {
        throw new Error('column.name cannot be empty')
      }
      if (!column.label) {
        throw new Error('column.label cannot be empty')
      }
      if (!column.getValue) {
        throw new Error('column.getValue cannot be empty')
      }

      columns.push(this.initColumn(column))

      context.initState({
        loaded: false,
        filters: {},
        page: 0,
        pageSize: 10,
        columns,
        allRows: [],
        filteredRows: [],
        sortedRows: [],
        currentRows: [],
        selectedRow: null,
        sort: initialSort,
        sortDirection: initialSortDirection
      })

      this.context = context
    })
  }

  initColumn(config) {
    const column = {}

    _.assign(column, {
      ...config,
      name: config.name,
      label: config.label,
      getValue: config.getValue,
      render: row => {
        const value = config.getValue({ row, column })

        const renderedValue = config.renderValue
          ? config.renderValue({
              value,
              row,
              column
            })
          : value

        if (renderedValue === null || renderedValue === undefined) {
          return ''
        } else if (_.isNumber(renderedValue) || _.isBoolean(renderedValue)) {
          return String(renderedValue)
        } else {
          return renderedValue
        }
      },
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

        const value = config.getValue({ row, column })

        if (config.filterValue) {
          return config.matchesValue({
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
        const value1 = config.getValue({ row: row1, column })
        const value2 = config.getValue({ row: row2, column })
        const { sortDirection } = this.context.getState()

        if (config.compareValue) {
          return config.compareValue({
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
      sortable: config.sortable === undefined ? true : config.sortable,
      visible: true
    })

    return column
  }

  async load() {
    const { rows, selectedRowId } = this.context.getProps()

    await this._loadSettings()
    await this.updateAllRows(rows)
    await this.updateSelectedRowId(selectedRowId)

    await this.context.setState(() => ({
      loaded: true
    }))
  }

  _loadSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    if (!props.session || !props.id) {
      return Promise.resolve()
    }

    let id = new openbis.PersonPermId(props.session.userName)
    let fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      let person = map[id]
      let webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[props.id]
        if (gridSettings) {
          let settings = JSON.parse(gridSettings.value)
          if (settings) {
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
        }
      }
    })
  }

  _saveSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    if (!props.session || !props.id) {
      return Promise.resolve()
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
    gridSettings.setName(props.id)
    gridSettings.setValue(JSON.stringify(settings))

    let update = new openbis.PersonUpdate()
    update.setUserId(new openbis.PersonPermId(props.session.userName))
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    openbis.updatePersons([update])
  }

  async updateAllRows(rows) {
    const { allRows } = this.context.getState()

    if (allRows !== rows) {
      await this._recalculateCurrentRows(rows)
    }
  }

  async updateSelectedRowId(selectedRowId) {
    const { selectedRow } = this.context.getState()

    if (!selectedRow || selectedRow.id !== selectedRowId) {
      await this._recalculateSelectedRow(selectedRowId)
    }
  }

  async _recalculateCurrentRows(rows) {
    const {
      allRows,
      columns,
      filters,
      sort,
      sortDirection,
      page,
      pageSize
    } = this.context.getState()

    if (!rows) {
      rows = allRows
    }

    const filteredRows = this._filter(rows, columns, filters)

    const pageCount = Math.max(Math.ceil(filteredRows.length / pageSize), 1)
    const newPage = Math.min(page, pageCount - 1)

    const sortedRows = this._sort(filteredRows, columns, sort, sortDirection)
    const currentRows = this._page(sortedRows, newPage, pageSize)

    await this.context.setState({
      allRows: rows,
      filteredRows,
      sortedRows,
      currentRows,
      page: newPage
    })

    const { selectedRow } = this.context.getState()

    if (selectedRow) {
      await this._recalculateSelectedRow(selectedRow.id)
    }
  }

  async _recalculateSelectedRow(selectedRowId) {
    const { selectedRow, currentRows } = this.context.getState()
    const { onSelectedRowChange } = this.context.getProps()

    let newSelectedRow = null

    if (selectedRowId) {
      const visible = _.some(
        currentRows,
        currentRow => currentRow.id === selectedRowId
      )
      newSelectedRow = {
        id: selectedRowId,
        visible
      }
    }

    if (!_.isEqual(selectedRow, newSelectedRow)) {
      await this.context.setState({
        selectedRow: newSelectedRow
      })
      if (onSelectedRowChange) {
        await onSelectedRowChange(newSelectedRow)
      }
    }
  }

  async showSelectedRow() {
    const { selectedRow, sortedRows, page, pageSize } = this.context.getState()

    if (!selectedRow) {
      return
    }

    const index = _.findIndex(sortedRows, ['id', selectedRow.id])

    if (index === -1) {
      return
    }

    const newPage = Math.floor(index / pageSize)

    if (newPage !== page) {
      await this.context.setState({
        page: newPage
      })
      await this._recalculateCurrentRows()
    }
  }

  handleFilterChange(column, filter) {
    const state = this.context.getState()

    let filters = {
      ...state.filters,
      [column]: filter
    }

    this.context
      .setState(() => ({
        page: 0,
        filters
      }))
      .then(() => {
        this._recalculateCurrentRows()
      })
  }

  handleColumnVisibleChange(name) {
    const state = this.context.getState()

    let columns = state.columns.map(column => {
      if (column.name === name) {
        return {
          ...column,
          visible: !column.visible
        }
      } else {
        return column
      }
    })

    this.context
      .setState(() => ({
        columns
      }))
      .then(() => {
        this._saveSettings()
      })
  }

  handleColumnOrderChange(sourceIndex, destinationIndex) {
    const state = this.context.getState()

    let columns = [...state.columns]
    let source = columns[sourceIndex]
    columns.splice(sourceIndex, 1)
    columns.splice(destinationIndex, 0, source)

    this.context
      .setState(() => ({
        columns
      }))
      .then(() => {
        this._saveSettings()
      })
  }

  handleSortChange(column) {
    if (!column.sortable) {
      return
    }

    this.context
      .setState(state => {
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
      .then(() => {
        this._saveSettings()
        this._recalculateCurrentRows()
      })
  }

  handlePageChange(page) {
    this.context
      .setState(() => ({
        page
      }))
      .then(() => {
        this._recalculateCurrentRows()
      })
  }

  handlePageSizeChange(pageSize) {
    this.context
      .setState(() => ({
        page: 0,
        pageSize
      }))
      .then(() => {
        this._saveSettings()
        this._recalculateCurrentRows()
      })
  }

  handleRowSelect(row) {
    this.updateSelectedRowId(row ? row.id : null)
  }

  _filter(rows, columns, filters) {
    return _.filter([...rows], row => {
      let matchesAll = true
      columns.forEach(column => {
        let filter = filters[column.name]
        matchesAll = matchesAll && column.matches(row, filter)
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

  getSelectedRow() {
    const { selectedRow } = this.context.getState()
    return selectedRow
  }
}
