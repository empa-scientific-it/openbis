import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import Link from '@material-ui/core/Link'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const TRUNCATE_HEIGHT = 100
const TRUNCATE_WIDTH = 400
const MORE_HEIGHT = 20

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
  nowrap: {
    whiteSpace: 'nowrap'
  },
  truncate: {
    maxHeight: TRUNCATE_HEIGHT + 'px',
    maxWidth: TRUNCATE_WIDTH,
    overflow: 'hidden'
  },
  truncateWithMore: {
    maxHeight: TRUNCATE_HEIGHT - MORE_HEIGHT + 'px',
    maxWidth: TRUNCATE_WIDTH,
    overflow: 'hidden'
  }
})

class GridCell extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      more: false
    }
    this.ref = React.createRef()
    this.handleMoreClick = this.handleMoreClick.bind(this)
  }

  componentDidMount() {
    this.componentDidUpdate()
  }

  componentDidUpdate() {
    this.renderDOMValue()

    const { column, row, onMeasured } = this.props
    if (column.truncate && this.ref.current) {
      onMeasured(this.ref, column, row)
    }
  }

  handleMoreClick() {
    this.setState(state => ({
      more: !state.more
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'GridCell.render')

    const { column, height, className, classes } = this.props
    const { more } = this.state

    const cellClasses = [classes.cell]
    if (className) {
      cellClasses.push(className)
    }

    const divClasses = []
    if (column.nowrap) {
      divClasses.push(classes.nowrap)
    }
    if (column.truncate && !more) {
      if (height && height > TRUNCATE_HEIGHT) {
        divClasses.push(classes.truncateWithMore)
      } else {
        divClasses.push(classes.truncate)
      }
    }

    return (
      <TableCell key={column.name} classes={{ root: cellClasses.join(' ') }}>
        <div ref={this.ref} className={divClasses.join(' ')}>
          {column.renderDOMValue ? null : this.renderValue()}
        </div>
        {this.renderMore()}
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

  renderMore() {
    const { column, height } = this.props
    const { more } = this.state

    if (column.truncate && height && height > TRUNCATE_HEIGHT) {
      return (
        <div>
          <Link onClick={this.handleMoreClick}>
            {more ? messages.get(messages.LESS) : messages.get(messages.MORE)}
          </Link>
        </div>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridCell)
