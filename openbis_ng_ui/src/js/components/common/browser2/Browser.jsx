import React from 'react'
import { Resizable } from 're-resizable'
import { withStyles } from '@material-ui/core/styles'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import BrowserNode from '@src/js/components/common/browser2/BrowserNode.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  resizable: {
    zIndex: 100,
    position: 'relative'
  },
  paper: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    borderRight: `1px solid ${theme.palette.border.primary}`
  },
  nodes: {
    height: '100%',
    overflow: 'auto'
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

    const { controller } = this

    if (!controller.getRoot().loaded) {
      return null
    }

    const { classes } = this.props

    return (
      <Resizable
        defaultSize={{
          width: 300,
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
        <div className={classes.paper}>
          <FilterField
            filter={controller.getFilter() || ''}
            filterChange={controller.filterChange}
            loading={controller.getRoot().loading}
          />
          <div className={classes.nodes}>
            <BrowserNode
              controller={controller}
              node={controller.getRoot()}
              level={-1}
            />
          </div>
        </div>
      </Resizable>
    )
  }
}

export default withStyles(styles)(Browser)
