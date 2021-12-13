import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryFormExecuteResultsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'QueryFormExecuteResultsGrid.render')

    const { tableModel } = this.props

    return (
      <Grid
        columns={this.getColumns(tableModel)}
        rows={this.getRows(tableModel)}
        selectable={true}
      />
    )
  }

  getColumns(tableModel) {
    return tableModel.columns.map((column, index) => ({
      name: column.title,
      label: column.title,
      getValue: ({ row }) => row[index] && row[index].value
    }))
  }

  getRows(tableModel) {
    return tableModel.rows.map((row, index) => ({
      id: index,
      ...row
    }))
  }
}

export default withStyles(styles)(QueryFormExecuteResultsGrid)
