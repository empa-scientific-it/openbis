import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import Link from '@material-ui/core/Link'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const TRUNCATE_HEIGHT = 50

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
    overflow: 'hidden'
  }
})

class GridCell extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      renderMore: false,
      moreOpen: false
    }
    this.ref = React.createRef()
    this.handleMoreClick = this.handleMoreClick.bind(this)
  }

  componentDidMount() {
    this.renderDOMValue()
    this.maybeScheduleRenderMore()
  }

  handleMoreClick() {
    this.setState(state => ({
      moreOpen: !state.moreOpen
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'GridCell.render')

    const { column, className, classes } = this.props
    const { moreOpen } = this.state

    const cellClasses = [classes.cell]
    if (className) {
      cellClasses.push(className)
    }

    const divClasses = []
    if (column.nowrap) {
      divClasses.push(classes.nowrap)
    }
    if (column.truncate && !moreOpen) {
      divClasses.push(classes.truncate)
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
    const { renderMore, moreOpen } = this.state

    if (!renderMore) {
      return null
    }

    return (
      <div>
        <Link onClick={this.handleMoreClick}>
          {moreOpen ? messages.get(messages.LESS) : messages.get(messages.MORE)}
        </Link>
      </div>
    )
  }

  maybeScheduleRenderMore() {
    const { column } = this.props
    if (
      column.truncate &&
      this.ref.current &&
      this.ref.current.scrollHeight > TRUNCATE_HEIGHT
    ) {
      setTimeout(() => {
        this.setState({ renderMore: true })
      }, 1)
    }
  }
}

export default withStyles(styles)(GridCell)
