import React from 'react'
import { Resizable } from 're-resizable'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import BrowserRoot from '@src/js/components/common/browser2/BrowserRoot.jsx'
import BrowserNode from '@src/js/components/common/browser2/BrowserNode.jsx'
import BrowserNodeAutoShowSelected from '@src/js/components/common/browser2/BrowserNodeAutoShowSelected.jsx'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const styles = theme => ({
  resizable: {
    zIndex: 100,
    position: 'relative'
  },
  browser: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    borderRight: `1px solid ${theme.palette.border.primary}`
  },
  header: {},
  footer: {},
  filterButtons: {
    marginLeft: '-12px',
    marginRight: '16px',
    display: 'flex'
  },
  filterButton: {
    marginLeft: '16px'
  },
  nodes: {
    height: '100%',
    overflow: 'auto'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none'
  }
})

class Browser extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.controller = props.controller
    this.controller.init(new ComponentContext(this))
  }

  async componentDidMount() {
    await this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    const { classes } = this.props

    return (
      <Resizable
        defaultSize={{
          width: '25%',
          height: '100%'
        }}
        enable={{
          right: true,
          left: false,
          top: false,
          bottom: false,
          topRight: false,
          bottomRight: false,
          bottomLeft: false,
          topLeft: false
        }}
        className={classes.resizable}
      >
        {this.renderBrowser()}
      </Resizable>
    )
  }

  renderBrowser() {
    const { controller } = this
    const { renderHeader, renderFooter, classes } = this.props

    if (!controller.isLoaded()) {
      return (
        <div className={classes.browser}>
          <FilterField filter={controller.getFilter() || ''} loading={true} />
          <BrowserRoot rootNode={controller.getNodeSetAsRoot()} />
        </div>
      )
    }

    const fullTree = controller.getFullTree()
    const filteredTree = controller.getFilteredTree()

    return (
      <div className={classes.browser}>
        {renderHeader && <div className={classes.header}>{renderHeader()}</div>}
        {this.renderFilter()}
        <BrowserRoot
          rootNode={controller.getNodeSetAsRoot()}
          onRootChange={node => {
            controller.setNodeAsRoot(node)
          }}
          onRootClear={() => {
            controller.setNodeAsRoot(null)
          }}
        />
        {fullTree && (
          <div
            className={util.classNames(
              classes.nodes,
              !controller.isLoading() && controller.isFullTreeVisible()
                ? classes.visible
                : classes.hidden
            )}
          >
            <BrowserNode controller={controller} node={fullTree} level={-1} />
          </div>
        )}
        {filteredTree && (
          <div
            className={util.classNames(
              classes.nodes,
              !controller.isLoading() && controller.isFilteredTreeVisible()
                ? classes.visible
                : classes.hidden
            )}
          >
            <BrowserNode
              controller={controller}
              node={filteredTree}
              level={-1}
            />
          </div>
        )}
        {renderFooter && <div className={classes.footer}>{renderFooter()}</div>}
      </div>
    )
  }

  renderFilter() {
    const { controller } = this
    const { classes } = this.props

    return (
      <FilterField
        filter={controller.getFilter() || ''}
        filterChange={controller.filterChange}
        filterClear={controller.filterClear}
        loading={controller.isLoading() || controller.isTreeLoading()}
        endAdornments={
          <div className={classes.filterButtons}>
            <div className={classes.filterButton}>
              <BrowserNodeAutoShowSelected
                value={controller.isAutoShowSelectedObject()}
                onClick={controller.changeAutoShowSelectedObject}
              />
            </div>
          </div>
        }
      />
    )
  }
}

export default withStyles(styles)(Browser)
