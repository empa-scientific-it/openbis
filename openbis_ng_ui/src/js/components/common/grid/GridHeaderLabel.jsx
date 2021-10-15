import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeaderLabel extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick() {
    const { column, onSortChange } = this.props
    if (onSortChange) {
      onSortChange(column)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeaderLabel.render')

    const { column, sort, sortDirection, classes } = this.props

    if (column.sortable) {
      const active = sort === column.name
      return (
        <TableCell classes={{ root: classes.cell }}>
          <TableSortLabel
            active={active}
            direction={active ? sortDirection : 'asc'}
            onClick={this.handleClick}
          >
            {column.label}
          </TableSortLabel>
        </TableCell>
      )
    } else {
      return (
        <TableCell classes={{ root: classes.cell }}>{column.label}</TableCell>
      )
    }
  }
}

export default withStyles(styles)(GridHeaderLabel)
