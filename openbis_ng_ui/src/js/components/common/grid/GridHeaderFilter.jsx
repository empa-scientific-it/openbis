import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TextField from '@src/js/components/common/form/TextField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeaderFilter extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleFilterChange = this.handleFilterChange.bind(this)
  }

  handleFilterChange(event) {
    const { column, onFilterChange } = this.props
    if (onFilterChange) {
      onFilterChange(column.name, event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeaderFilter.render')

    const { column, classes } = this.props

    if (column.visible) {
      let rendered = this.renderFilter()

      return (
        <TableCell classes={{ root: classes.cell }}>
          {column.filterable && rendered}
        </TableCell>
      )
    } else {
      return null
    }
  }

  renderFilter() {
    const { column, filter } = this.props

    const params = {
      value: filter,
      column,
      onChange: this.handleFilterChange
    }

    const renderedFilter = column.renderFilter ? (
      column.renderFilter(params)
    ) : (
      <TextField
        label={messages.get(messages.FILTER)}
        value={filter}
        onChange={this.handleFilterChange}
        variant='standard'
      />
    )

    if (renderedFilter === null || renderedFilter === undefined) {
      return ''
    } else if (_.isNumber(renderedFilter) || _.isBoolean(renderedFilter)) {
      return String(renderedFilter)
    } else {
      return renderedFilter
    }
  }
}

export default withStyles(styles)(GridHeaderFilter)
