import React from 'react'
import _ from 'lodash'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'
import pages from '@src/js/common/consts/pages.js'
import actions from '@src/js/store/actions/actions.js'
import selectors from '@src/js/store/selectors/selectors.js'

import Loading from '@src/js/components/common/loading/Loading.jsx'
import Error from '@src/js/components/common/error/Error.jsx'
import Menu from '@src/js/components/common/menu/Menu.jsx'

import Login from '@src/js/components/login/Login.jsx'
import Users from '@src/js/components/users/Users.jsx'
import Types from '@src/js/components/types/Types.jsx'
import Tools from '@src/js/components/tools/Tools.jsx'

const styles = {
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  page: {
    flex: '1 1 100%',
    display: 'flex',
    overflow: 'hidden'
  },
  visible: {
    display: 'flex'
  },
  hidden: {
    display: 'none'
  }
}

const pageToComponent = {
  [pages.TYPES]: Types,
  [pages.USERS]: Users,
  [pages.TOOLS]: Tools
}

function mapStateToProps(state) {
  return {
    loading: selectors.getLoading(state),
    session: selectors.getSession(state),
    currentPage: selectors.getCurrentPage(state),
    error: selectors.getError(state)
  }
}

function mapDispatchToProps(dispatch) {
  return {
    init: () => {
      dispatch(actions.init())
    },
    errorClosed: () => {
      dispatch(actions.errorChange(null))
    }
  }
}

class App extends React.Component {
  componentDidMount() {
    this.props.init()
  }

  render() {
    logger.log(logger.DEBUG, 'App.render')

    return (
      <Loading loading={this.props.loading}>
        <Error error={this.props.error} errorClosed={this.props.errorClosed}>
          {this.renderPage()}
        </Error>
      </Loading>
    )
  }

  renderPage() {
    const classes = this.props.classes

    if (this.props.session) {
      return (
        <div className={classes.container}>
          <Menu page={this.props.currentPage} />
          {_.map(pageToComponent, (PageComponent, page) => {
            let visible = this.props.currentPage === page
            return (
              <div
                key={page}
                className={util.classNames(
                  classes.page,
                  visible ? classes.visible : classes.hidden
                )}
              >
                <PageComponent />
              </div>
            )
          })}
        </div>
      )
    } else {
      return <Login disabled={this.props.loading} />
    }
  }
}

export default _.flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(App)
