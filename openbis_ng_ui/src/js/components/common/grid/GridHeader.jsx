import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0,
    borderColor: theme.palette.border.secondary,
    '&:last-child': {
      paddingRight: theme.spacing(2)
    }
  }
})

class GridHeader extends React.PureComponent {
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
    logger.log(logger.DEBUG, 'GridHeader.render')

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

export default withStyles(styles)(GridHeader)
