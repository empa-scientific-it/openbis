import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const styles = theme => ({
  row: {
    cursor: 'pointer'
  },
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  },
  wrap: {
    whiteSpace: 'normal'
  },
  nowrap: {
    whiteSpace: 'nowrap'
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
        {columns.map(column => this.renderCell(column))}
      </TableRow>
    )
  }

  renderCell(column) {
    const { classes } = this.props

    if (column.visible) {
      let rendered = this.renderValue(column)

      return (
        <TableCell
          key={column.name}
          classes={{ root: util.classNames(classes.cell, classes.nowrap) }}
        >
          {rendered ? rendered : <span>&nbsp;</span>}
        </TableCell>
      )
    } else {
      return null
    }
  }

  renderValue(column) {
    const { row, classes } = this.props

    const value = column.getValue({ row, column })
    const params = {
      value,
      row,
      column,
      classes: {
        wrap: classes.wrap,
        nowrap: classes.nowrap
      }
    }

    const renderedValue = column.renderValue
      ? column.renderValue(params)
      : value

    if (renderedValue === null || renderedValue === undefined) {
      return ''
    } else if (_.isNumber(renderedValue) || _.isBoolean(renderedValue)) {
      return String(renderedValue)
    } else {
      return renderedValue
    }
  }
}

export default withStyles(styles)(GridRow)
