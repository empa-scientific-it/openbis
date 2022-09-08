import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNode from '@src/js/components/common/browser2/BrowserNode.jsx'
import messages from '@src/js/common/messages.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const PADDING_PER_LEVEL = 24

const styles = theme => ({
  item: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1)
  },
  icon: {
    margin: '-2px 4px -2px 8px',
    minWidth: '24px'
  },
  text: {
    fontSize: theme.typography.body2.fontSize,
    lineHeight: theme.typography.body2.fontSize
  },
  listContainer: {
    flex: '1 1 100%'
  },
  list: {
    paddingTop: '0',
    paddingBottom: '0'
  }
})

class BrowserNodeClass extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
    this.handleExpand = this.handleExpand.bind(this)
    this.handleCollapse = this.handleCollapse.bind(this)
  }

  handleClick() {
    const { controller, node } = this.props
    controller.nodeSelect(node.id)
  }

  handleExpand(event) {
    const { controller, node } = this.props
    event.stopPropagation()
    controller.nodeExpand(node.id)
  }

  handleCollapse(event) {
    const { controller, node } = this.props
    event.stopPropagation()
    controller.nodeCollapse(node.id)
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNode.render')

    const { node, level, classes } = this.props

    return (
      <div>
        <ListItem
          button
          selected={node.selected}
          onClick={this.handleClick}
          style={{ paddingLeft: level * PADDING_PER_LEVEL + 'px' }}
          classes={{
            root: classes.item
          }}
        >
          {this.renderIcon(node)}
          {this.renderText(node)}
        </ListItem>
        {this.renderChildren()}
      </div>
    )
  }

  renderIcon(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderIcon')

    const { classes } = this.props

    if (node.canHaveChildren || (node.children && node.children.length > 0)) {
      let icon = null
      if (node.expanded) {
        icon = <ExpandMoreIcon onClick={this.handleCollapse} />
      } else {
        icon = <ChevronRightIcon onClick={this.handleExpand} />
      }
      return (
        <ListItemIcon
          classes={{
            root: classes.icon
          }}
        >
          {icon}
        </ListItemIcon>
      )
    } else {
      return (
        <ListItemIcon
          classes={{
            root: classes.icon
          }}
        >
          <span></span>
        </ListItemIcon>
      )
    }
  }

  renderText(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderText "' + node.text + '"')

    const { classes } = this.props

    return (
      <ListItemText
        primary={node.text}
        classes={{
          primary: classes.text
        }}
      />
    )
  }

  renderChildren() {
    const { node, level, classes } = this.props

    if (!node.canHaveChildren || !node.loaded) {
      return null
    }

    return (
      <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
        <List
          className={util.classNames(
            classes.list,
            level === 0 ? classes.listContainer : null
          )}
        >
          {this.renderNonEmptyChildren()}
          {this.renderShowMoreChildren()}
          {this.renderEmptyChildren()}
        </List>
      </Collapse>
    )
  }

  renderNonEmptyChildren() {
    const { controller, node, level } = this.props

    if (node.children && node.children.length > 0) {
      return node.children.map(child => {
        return (
          <BrowserNode
            key={child.id}
            controller={controller}
            node={child}
            level={level + 1}
          />
        )
      })
    } else {
      return null
    }
  }

  renderShowMoreChildren() {
    const { node, level, classes } = this.props

    if (
      node.children &&
      node.totalCount &&
      node.children.length < node.totalCount
    ) {
      return (
        <ListItem
          button
          style={{ paddingLeft: (level + 1) * PADDING_PER_LEVEL + 'px' }}
          classes={{
            root: classes.item
          }}
        >
          <ListItemIcon
            classes={{
              root: classes.icon
            }}
          >
            <span></span>
          </ListItemIcon>
          <ListItemText
            primary={messages.get(
              messages.LOAD_MORE,
              node.totalCount - node.children.length
            )}
            classes={{
              primary: classes.text
            }}
          />
        </ListItem>
      )
    } else {
      return null
    }
  }

  renderEmptyChildren() {
    const { node, level, classes } = this.props

    if (!node.children || node.children.length === 0) {
      return (
        <ListItem
          button
          style={{ paddingLeft: (level + 1) * PADDING_PER_LEVEL + 'px' }}
          classes={{
            root: classes.item
          }}
        >
          <ListItemIcon
            classes={{
              root: classes.icon
            }}
          >
            <span></span>
          </ListItemIcon>
          <ListItemText
            primary={messages.get(messages.LOADED_EMPTY)}
            classes={{
              primary: classes.text
            }}
          />
        </ListItem>
      )
    } else {
      return null
    }
  }
}

export default _.flow(withStyles(styles))(BrowserNodeClass)
