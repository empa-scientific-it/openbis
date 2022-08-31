import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNodes from '@src/js/components/common/browser/BrowserNodes.jsx'
import logger from '@src/js/common/logger.js'

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
  }
})

class BrowserNode extends React.PureComponent {
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

    const { controller, node, level, classes } = this.props

    return (
      <div>
        <ListItem
          button
          selected={node.selected}
          onClick={this.handleClick}
          style={{ paddingLeft: level * 24 + 'px' }}
          classes={{
            root: classes.item
          }}
        >
          {this.renderIcon(node)}
          {this.renderText(node)}
        </ListItem>
        {node.children && node.children.length > 0 && (
          <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
            <BrowserNodes
              controller={controller}
              nodes={node.children}
              level={level + 1}
            />
          </Collapse>
        )}
      </div>
    )
  }

  renderIcon(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderIcon')

    const { classes } = this.props

    if (node.children && node.children.length > 0) {
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
}

export default _.flow(withStyles(styles))(BrowserNode)
