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
    this.lastLoadTimeoutId = {}
    this.lastLoadPromise = {}
    this.lastObjectModifications = {}
  }

  init(context) {
    context.initState({
      root: {
        id: 'root',
        text: 'Root',
        object: {
          id: 'root',
          type: 'root'
        },
        loaded: false,
        loading: false,
        selected: false,
        expanded: true,
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
    const { filter } = this.context.getState()

    if (this.lastLoadTimeoutId[node.id]) {
      clearTimeout(this.lastLoadTimeoutId[node.id])
      delete this.lastLoadTimeoutId[node.id]
    }

    if (this.lastLoadPromise[node.id]) {
      delete this.lastLoadPromise[node.id]
    }

    this.lastLoadTimeoutId[node.id] = setTimeout(async () => {
      const { root } = this.context.getState()

      const [newRoot] = await this._modifyNodes([root], originalNode => {
        if (originalNode.id === node.id) {
          return {
            ...originalNode,
            loading: true
          }
        } else {
          return originalNode
        }
      })

      await this.context.setState({
        root: newRoot
      })

      const loadPromise = this.doLoadNodes({
        node: node,
        filter: util.trim(filter),
        offset: 0,
        limit: LOAD_LIMIT
      })

      this.lastLoadPromise[node.id] = loadPromise

      return loadPromise.then(async loadedNodes => {
        if (loadPromise !== this.lastLoadPromise[node.id]) {
          return
        }

        let newChildren = await this._setNodesExpanded(
          loadedNodes.nodes,
          this._getExpandedNodes([node]),
          true
        )

        const { selectedId, selectedObject } = this.context.getState()

        newChildren = await this._setNodesSelected(
          newChildren,
          selectedId,
          selectedObject
        )

        const { root } = this.context.getState()

        const [newRoot] = await this._modifyNodes([root], originalNode => {
          if (originalNode.id === node.id) {
            return {
              ...originalNode,
              loaded: true,
              loading: false,
              children: newChildren,
              totalCount: loadedNodes.totalCount
            }
          } else {
            return originalNode
          }
        })

        await this.context.setState({
          root: newRoot
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
      filter: newFilter
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

  getRoot() {
    const { root } = this.context.getState()
    return root
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
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
