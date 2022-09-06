import _ from 'lodash'
import autoBind from 'auto-bind'

const LIMIT = 100

export default class BrowserController {
  async doLoadNodes() {
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
      nodes: [],
      filter: null,
      selectedId: null,
      selectedObject: null
    })
    this.context = context
  }

  async load() {
    const { nodes, filter, selectedId, selectedObject } =
      this.context.getState()

    const loadedNodes = await this.doLoadNodes({
      node: null,
      filter: filter,
      offset: 0,
      limit: LIMIT
    })

    let newNodes = this._createNodes(loadedNodes)
    newNodes = await this._setNodesExpanded(
      newNodes,
      this._getExpandedNodes(nodes),
      true
    )
    newNodes = await this._setNodesSelected(
      newNodes,
      selectedId,
      selectedObject
    )

    this.loadedNodes = loadedNodes

    await this.context.setState({
      loaded: true,
      nodes: newNodes
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

  async filterChange(newFilter) {
    await this.context.setState({
      filter: newFilter
    })
    await this.load()
  }

  async nodeExpand(nodeId) {
    const { nodes } = this.context.getState()

    const newNodes = await this._setNodesExpanded(
      nodes,
      { [nodeId]: nodeId },
      true
    )

    this.context.setState({
      nodes: newNodes
    })
  }

  async nodeCollapse(nodeId) {
    const { nodes } = this.context.getState()

    const newNodes = await this._setNodesExpanded(
      nodes,
      { [nodeId]: nodeId },
      false
    )

    this.context.setState({
      nodes: newNodes
    })
  }

  async nodeSelect(nodeId) {
    const { nodes } = this.context.getState()
    const { onSelectedChange } = this.context.getProps()

    let nodeObject = null

    this._visitNodes(nodes, node => {
      if (node.id === nodeId) {
        nodeObject = node.object
      }
    })

    const newNodes = await this._setNodesSelected(nodes, nodeId, nodeObject)

    await this.context.setState({
      nodes: newNodes,
      selectedId: nodeId,
      selectedObject: nodeObject
    })

    if (onSelectedChange) {
      onSelectedChange(nodeObject)
    }
  }

  async objectSelect(object) {
    const { nodes } = this.context.getState()

    const newNodes = await this._setNodesSelected(nodes, null, object)

    await this.context.setState({
      nodes: newNodes,
      selectedId: null,
      selectedObject: object
    })
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

  async _setNodesExpanded(nodes, nodeIds, expanded) {
    const { filter } = this.context.getState()
    return await this._modifyNodes(nodes, async node => {
      if (nodeIds[node.id]) {
        if (expanded && !node.loaded) {
          const children = await this.doLoadNodes({
            node,
            filter,
            offset: 0,
            limit: LIMIT
          })
          return {
            ...node,
            loaded: true,
            expanded: true,
            children
          }
        } else {
          return {
            ...node,
            expanded
          }
        }
      } else {
        return node
      }
    })
  }

  async _setNodesSelected(nodes, selectedId, selectedObject) {
    return await this._modifyNodes(nodes, async node => {
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

  _modifyNodes = async (nodes, modifyFn) => {
    if (!nodes) {
      return nodes
    }

    let newNodes = []
    let modified = false

    for (let i = 0; i < nodes.length; i++) {
      const node = nodes[i]

      let newNode = await modifyFn(node)

      const newChildren = await this._modifyNodes(newNode.children, modifyFn)

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
    }

    return modified ? newNodes : nodes
  }
}
