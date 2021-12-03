import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridGlobalFilter from '@src/js/components/common/grid/GridGlobalFilter.jsx'
import GridFilter from '@src/js/components/common/grid/GridFilter.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class GridFilters extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridFilters.render')

    const { columns, filterModes, filterMode } = this.props

    if (filterModes && !filterModes.includes(filterMode)) {
      return (
        <TableRow>
          {this.renderMultiselectCell()}
          {this.renderNoFiltersCell()}
        </TableRow>
      )
    } else if (filterMode === GridFilterOptions.GLOBAL_FILTER) {
      return (
        <TableRow>
          {this.renderMultiselectCell()}
          {this.renderGlobalFilterCell()}
        </TableRow>
      )
    } else if (filterMode === GridFilterOptions.COLUMN_FILTERS) {
      return (
        <TableRow>
          {this.renderMultiselectCell()}
          {columns.map(column => this.renderFilterCell(column))}
        </TableRow>
      )
    } else {
      throw new Error('Unsupported filter mode: ' + filterMode)
    }
  }

  renderNoFiltersCell() {
    const { columns } = this.props
    return <TableCell colSpan={columns.length}></TableCell>
  }

  renderGlobalFilterCell() {
    const { columns, globalFilter, onGlobalFilterChange } = this.props

    return (
      <GridGlobalFilter
        columns={columns}
        globalFilter={globalFilter}
        onGlobalFilterChange={onGlobalFilterChange}
      />
    )
  }

  renderFilterCell(column) {
    const { filters, onFilterChange } = this.props

    return (
      <GridFilter
        key={column.name}
        column={column}
        filter={filters[column.name]}
        onFilterChange={onFilterChange}
      />
    )
  }

  renderMultiselectCell() {
    const { multiselectable } = this.props

    if (multiselectable) {
      return <TableCell></TableCell>
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridFilters)
