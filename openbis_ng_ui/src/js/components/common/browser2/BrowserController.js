import _ from 'lodash'
import autoBind from 'auto-bind'
import util from '@src/js/common/util.js'

const LOAD_LIMIT = 50
const LOAD_SILENT_PERIOD = 500

const ROOT_ID = 'root'
const ROOT_TYPE = 'root'

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
      nodes: {
        root: {
          id: ROOT_ID,
          object: {
            id: ROOT_ID,
            type: ROOT_TYPE
          },
          loaded: false,
          loading: false,
          selected: false,
          expanded: true,
          canHaveChildren: true
        }
      },
      filter: null,
      selectedId: null,
      selectedObject: null
    })
    this.context = context
  }

  async load() {
    const { filter } = this.context.getState()
    this.loadNode(ROOT_ID, filter, 0, LOAD_LIMIT)
  }

  async loadNode(nodeId, filter, offset, limit) {
    if (this.lastLoadTimeoutId[nodeId]) {
      clearTimeout(this.lastLoadTimeoutId[nodeId])
      delete this.lastLoadTimeoutId[nodeId]
    }

    if (this.lastLoadPromise[nodeId]) {
      delete this.lastLoadPromise[nodeId]
    }

    this.lastLoadTimeoutId[nodeId] = setTimeout(async () => {
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

      const loadPromise = this.doLoadNodes({
        node: node,
        filter: util.trim(filter),
        offset: offset,
        limit: limit
      })

      this.lastLoadPromise[nodeId] = loadPromise

      return loadPromise.then(async loadedNodes => {
        if (loadPromise !== this.lastLoadPromise[nodeId]) {
          return
        }

        this.context.setState(state => {
          const newNodes = { ...state.nodes }
          const newNodesIds = []

          if (loadedNodes.nodes) {
            loadedNodes.nodes.forEach(node => {
              const newNode = {
                ...node,
                children: node.children
                  ? node.children.map(child => child.id)
                  : []
              }

              // TODO expand nodes
              // TODO select nodes

              newNodes[newNode.id] = newNode
              newNodesIds.push(newNode.id)
            })
          }

          const newNode = {
            ...node,
            children:
              offset === 0 ? newNodesIds : node.children.concat(newNodesIds),
            loaded: true,
            loadedCount: offset + limit,
            totalCount: loadedNodes.totalCount
          }

          newNodes[nodeId] = newNode

          return {
            nodes: newNodes
          }
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

  async nodeLoadMore(nodeId) {
    const { nodes, filter } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      await this.loadNode(node.id, filter, node.loadedCount, LOAD_LIMIT)
    }
  }

  async nodeExpand(nodeId) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      if (!node.loaded) {
        await this.loadNode(nodeId, null, 0, LOAD_LIMIT)
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

  getRoot() {
    const { nodes } = this.context.getState()
    return nodes[ROOT_ID]
  }

  getNodes() {
    const { nodes } = this.context.getState()
    return nodes
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
  }

  getSelectedNode() {
    const { nodes, selectedId, selectedObject } = this.context.getState()

    let selectedNode = null

    Object.keys(nodes).forEach(id => {
      const node = nodes[id]
      if (
        (selectedId && selectedId === node.id) ||
        (selectedObject && _.isEqual(selectedObject, node.object))
      ) {
        selectedNode = node
      }
    })

    return selectedNode
  }
}
