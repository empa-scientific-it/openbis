import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryFormExecuteResultsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'QueryFormExecuteResultsGrid.render')

    const { tableModel } = this.props

    return (
      <GridWithOpenbis
        id={ids.QUERY_RESULTS_GRID_ID}
        settingsId={null}
        columns={this.getColumns(tableModel)}
        rows={this.getRows(tableModel)}
        exportable={{
          fileFormat: GridExportOptions.FILE_FORMAT.TSV,
          filePrefix: 'query-results'
        }}
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
