import React from 'react'
import { Resizable } from 're-resizable'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import BrowserNode from '@src/js/components/common/browser2/BrowserNode.jsx'
import IconButton from '@material-ui/core/IconButton'
import UnfoldLessIcon from '@material-ui/icons/UnfoldLess'
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
  filterButtons: {
    marginRight: '8px'
  },
  filterButton: {
    padding: '4px'
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

  componentDidMount() {
    this.controller.load()
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
    const { classes } = this.props

    if (!this.state.loaded) {
      return (
        <div className={classes.browser}>
          <FilterField filter={''} loading={true} />
        </div>
      )
    }

    return (
      <div className={classes.browser}>
        <FilterField
          filter={controller.getFilter() || ''}
          filterChange={controller.filterChange}
          filterClear={controller.filterClear}
          loading={controller.isLoading()}
          endAdornments={
            <div className={classes.filterButtons}>
              <IconButton
                onClick={controller.collapseAllNodes}
                classes={{ root: classes.filterButton }}
              >
                <UnfoldLessIcon fontSize='small' />
              </IconButton>
            </div>
          }
        />
        <div
          className={util.classNames(
            classes.nodes,
            controller.isFullTreeVisible() ? classes.visible : classes.hidden
          )}
        >
          <BrowserNode
            controller={controller}
            node={controller.getFullTree()}
            level={-1}
          />
        </div>
        <div
          className={util.classNames(
            classes.nodes,
            controller.isFilteredTreeVisible()
              ? classes.visible
              : classes.hidden
          )}
        >
          <BrowserNode
            controller={controller}
            node={controller.getFilteredTree()}
            level={-1}
          />
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(Browser)
