import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import ContentTabs from '@src/js/components/common/content/ContentTabs.jsx'
import AppController from '@src/js/components/AppController.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    width: '100px',
    zIndex: 200,
    overflow: 'auto'
  },
  component: {
    height: 0,
    flex: '1 1 100%',
    overflow: 'auto'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none'
  }
}

class Content extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleTabSelect(tab) {
    AppController.getInstance().objectOpen(
      this.props.page,
      tab.object.type,
      tab.object.id
    )
  }

  handleTabClose(tab) {
    AppController.getInstance().objectClose(
      this.props.page,
      tab.object.type,
      tab.object.id
    )
  }

  render() {
    logger.log(logger.DEBUG, 'Content.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <ContentTabs
          tabs={this.props.openTabs}
          selectedTab={this.props.selectedTab}
          tabSelect={this.handleTabSelect}
          tabClose={this.handleTabClose}
          renderTab={this.props.renderTab}
        />
        {this.props.openTabs.map(openTab => {
          let ObjectComponent = this.props.renderComponent(openTab)
          if (ObjectComponent) {
            let visible = _.isEqual(openTab, this.props.selectedTab)
            return (
              <div
                key={openTab.id}
                className={util.classNames(
                  classes.component,
                  visible ? classes.visible : classes.hidden
                )}
              >
                <ErrorBoundary>{ObjectComponent}</ErrorBoundary>
              </div>
            )
          }
        })}
      </div>
    )
  }
}

export default _.flow(
  withStyles(styles),
  AppController.getInstance().withState(ownProps => ({
    openTabs: AppController.getInstance().getOpenTabs(ownProps.page),
    selectedTab: AppController.getInstance().getSelectedTab(ownProps.page)
  }))
)(Content)
