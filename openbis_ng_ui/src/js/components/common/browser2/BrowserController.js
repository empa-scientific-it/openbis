import _ from 'lodash'
import autoBind from 'auto-bind'
import util from '@src/js/common/util.js'

const LOAD_LIMIT = 100
const LOAD_SILENT_PERIOD = 500

export default class BrowserController {
  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)
    this.lastLoadTimeoutId = null
    this.lastLoadPromise = null
    this.lastObjectModifications = {}
  }

  init(context) {
    context.initState({
      loaded: false,
      loading: false,
      root: {
        id: 'root',
        text: 'Root',
        object: {
          id: 'root',
          type: 'root'
        },
        canHaveChildren: true
      },
      filter: null,
      selectedId: null,
      selectedObject: null
    })
    this.context = context
  }

  async load() {
    const { root } = this.context.getState()
    this.loadNode(root)
  }

  async loadNode(node) {
    const { root, filter } = this.context.getState()

    if (this.lastLoadTimeoutId) {
      clearTimeout(this.lastLoadTimeoutId)
    }

    if (this.lastLoadPromise) {
      this.lastLoadPromise = null
    }

    this.context.setState({
      loading: true
    })

    this.lastLoadTimeoutId = setTimeout(() => {
      const loadPromise = this.doLoadNodes({
        node: node,
        filter: filter,
        offset: 0,
        limit: LOAD_LIMIT
      })

      this.lastLoadPromise = loadPromise

      return loadPromise
        .then(async loadedNodes => {
          if (loadPromise !== this.lastLoadPromise) {
            return
          }

          const { selectedId, selectedObject } = this.context.getState()

          let newChildren = loadedNodes.nodes
          let totalCount = loadedNodes.totalCount

          newChildren = await this._setNodesExpanded(
            newChildren,
            this._getExpandedNodes([node]),
            true
          )

          newChildren = await this._setNodesSelected(
            newChildren,
            selectedId,
            selectedObject
          )

          const [newRoot] = await this._modifyNodes([root], originalNode => {
            if (originalNode === node) {
              return {
                ...originalNode,
                children: newChildren,
                totalCount: totalCount
              }
            } else {
              return originalNode
            }
          })

          await this.context.setState({
            loaded: true,
            root: newRoot
          })
        })
        .finally(() => {
          this.context.setState({
            loading: false
          })
        })
    }, LOAD_SILENT_PERIOD)
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
      filter: util.trim(newFilter)
    })
    await this.load()
  }

  async nodeExpand(nodeId) {
    const { root } = this.context.getState()

    const [newRoot] = await this._setNodesExpanded(
      [root],
      { [nodeId]: nodeId },
      true
    )

    this.context.setState({
      root: newRoot
    })
  }

  async nodeCollapse(nodeId) {
    const { root } = this.context.getState()

    const [newRoot] = await this._setNodesExpanded(
      [root],
      { [nodeId]: nodeId },
      false
    )

    this.context.setState({
      root: newRoot
    })
  }

  async nodeSelect(nodeId) {
    const { root } = this.context.getState()
    const { onSelectedChange } = this.context.getProps()

    let nodeObject = null

    this._visitNodes([root], node => {
      if (node.id === nodeId) {
        nodeObject = node.object
      }
    })

    const [newRoot] = await this._setNodesSelected([root], nodeId, nodeObject)

    await this.context.setState({
      root: newRoot,
      selectedId: nodeId,
      selectedObject: nodeObject
    })

    if (onSelectedChange) {
      onSelectedChange(nodeObject)
    }
  }

  async objectSelect(object) {
    const { root } = this.context.getState()

    const [newRoot] = await this._setNodesSelected([root], null, object)

    await this.context.setState({
      root: newRoot,
      selectedId: null,
      selectedObject: object
    })
  }

  getLoaded() {
    const { loaded } = this.context.getState()
    return loaded
  }

  getLoading() {
    const { loading } = this.context.getState()
    return loading
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
  }

  getRoot() {
    const { root } = this.context.getState()
    return root
  }

  getSelectedNode() {
    const { root, selectedId, selectedObject } = this.context.getState()

    let selectedNode = null

    this._visitNodes([root], node => {
      if (
        (selectedId && selectedId === node.id) ||
        (selectedObject && _.isEqual(selectedObject, node.object))
      ) {
        selectedNode = node
      }
    })

    return selectedNode
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
    return await this._modifyNodes(nodes, async node => {
      if (nodeIds[node.id]) {
        if (expanded && !node.loaded) {
          const loadedNodes = await this.doLoadNodes({
            node,
            filter: null,
            offset: 0,
            limit: LOAD_LIMIT
          })
          return {
            ...node,
            loaded: true,
            expanded: true,
            children: loadedNodes.nodes,
            totalCount: loadedNodes.totalCount
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
