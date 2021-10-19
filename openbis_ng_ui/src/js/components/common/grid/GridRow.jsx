import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import GridCell from '@src/js/components/common/grid/GridCell.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  row: {
    cursor: 'pointer'
  },
  multiselect: {
    display: 'inline-block'
  }
})

class GridRow extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleSelect = this.handleSelect.bind(this)
    this.handleMultiselect = this.handleMultiselect.bind(this)
  }

  handleSelect() {
    const { selectable, onSelect, row } = this.props
    if (selectable && onSelect) {
      onSelect(row)
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

    const { columns, row, selected, classes } = this.props

    return (
      <TableRow
        key={row.id}
        onClick={this.handleSelect}
        hover={true}
        selected={selected}
        classes={{
          root: classes.row
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
        <TableCell>
          <div className={classes.multiselect}>
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
