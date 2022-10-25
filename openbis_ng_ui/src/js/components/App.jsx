import React from 'react'
import _ from 'lodash'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'
import pages from '@src/js/common/consts/pages.js'

import Loading from '@src/js/components/common/loading/Loading.jsx'
import Error from '@src/js/components/common/error/Error.jsx'
import Menu from '@src/js/components/common/menu/Menu.jsx'

import Login from '@src/js/components/login/Login.jsx'
import Database from '@src/js/components/database/Database.jsx'
import Users from '@src/js/components/users/Users.jsx'
import Types from '@src/js/components/types/Types.jsx'
import Tools from '@src/js/components/tools/Tools.jsx'

import AppController from '@src/js/components/AppController.js'
import ComponentContext from '@src/js/components/common/ComponentContext.js'

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
  [pages.DATABASE]: Database,
  [pages.TYPES]: Types,
  [pages.USERS]: Users,
  [pages.TOOLS]: Tools
}

class App extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = AppController.getInstance()
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  handleErrorClosed() {
    AppController.getInstance().errorChange(null)
  }

  render() {
    logger.log(logger.DEBUG, 'App.render')

    return (
      <AppController.AppContext.Provider value={this.state}>
        <Loading loading={AppController.getInstance().getLoading()}>
          <Error
            error={AppController.getInstance().getError()}
            errorClosed={this.handleErrorClosed}
          >
            {AppController.getInstance().getLoaded() && this.renderPage()}
          </Error>
        </Loading>
      </AppController.AppContext.Provider>
    )
  }

  renderPage() {
    const classes = this.props.classes

    if (AppController.getInstance().getSession()) {
      return (
        <div className={classes.container}>
          <Menu />
          {_.map(pageToComponent, (PageComponent, page) => {
            let visible = AppController.getInstance().getCurrentPage() === page
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
      return <Login disabled={AppController.getInstance().getLoading()} />
    }
  }
}

export default _.flow(
  withStyles(styles),
  AppController.getInstance().withState()
)(App)
