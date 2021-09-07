import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import GridCell from '@src/js/components/common/grid/GridCell.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  row: {
    cursor: 'pointer'
  }
})

class GridRow extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick() {
    const { onClick, row } = this.props
    if (onClick) {
      onClick(row)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridRow.render')

    const { columns, row, selected, classes } = this.props

    return (
      <TableRow
        key={row.id}
        onClick={this.handleClick}
        hover={true}
        selected={selected}
        classes={{
          root: classes.row
        }}
      >
        {columns.map(column => (
          <GridCell key={column.name} row={row} column={column} />
        ))}
      </TableRow>
    )
  }
}

export default withStyles(styles)(GridRow)
