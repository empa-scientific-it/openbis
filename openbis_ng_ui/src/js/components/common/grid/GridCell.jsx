import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const styles = theme => ({
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
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

    const { column, classes } = this.props

    return (
      <TableCell
        ref={this.ref}
        key={column.name}
        classes={{ root: util.classNames(classes.cell, classes.nowrap) }}
      >
        {column.renderDOMValue ? null : this.renderValue()}
      </TableCell>
    )
  }

  renderValue() {
    const { row, column, classes } = this.props

    const value = column.getValue({ row, column })
    const renderedValue = column.renderValue
      ? column.renderValue({
          value,
          row,
          column,
          classes: {
            wrap: classes.wrap,
            nowrap: classes.nowrap
          }
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
    const { row, column, classes } = this.props

    if (column.renderDOMValue && this.ref.current) {
      const value = column.getValue({ row, column })
      column.renderDOMValue({
        container: this.ref.current,
        value,
        row,
        column,
        classes: {
          wrap: classes.wrap,
          nowrap: classes.nowrap
        }
      })
    }
  }
}

export default withStyles(styles)(GridCell)
