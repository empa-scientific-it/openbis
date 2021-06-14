import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
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

  handleFilterChange(filter) {
    const { column, onFilterChange } = this.props
    if (onFilterChange) {
      onFilterChange(column.name, filter)
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
      <FilterField
        filter={filter || ''}
        filterChange={this.handleFilterChange}
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
