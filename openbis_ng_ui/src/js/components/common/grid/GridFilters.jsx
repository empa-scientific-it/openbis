import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridGlobalFilter from '@src/js/components/common/grid/GridGlobalFilter.jsx'
import GridFilter from '@src/js/components/common/grid/GridFilter.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  row: {
    backgroundColor: theme.palette.background.paper
  },
  multiselectable: {},
  multiselectCell: {
    backgroundColor: 'inherit',
    padding: 0,
    position: 'sticky',
    left: 0,
    zIndex: 100
  },
  columnFilterCell: {
    backgroundColor: 'inherit',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: 0,
    paddingRight: theme.spacing(2),
    '&$firstCell': {
      paddingLeft: theme.spacing(2),
      position: 'sticky',
      left: 0,
      zIndex: 100
    },
    '$multiselectable &$firstCell': {
      left: '44px'
    }
  },
  globalFilterCell: {
    backgroundColor: 'inherit',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2)
  },
  firstCell: {}
})

class GridFilters extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridFilters.render')

    const { multiselectable, columns, filterModes, filterMode, classes } =
      this.props

    if (columns.length === 0) {
      return null
    }

    let rowClasses = [classes.row]
    if (multiselectable) {
      rowClasses.push(classes.multiselectable)
    }

    if (filterModes && !filterModes.includes(filterMode)) {
      return null
    } else if (filterMode === GridFilterOptions.GLOBAL_FILTER) {
      return (
        <TableRow classes={{ root: rowClasses.join(' ') }}>
          {this.renderGlobalFilterCell()}
        </TableRow>
      )
    } else if (filterMode === GridFilterOptions.COLUMN_FILTERS) {
      const someFilterable = columns.some(column => column.filterable)
      if (someFilterable) {
        return (
          <TableRow classes={{ root: rowClasses.join(' ') }}>
            {this.renderMultiselectCell()}
            {this.renderColumnFiltersCells()}
          </TableRow>
        )
      } else {
        return null
      }
    } else {
      throw new Error('Unsupported filter mode: ' + filterMode)
    }
  }

  renderGlobalFilterCell() {
    const {
      columns,
      multiselectable,
      globalFilter,
      onGlobalFilterChange,
      classes
    } = this.props

    return (
      <TableCell
        colSpan={columns.length + (multiselectable ? 1 : 0)}
        classes={{ root: classes.globalFilterCell }}
      >
        <GridGlobalFilter
          globalFilter={globalFilter}
          onGlobalFilterChange={onGlobalFilterChange}
        />
      </TableCell>
    )
  }

  renderColumnFiltersCells() {
    const { columns, filters, onFilterChange, classes } = this.props

    return columns.map((column, columnIndex) => {
      const cellClasses = [classes.columnFilterCell]

      if (columnIndex === 0) {
        cellClasses.push(classes.firstCell)
      }

      return (
        <TableCell key={column.name} classes={{ root: cellClasses.join(' ') }}>
          <GridFilter
            column={column}
            filter={filters[column.name]}
            onFilterChange={onFilterChange}
          />
        </TableCell>
      )
    })
  }

  renderMultiselectCell() {
    const { columns, multiselectable, classes } = this.props

    if (columns.length > 0 && multiselectable) {
      return <TableCell classes={{ root: classes.multiselectCell }}></TableCell>
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridFilters)
