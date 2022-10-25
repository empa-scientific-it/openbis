import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import CircularProgress from '@material-ui/core/CircularProgress'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import Collapse from '@material-ui/core/Collapse'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNode from '@src/js/components/common/browser2/BrowserNode.jsx'
import BrowserNodeSortings from '@src/js/components/common/browser2/BrowserNodeSortings.jsx'
import BrowserNodeCollapseAll from '@src/js/components/common/browser2/BrowserNodeCollapseAll.jsx'
import messages from '@src/js/common/messages.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const PADDING_PER_LEVEL = 24

const styles = theme => ({
  item: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    '&:hover $options': {
      visibility: 'visible'
    }
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
  },
  options: {
    visibility: 'hidden',
    display: 'flex'
  },
  option: {
    paddingLeft: '16px'
  },
  selected: {
    backgroundColor: theme.palette.background.primary
  }
})

class BrowserNodeClass extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
    this.handleExpand = this.handleExpand.bind(this)
    this.handleCollapse = this.handleCollapse.bind(this)
    this.handleLoadMore = this.handleLoadMore.bind(this)
    this.handleSortingChange = this.handleSortingChange.bind(this)
    this.handleCollapseAll = this.handleCollapseAll.bind(this)
    this.references = {
      node: React.createRef(),
      loadMore: React.createRef()
    }
  }

  handleClick() {
    const { controller, node } = this.props
    controller.selectObject(node.object)
  }

  handleExpand(event) {
    const { controller, node } = this.props
    event.stopPropagation()
    controller.expandNode(node.id)
  }

  handleCollapse(event) {
    const { controller, node } = this.props
    event.stopPropagation()
    controller.collapseNode(node.id)
  }

  handleLoadMore() {
    const { controller, node } = this.props
    controller.loadMoreNodes(node.id)
  }

  handleSortingChange(nodeId, sortingId) {
    const { controller } = this.props
    controller.changeSorting(nodeId, sortingId)
  }

  handleCollapseAll(nodeId) {
    const { controller } = this.props
    controller.collapseAllNodes(nodeId)
  }

  componentDidMount() {
    this.componentDidUpdate(null)
  }

  componentDidUpdate(prevProps) {
    const prevScrollTo = prevProps ? prevProps.node.scrollTo : null
    const scrollTo = this.props.node.scrollTo

    if (scrollTo && scrollTo !== prevScrollTo) {
      const element = this.references[scrollTo.ref].current
      if (element) {
        setTimeout(() => {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' })
          element.classList.add(this.props.classes.selected)
          setTimeout(() => {
            element.classList.remove(this.props.classes.selected)
          }, 1500)
        }, 500)
      }
      scrollTo.clear()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNode.render')

    const { node, level, classes } = this.props

    if (level === -1) {
      return node.expanded ? this.renderChildren() : null
    } else {
      return (
        <React.Fragment>
          <ListItem
            ref={this.references['node']}
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
            {this.renderOptions(node)}
          </ListItem>
          <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
            {this.renderChildren()}
          </Collapse>
        </React.Fragment>
      )
    }
  }

  renderIcon(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderIcon')

    const { classes } = this.props

    if (node.canHaveChildren) {
      let icon = null

      if (node.loading) {
        icon = <CircularProgress size={20} />
      } else if (node.expanded) {
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

  renderOptions(node) {
    const { classes } = this.props

    return (
      <div className={classes.options}>
        <div className={classes.option}>
          <BrowserNodeSortings
            node={node}
            onChange={this.handleSortingChange}
          />
        </div>
        <div className={classes.option}>
          <BrowserNodeCollapseAll
            node={node}
            onClick={this.handleCollapseAll}
          />
        </div>
      </div>
    )
  }

  renderChildren() {
    const { node, level, classes } = this.props

    if (!node.canHaveChildren || !node.loaded) {
      return null
    }

    return (
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
    )
  }

  renderNonEmptyChildren() {
    const { controller, node, level } = this.props

    if (!_.isEmpty(node.children)) {
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
          ref={this.references['loadMore']}
          button
          onClick={this.handleLoadMore}
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
            {node.loading ? <CircularProgress size={20} /> : <span></span>}
          </ListItemIcon>
          <ListItemText
            primary={
              <span>
                {messages.get(
                  messages.LOAD_MORE,
                  node.totalCount - node.children.length
                )}
              </span>
            }
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
