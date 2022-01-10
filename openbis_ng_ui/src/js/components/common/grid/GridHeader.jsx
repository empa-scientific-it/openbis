import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import GridSortingOptions from '@src/js/components/common/grid/GridSortingOptions.js'
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
  },
  sortIndex: {
    color: theme.typography.label.color,
    position: 'absolute',
    right: 0,
    paddingTop: '10px',
    fontSize: theme.typography.label.fontSize
  }
})

class GridHeader extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    const { column, onSortChange } = this.props
    if (onSortChange) {
      onSortChange(column, event.ctrlKey || event.metaKey)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeader.render')

    const { column, sortCount, sortIndex, sortDirection, classes } = this.props

    if (column.sortable) {
      const active = sortIndex !== null && sortDirection !== null
      return (
        <TableCell classes={{ root: classes.cell }}>
          <TableSortLabel
            active={active}
            direction={active ? sortDirection : GridSortingOptions.ASC}
            onClick={this.handleClick}
          >
            {column.label}
            {sortCount > 1 && sortIndex !== null && (
              <span className={classes.sortIndex}>{sortIndex + 1}</span>
            )}
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
