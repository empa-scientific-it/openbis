import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeaderFilter extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleFilterChange = this.handleFilterChange.bind(this)
  }

  handleFilterChange(filter) {
    const { column, onFilterChange } = this.props
    if (onFilterChange) {
      onFilterChange(column.name, filter)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeaderFilter.render')

    const { column, filter, classes } = this.props

    if (column.visible) {
      return (
        <TableCell classes={{ root: classes.cell }}>
          {column.filterable && (
            <FilterField
              filter={filter || ''}
              filterChange={this.handleFilterChange}
            />
          )}
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeaderFilter)
