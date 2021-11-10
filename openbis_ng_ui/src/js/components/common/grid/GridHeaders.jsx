import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import GridHeader from '@src/js/components/common/grid/GridHeader.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  header: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      fontWeight: 'bold',
      backgroundColor: theme.palette.background.primary
    }
  },
  multiselect: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0,
    width: '30px'
  }
})

class GridHeaders extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleMultiselectAllRowsChange() {
    const { onMultiselectAllRowsChange } = this.props
    if (onMultiselectAllRowsChange) {
      onMultiselectAllRowsChange()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeaders.render')

    const { columns, classes } = this.props

    return (
      <TableRow classes={{ root: classes.header }}>
        {this.renderMultiselectCell()}
        {columns.map(column => this.renderHeaderCell(column))}
      </TableRow>
    )
  }

  renderHeaderCell(column) {
    const { sort, sortDirection, onSortChange } = this.props

    return (
      <GridHeader
        key={column.name}
        column={column}
        sort={sort}
        sortDirection={sortDirection}
        onSortChange={onSortChange}
      />
    )
  }

  renderMultiselectCell() {
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
            onChange={this.handleMultiselectAllRowsChange}
          />
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeaders)
