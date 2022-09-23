import _ from 'lodash'
import autoBind from 'auto-bind'

const ROOT = {
  id: 'root',
  object: {
    id: 'root',
    type: 'root'
  },
  loaded: false,
  loading: false,
  selected: false,
  expanded: true,
  canHaveChildren: true
}

const LOAD_LIMIT = 50

export default class BrowserSubController {
  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)
  }

  init(context) {
    context.initState({
      nodes: {
        root: ROOT
      },
      selectedId: null,
      selectedObject: null
    })

    this.lastLoadPromise = {}
    this.context = context
  }

  async clear() {
    await this.context.setState({
      nodes: {
        root: ROOT
      },
      selectedId: null,
      selectedObject: null
    })

    this.lastLoadPromise = {}
  }

  async load() {
    this.loadNode(ROOT.id, 0, LOAD_LIMIT)
  }

  async loadNode(nodeId, offset, limit) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (!node) {
      return
    }

    await this.context.setState(state => ({
      nodes: {
        ...state.nodes,
        [nodeId]: {
          ...node,
          loading: true
        }
      }
    }))

    if (this.lastLoadPromise[nodeId]) {
      delete this.lastLoadPromise[nodeId]
    }

    const loadPromise = this.doLoadNodes({
      node: node,
      offset: offset,
      limit: limit
    })

    this.lastLoadPromise[nodeId] = loadPromise

    return loadPromise.then(async loadedNodes => {
      if (loadPromise !== this.lastLoadPromise[nodeId]) {
        return
      }

      await this.context.setState(state => {
        const newNodes = { ...state.nodes }
        const newNodesIds = []

        if (loadedNodes.nodes) {
          loadedNodes.nodes.forEach(node => {
            const newNode = {
              ...node,
              selected:
                node.id === state.selectedId ||
                (node.object && _.isEqual(node.object, state.selectedObject)),
              children: node.children
                ? node.children.map(child => child.id)
                : []
            }
            newNodes[newNode.id] = newNode
            newNodesIds.push(newNode.id)
          })
        }

        const newNode = {
          ...node,
          loaded: true,
          loadedCount: offset + limit,
          totalCount: loadedNodes.totalCount,
          children:
            offset === 0 ? newNodesIds : node.children.concat(newNodesIds)
        }

        newNodes[nodeId] = newNode

        return {
          nodes: newNodes
        }
      })
    })
  }

  async nodeLoadMore(nodeId) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      await this.loadNode(node.id, node.loadedCount, LOAD_LIMIT)
    }
  }

  async nodeExpand(nodeId) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      if (!node.loaded) {
        await this.loadNode(nodeId, 0, LOAD_LIMIT)
      }

      await this.context.setState(state => ({
        nodes: {
          ...state.nodes,
          [nodeId]: {
            ...state.nodes[nodeId],
            expanded: true
          }
        }
      }))
    }
  }

  async nodeCollapse(nodeId) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      await this.context.setState(state => ({
        nodes: {
          ...state.nodes,
          [nodeId]: {
            ...state.nodes[nodeId],
            expanded: false
          }
        }
      }))
    }
  }

  async nodeSelect(nodeId) {
    const { onSelectedChange } = this.context.getState()
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      await this._setNodesSelected(node.id, node.object)
      if (onSelectedChange) {
        onSelectedChange(node.object)
      }
    }
  }

  async objectSelect(object) {
    await this._setNodesSelected(null, object)
  }

  async _setNodesSelected(nodeId, nodeObject) {
    await this.context.setState(state => {
      const newNodes = { ...state.nodes }

      Object.keys(state.nodes).forEach(id => {
        const node = state.nodes[id]
        const selected =
          nodeId === node.id ||
          (node.object && _.isEqual(nodeObject, node.object))

        if (selected ^ node.selected) {
          newNodes[id] = {
            ...node,
            selected
          }
        }
      })

      return {
        nodes: newNodes,
        selectedId: nodeId,
        selectedObject: nodeObject
      }
    })
  }

  static isRoot(node) {
    return node && node.object && node.object.type === ROOT.object.type
  }

  getRoot() {
    const { nodes } = this.context.getState()
    return nodes[ROOT.id]
  }

  getNodes() {
    const { nodes } = this.context.getState()
    return nodes
  }
}
