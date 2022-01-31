import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import GridSortingOptions from '@src/js/components/common/grid/GridSortingOptions.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    backgroundColor: theme.palette.background.primary,
    borderColor: theme.palette.border.secondary,
    fontWeight: 'bold',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: 0,
    paddingRight: theme.spacing(2)
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

    const { column, sortCount, sortIndex, sortDirection, className, classes } =
      this.props

    if (column.sortable) {
      const active = sortIndex !== null && sortDirection !== null
      return (
        <TableCell
          classes={{
            root: `${className} ${classes.cell}`
          }}
        >
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
        <TableCell
          classes={{
            root: `${className} ${classes.cell}`
          }}
        >
          {column.label}
        </TableCell>
      )
    }
  }
}

export default withStyles(styles)(GridHeader)
