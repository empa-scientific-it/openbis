import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import GridCell from '@src/js/components/common/grid/GridCell.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  pointer: {
    cursor: 'pointer'
  },
  multiselect: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0
  },
  checkbox: {
    display: 'inline-block'
  }
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

    const { columns, row, clickable, selectable, selected, classes } =
      this.props

    return (
      <TableRow
        key={row.id}
        onClick={this.handleClick}
        hover={true}
        selected={selected}
        classes={{
          root: selectable || clickable ? classes.pointer : null
        }}
      >
        {this.renderMultiselect()}
        {columns.map(column => (
          <GridCell key={column.name} row={row} column={column} />
        ))}
      </TableRow>
    )
  }

  renderMultiselect() {
    const { multiselectable, multiselected, classes } = this.props

    if (multiselectable) {
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
