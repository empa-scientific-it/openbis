import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Content from '@src/js/components/common/content/Content.jsx'
import ContentObjectTab from '@src/js/components/common/content/ContentObjectTab.jsx'
import ContentSearchTab from '@src/js/components/common/content/ContentSearchTab.jsx'

import UsersBrowser from './browser/UsersBrowser.jsx'
import User from './user/User.jsx'
import Group from './group/Group.jsx'
import Search from './search/Search.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Users extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Users.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <UsersBrowser />
        <Content
          page={pages.USERS}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(object) {
    if (object.type === objectType.USER) {
      return <User objectId={object.id} />
    } else if (object.type === objectType.GROUP) {
      return <Group objectId={object.id} />
    } else if (object.type === objectType.SEARCH) {
      return <Search objectId={object.id} />
    }
  }

  renderTab(object, changed) {
    if (object.type === objectType.USER || object.type === objectType.GROUP) {
      return <ContentObjectTab object={object} changed={changed} />
    } else if (object.type === objectType.SEARCH) {
      return <ContentSearchTab object={object} />
    }
  }
}

export default withStyles(styles)(Users)
