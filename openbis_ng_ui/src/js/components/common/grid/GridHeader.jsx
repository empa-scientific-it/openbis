import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import GridHeaderFilter from '@src/js/components/common/grid/GridHeaderFilter.jsx'
import GridHeaderLabel from '@src/js/components/common/grid/GridHeaderLabel.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  header: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      fontWeight: 'bold',
      backgroundColor: theme.palette.background.primary,
      minWidth: '120px'
    }
  },
  multiselect: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  }
})

class GridHeader extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleSelectAllRowsChange() {
    const { onSelectAllRowsChange } = this.props
    if (onSelectAllRowsChange) {
      onSelectAllRowsChange()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeader.render')

    const { columns, classes } = this.props

    return (
      <TableHead>
        <TableRow>
          {this.renderMultiselectFilterCell()}
          {columns.map(column => this.renderFilterCell(column))}
        </TableRow>
        <TableRow classes={{ root: classes.header }}>
          {this.renderMultiselectHeaderCell()}
          {columns.map(column => this.renderHeaderCell(column))}
        </TableRow>
      </TableHead>
    )
  }

  renderFilterCell(column) {
    const { filters, onFilterChange } = this.props

    return (
      <GridHeaderFilter
        key={column.name}
        column={column}
        filter={filters[column.name]}
        onFilterChange={onFilterChange}
      />
    )
  }

  renderMultiselectFilterCell() {
    if (this.props.multiselectable) {
      const { classes } = this.props
      return <TableCell classes={{ root: classes.multiselect }}></TableCell>
    } else {
      return null
    }
  }

  renderHeaderCell(column) {
    const { sort, sortDirection, onSortChange } = this.props

    return (
      <GridHeaderLabel
        key={column.name}
        column={column}
        sort={sort}
        sortDirection={sortDirection}
        onSortChange={onSortChange}
      />
    )
  }

  renderMultiselectHeaderCell() {
    const { multiselectable, multiselectedRows, rows, classes } = this.props

    if (multiselectable) {
      const multiselectedRowIds = Object.keys(multiselectedRows)
      const rowIds = rows.map(row => String(row.id))
      const rowIdsSelected = _.intersection(rowIds, multiselectedRowIds)

      const value = rowIds.length > 0 && rowIds.length === rowIdsSelected.length
      const indeterminate =
        rowIdsSelected.length > 0 && rowIdsSelected.length < rowIds.length

      return (
        <TableCell classes={{ root: classes.multiselect }}>
          <CheckboxField
            value={value}
            indeterminate={indeterminate}
            onChange={this.handleSelectAllRowsChange}
          />
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeader)
