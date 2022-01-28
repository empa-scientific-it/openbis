import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import GridCell from '@src/js/components/common/grid/GridCell.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  row: {
    backgroundColor: theme.palette.background.paper,
    '&:hover': {
      backgroundColor: '#f5f5f5'
    },
    '&:hover$selected': {
      backgroundColor: '#e8f7fd'
    }
  },
  clickable: {
    cursor: 'pointer'
  },
  selectable: {
    cursor: 'pointer'
  },
  selected: {
    backgroundColor: '#e8f7fd'
  },
  multiselectable: {},
  multiselect: {
    backgroundColor: 'inherit',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0,
    position: 'sticky',
    left: 0,
    zIndex: 100
  },
  checkbox: {
    display: 'inline-block'
  },
  cell: {
    backgroundColor: 'inherit',
    '&$firstCell': {
      paddingLeft: theme.spacing(2),
      position: 'sticky',
      left: 0,
      zIndex: 100
    },
    '$multiselectable &$firstCell': {
      left: '48px'
    }
  },
  firstCell: {}
})

class GridRow extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
    this.handleMultiselect = this.handleMultiselect.bind(this)
  }

  handleClick() {
    const { clickable, selectable, onClick, onSelect, row } = this.props

    if (selectable && onSelect) {
      onSelect(row)
    }

    if (clickable && onClick) {
      onClick(row)
    }
  }

  handleMultiselect(event) {
    event.preventDefault()
    event.stopPropagation()

    const { multiselectable, onMultiselect, row } = this.props

    if (multiselectable && onMultiselect) {
      onMultiselect(row)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridRow.render')

    const {
      multiselectable,
      columns,
      row,
      clickable,
      selectable,
      selected,
      classes
    } = this.props

    const rowClasses = [classes.row]

    if (multiselectable) {
      rowClasses.push(classes.multiselectable)
    }
    if (selectable) {
      rowClasses.push(classes.selectable)
    }
    if (selected) {
      rowClasses.push(classes.selected)
    }
    if (clickable) {
      rowClasses.push(classes.clickable)
    }

    return (
      <TableRow
        key={row.id}
        onClick={this.handleClick}
        classes={{ root: rowClasses.join(' ') }}
      >
        {this.renderMultiselect()}
        {columns.map((column, columnIndex) =>
          this.renderCell(column, columnIndex, row)
        )}
      </TableRow>
    )
  }

  renderCell(column, columnIndex, row) {
    const { classes } = this.props

    const cellClasses = [classes.cell]
    if (columnIndex === 0) {
      cellClasses.push(classes.firstCell)
    }

    return (
      <GridCell
        key={column.name}
        row={row}
        column={column}
        styles={{ root: cellClasses.join(' ') }}
      />
    )
  }

  renderMultiselect() {
    const { columns, multiselectable, multiselected, classes } = this.props

    if (columns.length > 0 && multiselectable) {
      return (
        <TableCell classes={{ root: classes.multiselect }}>
          <div className={classes.checkbox}>
            <CheckboxField
              value={multiselected}
              onClick={this.handleMultiselect}
            />
          </div>
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridRow)
