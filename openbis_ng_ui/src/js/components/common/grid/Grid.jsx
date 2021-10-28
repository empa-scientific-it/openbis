import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import Header from '@src/js/components/common/form/Header.jsx'
import GridController from '@src/js/components/common/grid/GridController.js'
import GridHeader from '@src/js/components/common/grid/GridHeader.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridActions from '@src/js/components/common/grid/GridActions.jsx'
import GridExports from '@src/js/components/common/grid/GridExports.jsx'
import GridPaging from '@src/js/components/common/grid/GridPaging.jsx'
import ColumnConfig from '@src/js/components/common/grid/ColumnConfig.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    height: '100%'
  },
  tableHeaderAndBody: {
    width: '100%',
    overflow: 'auto'
  },
  table: {
    borderCollapse: 'unset',
    marginTop: -theme.spacing(1)
  },
  tableBody: {
    '& tr:last-child td': {
      border: 0
    }
  },
  tableFooter: {
    position: 'sticky',
    bottom: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    borderTopWidth: '1px',
    borderTopStyle: 'solid',
    borderTopColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper,
    overflow: 'hidden'
  },
  tableFooterLeft: {
    display: 'flex',
    flex: '1 1 auto',
    paddingLeft: theme.spacing(2)
  },
  tableFooterRight: {
    display: 'flex',
    flex: '0 0 auto'
  }
})

class Grid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new GridController()
    }

    this.controller.init(new ComponentContext(this))

    if (this.props.controllerRef) {
      this.props.controllerRef(this.controller)
    }
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleRowSelect(null)
  }

  handleClickTable(event) {
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if (!this.state.loaded) {
      return <Loading loading={true}></Loading>
    }

    const { header, selectable, multiselectable, actions, classes } = this.props
    const {
      loading,
      filters,
      sort,
      sortDirection,
      page,
      pageSize,
      columnsVisibility,
      rows,
      selectedRow,
      multiselectedRows,
      totalCount,
      exportOptions
    } = this.state

    const allColumns = this.controller.getAllColumns()
    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <div onClick={this.handleClickContainer} className={classes.container}>
        <div>{header && <Header>{header}</Header>}</div>
        <div>
          <Loading loading={loading}>
            <div onClick={this.handleClickTable}>
              <div className={classes.tableHeaderAndBody}>
                <Table classes={{ root: classes.table }}>
                  <GridHeader
                    columns={visibleColumns}
                    rows={rows}
                    filters={filters}
                    sort={sort}
                    sortDirection={sortDirection}
                    onSortChange={this.controller.handleSortChange}
                    onFilterChange={this.controller.handleFilterChange}
                    onSelectAllRowsChange={
                      this.controller.handleSelectAllRowsChange
                    }
                    multiselectable={multiselectable}
                    multiselectedRows={multiselectedRows}
                  />
                  <TableBody classes={{ root: classes.tableBody }}>
                    {rows.map(row => {
                      return (
                        <GridRow
                          key={row.id}
                          columns={visibleColumns}
                          row={row}
                          selectable={selectable}
                          selected={selectedRow && selectedRow.id === row.id}
                          multiselectable={multiselectable}
                          multiselected={
                            multiselectedRows && multiselectedRows[row.id]
                          }
                          onSelect={this.controller.handleRowSelect}
                          onMultiselect={this.controller.handleRowMultiselect}
                        />
                      )
                    })}
                  </TableBody>
                </Table>
              </div>
              <div className={classes.tableFooter}>
                <div className={classes.tableFooterLeft}>
                  <GridActions
                    actions={actions}
                    disabled={Object.keys(multiselectedRows).length === 0}
                    onExecute={this.controller.handleExecuteAction}
                  />
                  <GridExports
                    disabled={rows.length === 0}
                    exportOptions={exportOptions}
                    onExport={this.controller.handleExport}
                    onExportOptionsChange={
                      this.controller.handleExportOptionsChange
                    }
                  />
                </div>
                <div className={classes.tableFooterRight}>
                  <GridPaging
                    count={totalCount}
                    page={page}
                    pageSize={pageSize}
                    onPageChange={this.controller.handlePageChange}
                    onPageSizeChange={this.controller.handlePageSizeChange}
                  />
                  <ColumnConfig
                    columns={allColumns}
                    columnsVisibility={columnsVisibility}
                    onVisibleChange={this.controller.handleColumnVisibleChange}
                    onOrderChange={this.controller.handleColumnOrderChange}
                  />
                </div>
              </div>
            </div>
          </Loading>
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(Grid)
