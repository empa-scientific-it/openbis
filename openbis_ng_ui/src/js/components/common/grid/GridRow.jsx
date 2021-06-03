import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  row: {
    cursor: 'pointer'
  },
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridRow extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick() {
    const { onClick, row } = this.props
    if (onClick) {
      onClick(row)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridRow.render')

    const { columns, row, selected, classes } = this.props

    return (
      <TableRow
        key={row.id}
        onClick={this.handleClick}
        hover={true}
        selected={selected}
        classes={{
          root: classes.row
        }}
      >
        {columns.map(column => this.renderCell(column, row))}
      </TableRow>
    )
  }

  renderCell(column, row) {
    const { classes } = this.props

    if (column.visible) {
      let rendered = column.render(row)
      return (
        <TableCell key={column.name} classes={{ root: classes.cell }}>
          {rendered ? rendered : <span>&nbsp;</span>}
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridRow)
