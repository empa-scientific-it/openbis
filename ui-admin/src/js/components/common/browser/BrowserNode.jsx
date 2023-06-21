import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import { Draggable, Droppable } from 'react-beautiful-dnd'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import CircularProgress from '@material-ui/core/CircularProgress'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import Collapse from '@material-ui/core/Collapse'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNode from '@src/js/components/common/browser/BrowserNode.jsx'
import BrowserNodeSetAsRoot from '@src/js/components/common/browser/BrowserNodeSetAsRoot.jsx'
import BrowserNodeSortings from '@src/js/components/common/browser/BrowserNodeSortings.jsx'
import BrowserNodeCollapseAll from '@src/js/components/common/browser/BrowserNodeCollapseAll.jsx'
import BrowserNodeLoadMore from '@src/js/components/common/browser/BrowserNodeLoadMore.jsx'
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
    },
    '&:hover $textContainer': {
      overflow: 'hidden'
    }
  },
  icon: {
    margin: '-2px 4px -2px 8px',
    minWidth: '24px'
  },
  textContainer: {},
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
  selected: {
    backgroundColor: theme.palette.background.primary
  }
})

class BrowserNodeClass extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.references = {
      node: React.createRef(),
      text: React.createRef(),
      loadMore: React.createRef()
    }
  }

  handleClick() {
    const { controller, node } = this.props
    if (node.selectable) {
      controller.selectObject(node.object)
    } else if (node.onClick) {
      node.onClick()
    }
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

  handleCustomSortingClear(nodeId) {
    const { controller } = this.props
    controller.clearCustomSorting(nodeId)
  }

  handleCollapseAll(nodeId) {
    const { controller } = this.props
    controller.collapseAllNodes(nodeId)
  }

  handleUndoCollapseAll(nodeId) {
    const { controller } = this.props
    controller.undoCollapseAllNodes(nodeId)
  }

  handleSetAsRoot(node) {
    const { controller } = this.props
    controller.setNodeAsRoot(node.id)
  }

  componentDidMount() {
    this.componentDidUpdate()
  }

  componentDidUpdate() {
    this.renderDOMNode()
    this.scrollTo()
  }

  scrollTo() {
    const {
      node: { object, scrollTo }
    } = this.props

    if (!scrollTo) {
      return
    }

    let element = null

    if (_.isEqual(object, scrollTo.object)) {
      element = this.references.node.current
    } else {
      element = this.references.loadMore.current
    }

    if (element && element !== this.scrollToElement) {
      this.scrollToElement = element
      setTimeout(() => {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' })
        element.classList.add(this.props.classes.selected)
        setTimeout(() => {
          element.classList.remove(this.props.classes.selected)
          this.scrollToElement = null
        }, 1500)
      }, 500)
    }

    scrollTo.clear()
  }

  renderDOMNode() {
    const renderDOMNode = this.props.node.renderDOM || this.props.renderDOMNode

    if (renderDOMNode && this.references.text.current) {
      renderDOMNode({
        container: this.references.text.current,
        node: this.props.node
      })
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
            ref={this.references.node}
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
    logger.log(logger.DEBUG, 'BrowserNode.renderText')

    let text = null

    if (!_.isNil(node.renderDOM)) {
      text = <div ref={this.references.text}></div>
    } else if (!_.isNil(node.render)) {
      text = node.render({
        node
      })
    } else if (!_.isNil(this.props.renderDOMNode)) {
      text = <div ref={this.references.text}></div>
    } else if (!_.isNil(this.props.renderNode)) {
      text = this.props.renderNode({
        node
      })
    } else {
      text = node.text
    }

    return (
      <ListItemText
        primary={text}
        classes={{
          root: this.props.classes.textContainer,
          primary: this.props.classes.text
        }}
      />
    )
  }

  renderOptions(node) {
    const { controller, classes } = this.props

    return (
      <div className={classes.options}>
        <BrowserNodeSetAsRoot node={node} onClick={this.handleSetAsRoot} />
        <BrowserNodeSortings
          node={node}
          onChange={this.handleSortingChange}
          onClearCustom={this.handleCustomSortingClear}
        />
        <BrowserNodeCollapseAll
          node={node}
          canUndo={controller.canUndoCollapseAllNodes(node.id)}
          onCollapseAll={this.handleCollapseAll}
          onUndoCollapseAll={this.handleUndoCollapseAll}
        />
      </div>
    )
  }

  renderChildren() {
    const { node, level, classes } = this.props

    if (!node.canHaveChildren) {
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
    const { node } = this.props

    if (!_.isEmpty(node.children)) {
      return (
        <Droppable droppableId={node.id} type={node.id}>
          {provided => (
            <div ref={provided.innerRef} {...provided.droppableProps}>
              {node.children.map((child, index) =>
                this.renderNonEmptyChild(child, index)
              )}
              {provided.placeholder}
            </div>
          )}
        </Droppable>
      )
    } else {
      return null
    }
  }

  renderNonEmptyChild(child, index) {
    const { node, renderNode, renderDOMNode, level, controller } = this.props

    return (
      <Draggable
        key={child.id}
        draggableId={child.id}
        isDragDisabled={!child.draggable}
        type={node.id}
        index={index}
      >
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            {...provided.dragHandleProps}
          >
            <BrowserNode
              key={child.id}
              controller={controller}
              node={child}
              renderNode={renderNode}
              renderDOMNode={renderDOMNode}
              level={level + 1}
            />
          </div>
        )}
      </Draggable>
    )
  }

  renderShowMoreChildren() {
    const { node, level, classes } = this.props

    if (this.hasMoreChildren(node)) {
      return (
        <ListItem
          ref={this.references.loadMore}
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
              <BrowserNodeLoadMore
                count={
                  _.isNil(node.childrenLoadRepeatLimitForFullBatch)
                    ? node.childrenTotalCount - node.childrenLoadOffset
                    : null
                }
              />
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

    if (!this.hasChildren(node) && !this.hasMoreChildren(node)) {
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

  hasChildren(node) {
    return !_.isEmpty(node.children)
  }

  hasMoreChildren(node) {
    return (
      !_.isNil(node.childrenLoadOffset) &&
      !_.isNil(node.childrenLoadLimit) &&
      node.childrenLoadOffset < node.childrenTotalCount
    )
  }
}

export default _.flow(withStyles(styles))(BrowserNodeClass)
