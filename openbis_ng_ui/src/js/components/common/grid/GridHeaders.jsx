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
  multiselectable: {},
  multiselectContainer: {
    width: '32px'
  },
  multiselect: {
    backgroundColor: theme.palette.background.primary,
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0,
    position: 'sticky',
    left: 0,
    zIndex: 100
  },
  header: {
    '&$firstHeader': {
      paddingLeft: theme.spacing(2),
      position: 'sticky',
      left: 0,
      zIndex: 100
    },
    '$multiselectable &$firstHeader': {
      left: '48px'
    }
  },
  firstHeader: {}
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

    const { multiselectable, columns, classes } = this.props

    let rowClass = null
    if (multiselectable) {
      rowClass = classes.multiselectable
    }

    return (
      <TableRow classes={{ root: rowClass }}>
        {this.renderMultiselectCell()}
        {columns.map((column, columnIndex) =>
          this.renderHeaderCell(column, columnIndex)
        )}
      </TableRow>
    )
  }

  renderHeaderCell(column, columnIndex) {
    const { sortings, onSortChange, classes } = this.props

    const index = _.findIndex(
      sortings,
      sorting => sorting.columnName === column.name
    )

    const headerClasses = [classes.header]
    if (columnIndex === 0) {
      headerClasses.push(classes.firstHeader)
    }

    return (
      <GridHeader
        key={column.name}
        column={column}
        sortCount={sortings.length}
        sortIndex={index !== -1 ? index : null}
        sortDirection={index !== -1 ? sortings[index].sortDirection : null}
        onSortChange={onSortChange}
        styles={{ root: headerClasses.join(' ') }}
      />
    )
  }

  renderMultiselectCell() {
    const { columns, multiselectable, multiselectedRows, rows, classes } =
      this.props

    if (columns.length > 0 && multiselectable) {
      const multiselectedRowIds = Object.keys(multiselectedRows)
      const rowIds = rows.map(row => String(row.id))
      const rowIdsSelected = _.intersection(rowIds, multiselectedRowIds)

      const value = rowIds.length > 0 && rowIds.length === rowIdsSelected.length
      const indeterminate =
        rowIdsSelected.length > 0 && rowIdsSelected.length < rowIds.length

      return (
        <TableCell classes={{ root: classes.multiselect }}>
          <div className={classes.multiselectContainer}>
            <CheckboxField
              value={value}
              indeterminate={indeterminate}
              onChange={this.handleMultiselectAllRowsChange}
            />
          </div>
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeaders)
