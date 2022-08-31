import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Content from '@src/js/components/common/content/Content.jsx'
import DatabaseBrowser from '@src/js/components/database/browser/DatabaseBrowser.jsx'
import pages from '@src/js/common/consts/pages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Database extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'Database.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <DatabaseBrowser />
        <Content
          page={pages.DATABASE}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent() {
    return null
  }

  renderTab() {
    return null
  }
}

export default withStyles(styles)(Database)
