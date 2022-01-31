import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: 0,
    paddingRight: theme.spacing(2),
    borderColor: theme.palette.border.secondary,
    '&:empty:before': {
      content: '"\\a0"'
    }
  },
  wrap: {
    whiteSpace: 'normal'
  },
  nowrap: {
    whiteSpace: 'nowrap'
  }
})

class GridCell extends React.PureComponent {
  constructor(props) {
    super(props)
    this.ref = React.createRef()
  }

  componentDidMount() {
    this.renderDOMValue()
  }

  componentDidUpdate() {
    this.renderDOMValue()
  }

  render() {
    logger.log(logger.DEBUG, 'GridCell.render')

    const { column, className, classes } = this.props

    const cellClasses = [classes.cell]
    if (column.wrappable) {
      cellClasses.push(classes.wrap)
    } else {
      cellClasses.push(classes.nowrap)
    }
    if (className) {
      cellClasses.push(className)
    }

    return (
      <TableCell
        ref={this.ref}
        key={column.name}
        classes={{ root: cellClasses.join(' ') }}
      >
        {column.renderDOMValue ? null : this.renderValue()}
      </TableCell>
    )
  }

  renderValue() {
    const { row, column } = this.props

    const value = column.getValue({ row, column, operation: 'render' })
    const renderedValue = column.renderValue
      ? column.renderValue({
          value,
          row,
          column
        })
      : value

    if (renderedValue === null || renderedValue === undefined) {
      return null
    } else if (_.isNumber(renderedValue) || _.isBoolean(renderedValue)) {
      return String(renderedValue)
    } else {
      return renderedValue
    }
  }

  renderDOMValue() {
    const { row, column } = this.props

    if (column.renderDOMValue && this.ref.current) {
      const value = column.getValue({ row, column, operation: 'render' })
      column.renderDOMValue({
        container: this.ref.current,
        value,
        row,
        column
      })
    }
  }
}

export default withStyles(styles)(GridCell)
