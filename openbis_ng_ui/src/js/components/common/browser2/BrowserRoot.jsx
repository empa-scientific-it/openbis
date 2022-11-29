import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import AccountTreeIcon from '@material-ui/icons/AccountTreeOutlined'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  item: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: 0
  },
  icon: {
    margin: '-2px 4px -2px 8px',
    minWidth: '24px'
  },
  text: {
    fontSize: theme.typography.body2.fontSize,
    lineHeight: theme.typography.body2.fontSize
  },
  pathPart: {}
})

class BrowserRoot extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClear = this.handleClear.bind(this)
  }

  handleClear() {
    const { rootClear } = this.props
    if (rootClear) {
      rootClear()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserRoot.render')

    const { rootNode, classes } = this.props

    if (!rootNode) {
      return null
    }

    return (
      <ListItem
        button
        onClick={this.handleClear}
        classes={{
          root: classes.item
        }}
      >
        <ListItemIcon
          classes={{
            root: classes.icon
          }}
        >
          <AccountTreeIcon fontSize='small' />
        </ListItemIcon>
        <ListItemText
          primary={this.renderPath()}
          classes={{
            primary: classes.text
          }}
        />
      </ListItem>
    )
  }

  renderPath() {
    const { rootNode, classes } = this.props

    if (_.isEmpty(rootNode.path)) {
      return null
    }

    return rootNode.path.map((part, index) => (
      <span key={index} className={classes.path}>
        {part.text}
        {index < rootNode.path.length - 1 ? <ChevronRightIcon /> : null}
      </span>
    ))
  }
}

export default withStyles(styles)(BrowserRoot)
