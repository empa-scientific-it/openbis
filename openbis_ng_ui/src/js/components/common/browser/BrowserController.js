import _ from 'lodash'
import autoBind from 'auto-bind'
import actions from '@src/js/store/actions/actions.js'

export default class BrowserController {
  doGetPage() {
    throw 'Method not implemented'
  }

  doLoadNodes() {
    throw 'Method not implemented'
  }

  doNodeAdd() {
    throw 'Method not implemented'
  }

  doNodeRemove() {
    throw 'Method not implemented'
  }

  doGetObservedModifications() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)
    this.loadedNodes = []
    this.lastObjectModifications = {}
  }

  init(context) {
    context.initState({
      loaded: false,
      filter: '',
      nodes: [],
      selectedId: null,
      selectedObject: null,
      removeNodeDialogOpen: false
    })
    this.context = context
  }

  load() {
    return this.doLoadNodes().then(loadedNodes => {
      const {
        filter,
        nodes,
        selectedId,
        selectedObject
      } = this.context.getState()

      let newNodes = this._createNodes(loadedNodes)
      newNodes = this._filterNodes(newNodes, filter)
      newNodes = this._setNodesExpanded(
        newNodes,
        this._getExpandedNodes(nodes),
        true
      )
      newNodes = this._setNodesSelected(newNodes, selectedId, selectedObject)
      this._sortNodes(newNodes)

      this.loadedNodes = loadedNodes

      this.context.setState({
        loaded: true,
        nodes: newNodes
      })
    })
  }

  refresh(fullObjectModifications) {
    const observedModifications = this.doGetObservedModifications()

    const getTimestamp = (modifications, type, operation) => {
      return (
        (modifications &&
          modifications[type] &&
          modifications[type][operation]) ||
        0
      )
    }

    const newObjectModifications = {}
    let refresh = false

    Object.keys(observedModifications).forEach(observedType => {
      const observedOperations = observedModifications[observedType]

      newObjectModifications[observedType] = {}

      observedOperations.forEach(observedOperation => {
        const timestamp = getTimestamp(
          this.lastObjectModifications,
          observedType,
          observedOperation
        )
        const newTimestamp = getTimestamp(
          fullObjectModifications,
          observedType,
          observedOperation
        )

        newObjectModifications[observedType][observedOperation] = Math.max(
          timestamp,
          newTimestamp
        )

        if (newTimestamp > timestamp) {
          refresh = true
        }
      })
    })

    this.lastObjectModifications = newObjectModifications

    if (refresh) {
      this.load()
    }
  }

  filterChange(newFilter) {
    const {
      filter,
      nodes,
      selectedId,
      selectedObject
    } = this.context.getState()

    let initialNodes = null

    if (newFilter.startsWith(filter)) {
      initialNodes = nodes
    } else {
      initialNodes = this.loadedNodes
    }

    let newNodes = this._createNodes(initialNodes)
    newNodes = this._filterNodes(newNodes, newFilter)
    newNodes = this._setNodesExpanded(
      newNodes,
      this._getParentNodes(newNodes),
      true
    )
    newNodes = this._setNodesSelected(newNodes, selectedId, selectedObject)
    this._sortNodes(newNodes)

    this.context.setState({
      filter: newFilter,
      nodes: newNodes
    })
  }

  nodeExpand(nodeId) {
    const { nodes } = this.context.getState()

    const newNodes = this._setNodesExpanded(nodes, { [nodeId]: nodeId }, true)

    this.context.setState({
      nodes: newNodes
    })
  }

  nodeCollapse(nodeId) {
    const { nodes } = this.context.getState()

    const newNodes = this._setNodesExpanded(nodes, { [nodeId]: nodeId }, false)

    this.context.setState({
      nodes: newNodes
    })
  }

  nodeSelect(nodeId) {
    const { nodes } = this.context.getState()

    let nodeObject = null

    this._visitNodes(nodes, node => {
      if (node.id === nodeId) {
        nodeObject = node.object
      }
    })

    if (nodeObject) {
      this.context.dispatch(
        actions.objectOpen(this.getPage(), nodeObject.type, nodeObject.id)
      )
    }

    const newNodes = this._setNodesSelected(nodes, nodeId, nodeObject)

    this.context.setState({
      nodes: newNodes,
      selectedId: nodeId,
      selectedObject: nodeObject
    })
  }

  nodeAdd() {
    if (!this.isAddNodeEnabled()) {
      return
    }

    const selectedNode = this.getSelectedNode()
    this.doNodeAdd(selectedNode)
  }

  nodeRemove() {
    if (!this.isRemoveNodeEnabled()) {
      return
    }

    this.context.setState({
      removeNodeDialogOpen: true
    })
  }

  nodeRemoveConfirm() {
    const { removeNodeDialogOpen } = this.context.getState()

    if (!removeNodeDialogOpen) {
      return Promise.resolve()
    }

    const selectedNode = this.getSelectedNode()

    return this.doNodeRemove(selectedNode).then(() => {
      return this.context.setState({
        removeNodeDialogOpen: false
      })
    })
  }

  nodeRemoveCancel() {
    this.context.setState({
      removeNodeDialogOpen: false
    })
  }

  objectSelect(object) {
    const { nodes } = this.context.getState()

    const newNodes = this._setNodesSelected(nodes, null, object)

    this.context.setState({
      nodes: newNodes,
      selectedId: null,
      selectedObject: object
    })
  }

  getPage() {
    return this.doGetPage()
  }

  getLoaded() {
    const { loaded } = this.context.getState()
    return loaded
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
  }

  getNodes() {
    const { nodes } = this.context.getState()
    return nodes
  }

  getSelectedNode() {
    const { nodes, selectedId, selectedObject } = this.context.getState()

    let selectedNode = null

    this._visitNodes(nodes, node => {
      if (
        (selectedId && selectedId === node.id) ||
        (selectedObject && _.isEqual(selectedObject, node.object))
      ) {
        selectedNode = node
      }
    })

    return selectedNode
  }

  isAddNodeEnabled() {
    const selectedNode = this.getSelectedNode()
    return selectedNode && selectedNode.canAdd
  }

  isRemoveNodeEnabled() {
    const selectedNode = this.getSelectedNode()
    return selectedNode && selectedNode.canRemove
  }

  isRemoveNodeDialogOpen() {
    const { removeNodeDialogOpen } = this.context.getState()
    return removeNodeDialogOpen
  }

  _createNodes = nodes => {
    if (!nodes) {
      return []
    }

    const newNodes = []

    nodes.forEach(node => {
      const newNode = {
        ...node,
        selected: false,
        expanded: false,
        children: this._createNodes(node.children)
      }
      newNodes.push(newNode)
    })

    return newNodes
  }

  _getExpandedNodes(nodes) {
    return this._visitNodes(
      nodes,
      (node, result) => {
        if (node.expanded) {
          result[node.id] = node.id
        }
      },
      {}
    )
  }

  _getParentNodes(nodes) {
    return this._visitNodes(
      nodes,
      (node, result) => {
        if (node.children && node.children.length > 0) {
          result[node.id] = node.id
        }
      },
      {}
    )
  }

  _setNodesExpanded(nodes, nodeIds, expanded) {
    return this._modifyNodes(nodes, node => {
      if (nodeIds[node.id]) {
        return {
          ...node,
          expanded
        }
      } else {
        return node
      }
    })
  }

  _setNodesSelected(nodes, selectedId, selectedObject) {
    return this._modifyNodes(nodes, node => {
      if (
        (selectedId && selectedId === node.id) ||
        (selectedObject && _.isEqual(selectedObject, node.object))
      ) {
        return {
          ...node,
          selected: true
        }
      } else if (node.selected) {
        return {
          ...node,
          selected: false
        }
      } else {
        return node
      }
    })
  }

  _sortNodes = (nodes, level = 0) => {
    if (!nodes) {
      return
    }
    if (level > 0) {
      nodes.sort((n1, n2) => {
        return n1.text.localeCompare(n2.text)
      })
    }
    nodes.forEach(node => {
      this._sortNodes(node.children, level + 1)
    })
  }

  _visitNodes = (nodes, visitFn, results = []) => {
    if (nodes) {
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i]

        visitFn(node, results)

        this._visitNodes(node.children, visitFn, results)
      }
    }
    return results
  }

  _filterNodes = (nodes, filter, ascendantMatches = false) => {
    if (!nodes || filter.trim().length === 0) {
      return nodes
    }

    let newNodes = []

    nodes.forEach(node => {
      const nodeMatches =
        node.canMatchFilter &&
        node.text &&
        node.text.toLowerCase().indexOf(filter.trim().toLowerCase()) !== -1

      const newNode = {
        ...node
      }

      newNode.children = this._filterNodes(
        node.children,
        filter,
        ascendantMatches || nodeMatches
      )

      if (
        ascendantMatches ||
        nodeMatches ||
        (newNode.children && newNode.children.length > 0)
      ) {
        newNodes.push(newNode)
      }
    })

    return newNodes
  }

  _modifyNodes = (nodes, modifyFn) => {
    if (!nodes) {
      return nodes
    }

    let newNodes = []
    let modified = false

    nodes.forEach(node => {
      let newNode = modifyFn(node)

      const newChildren = this._modifyNodes(newNode.children, modifyFn)

      if (newNode === node) {
        if (newChildren !== node.children) {
          newNode = {
            ...node,
            children: newChildren
          }
        }
      } else {
        newNode.children = newChildren
      }

      newNodes.push(newNode)

      if (newNode !== node) {
        modified = true
      }
    })

    return modified ? newNodes : nodes
  }
}
