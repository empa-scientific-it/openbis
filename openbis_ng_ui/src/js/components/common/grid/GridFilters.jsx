import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridGlobalFilter from '@src/js/components/common/grid/GridGlobalFilter.jsx'
import GridFilter from '@src/js/components/common/grid/GridFilter.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  multiselectCell: {
    padding: 0,
    paddingLeft: theme.spacing(2),
    paddingBottom: theme.spacing(2),
    minWidth: 50
  },
  noFilters: {
    padding: 0,
    paddingTop: theme.spacing(1)
  },
  filterTypeCell: {
    borderBottom: 0,
    paddingBottom: 0,
    paddingTop: theme.spacing(1)
  }
})

class GridFilters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleFilterModeChange = this.handleFilterModeChange.bind(this)
  }

  handleFilterModeChange(event) {
    const { onFilterModeChange } = this.props
    if (onFilterModeChange) {
      onFilterModeChange(event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridFilters.render')

    const { columns, filterModes, filterMode } = this.props

    if (filterModes && !filterModes.includes(filterMode)) {
      return (
        <TableRow>
          {this.renderMultiselectCell()}
          {this.renderNoFiltersCell()}
        </TableRow>
      )
    } else if (filterMode === GridFilterOptions.GLOBAL_FILTER) {
      return (
        <React.Fragment>
          <TableRow>{this.renderGlobalFilterCell()}</TableRow>
        </React.Fragment>
      )
    } else if (filterMode === GridFilterOptions.COLUMN_FILTERS) {
      return (
        <React.Fragment>
          <TableRow>
            {this.renderMultiselectCell()}
            {columns.map(column => this.renderFilterCell(column))}
          </TableRow>
        </React.Fragment>
      )
    } else {
      throw new Error('Unsupported filter mode: ' + filterMode)
    }
  }

  renderNoFiltersCell() {
    const { columns, classes } = this.props
    return (
      <TableCell
        colSpan={columns.length}
        classes={{ root: classes.noFilters }}
      ></TableCell>
    )
  }

  renderGlobalFilterCell() {
    const { columns, globalFilter, onGlobalFilterChange } = this.props

    return (
      <GridGlobalFilter
        columns={columns}
        globalFilter={globalFilter}
        onGlobalFilterChange={onGlobalFilterChange}
      />
    )
  }

  renderFilterCell(column) {
    const { filters, onFilterChange } = this.props

    return (
      <GridFilter
        key={column.name}
        column={column}
        filter={filters[column.name]}
        onFilterChange={onFilterChange}
      />
    )
  }

  renderMultiselectCell() {
    const { columns, multiselectable, classes } = this.props

    if (columns.length > 0 && multiselectable) {
      return (
        <TableCell classes={{ root: classes.multiselectCell }}>
          <TextField label='Filter' variant='standard' />
        </TableCell>
      )
    } else {
      return null
    }
  }

  renderFilterType() {
    const { filterMode, columns, classes } = this.props

    return (
      <TableRow>
        <TableCell
          colSpan={columns.length + 1}
          classes={{ root: classes.filterTypeCell }}
        >
          <SelectField
            name='filters'
            label='Filter Type'
            options={[
              {
                label: messages.get(messages.GLOBAL_FILTER),
                value: GridFilterOptions.GLOBAL_FILTER
              },
              {
                label: messages.get(messages.COLUMN_FILTERS),
                value: GridFilterOptions.COLUMN_FILTERS
              }
            ]}
            value={filterMode}
            variant='standard'
            fullWidth={false}
            onChange={this.handleFilterModeChange}
          />
        </TableCell>
      </TableRow>
    )
  }
}

export default withStyles(styles)(GridFilters)
