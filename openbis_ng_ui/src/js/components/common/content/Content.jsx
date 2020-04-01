import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import actions from '@src/js/store/actions/actions.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

import ContentTabs from './ContentTabs.jsx'

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    width: '100px',
    zIndex: 1000
  },
  component: {
    height: 0,
    flex: '1 1 100%'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none'
  }
}

function mapStateToProps() {
  const getSelectedObject = selectors.createGetSelectedObject()
  return (state, ownProps) => {
    return {
      openObjects: selectors.getOpenObjects(state, ownProps.page),
      changedObjects: selectors.getChangedObjects(state, ownProps.page),
      selectedObject: getSelectedObject(state, ownProps.page)
    }
  }
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    objectSelect: (type, id) => {
      dispatch(actions.objectOpen(ownProps.page, type, id))
    },
    objectClose: (type, id) => {
      dispatch(actions.objectClose(ownProps.page, type, id))
    }
  }
}

class Content extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Content.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <ContentTabs
          objects={this.props.openObjects}
          changedObjects={this.props.changedObjects}
          selectedObject={this.props.selectedObject}
          objectSelect={this.props.objectSelect}
          objectClose={this.props.objectClose}
          renderTab={this.props.renderTab}
        />
        {this.props.openObjects.map(object => {
          let ObjectComponent = this.props.renderComponent(object)
          if (ObjectComponent) {
            let key = object.type + '/' + object.id
            let visible = _.isEqual(object, this.props.selectedObject)
            return (
              <div
                key={key}
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
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(Content)
