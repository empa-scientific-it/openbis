import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridFilter from '@src/js/components/common/grid/GridFilter.jsx'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class GridFilters extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridFilters.render')

    const { columns } = this.props

    return (
      <TableRow>
        {this.renderMultiselectCell()}
        {columns.map(column => this.renderFilterCell(column))}
      </TableRow>
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
