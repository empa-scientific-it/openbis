import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Table from '@material-ui/core/Table'
import TableHead from '@material-ui/core/TableHead'
import TableBody from '@material-ui/core/TableBody'
import Header from '@src/js/components/common/form/Header.jsx'
import GridController from '@src/js/components/common/grid/GridController.js'
import GridFilters from '@src/js/components/common/grid/GridFilters.jsx'
import GridHeaders from '@src/js/components/common/grid/GridHeaders.jsx'
import GridMultiselectionRow from '@src/js/components/common/grid/GridMultiselectionRow.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridActions from '@src/js/components/common/grid/GridActions.jsx'
import GridExports from '@src/js/components/common/grid/GridExports.jsx'
import GridPaging from '@src/js/components/common/grid/GridPaging.jsx'
import GridConfig from '@src/js/components/common/grid/GridConfig.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  loadingContainer: {
    flex: '1 1 auto'
  },
  tableContainer: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%'
  },
  tableHeaderAndBody: {
    height: '1px',
    width: '100%',
    overflow: 'auto',
    flex: '1 1 auto'
  },
  table: {
    borderCollapse: 'unset',
    marginTop: -theme.spacing(1)
  },
  tableHead: {
    position: 'sticky',
    top: '-8px',
    zIndex: '200',
    backgroundColor: theme.palette.background.paper
  },
  tableBody: {
    '& tr:last-child td': {
      border: 0
    }
  },
  tableFooter: {
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

    const {
      header,
      selectable,
      multiselectable,
      actions,
      onRowClick,
      classes
    } = this.props
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
        <div className={classes.loadingContainer}>
          <Loading loading={loading}>
            <div
              className={classes.tableContainer}
              onClick={this.handleClickTable}
            >
              <div className={classes.tableHeaderAndBody}>
                <Table classes={{ root: classes.table }}>
                  <TableHead classes={{ root: classes.tableHead }}>
                    <GridFilters
                      columns={visibleColumns}
                      filters={filters}
                      onFilterChange={this.controller.handleFilterChange}
                      multiselectable={multiselectable}
                    />
                    <GridHeaders
                      columns={visibleColumns}
                      rows={rows}
                      sort={sort}
                      sortDirection={sortDirection}
                      onSortChange={this.controller.handleSortChange}
                      onMultiselectAllRowsChange={
                        this.controller.handleMultiselectAllRowsChange
                      }
                      multiselectable={multiselectable}
                      multiselectedRows={multiselectedRows}
                    />
                  </TableHead>
                  <TableBody classes={{ root: classes.tableBody }}>
                    <GridMultiselectionRow
                      columns={visibleColumns}
                      rows={rows}
                      onMultiselectionClear={
                        this.controller.handleMultiselectionClear
                      }
                      multiselectable={multiselectable}
                      multiselectedRows={multiselectedRows}
                    />
                    {rows.map(row => {
                      return (
                        <GridRow
                          key={row.id}
                          columns={visibleColumns}
                          row={row}
                          clickable={onRowClick ? true : false}
                          selectable={selectable}
                          selected={
                            selectedRow ? selectedRow.id === row.id : false
                          }
                          multiselectable={multiselectable}
                          multiselected={
                            multiselectedRows && multiselectedRows[row.id]
                          }
                          onClick={this.controller.handleRowClick}
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
                    multiselectable={multiselectable}
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
                  <GridConfig
                    columns={allColumns}
                    columnsVisibility={columnsVisibility}
                    loading={loading}
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
