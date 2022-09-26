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
      selectedObject: null,
      expandedIds: {}
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
      selectedObject: null,
      expandedIds: {}
    })

    this.lastLoadPromise = {}
  }

  async load() {
    const settings = await this._loadSettings()

    if (!_.isEmpty(settings)) {
      this.context.setState({ ...settings })
    }

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
          ...state.nodes[nodeId],
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

      const { nodes, selectedId, selectedObject, expandedIds } =
        this.context.getState()

      const loadedNodesIds = []

      if (!_.isEmpty(loadedNodes.nodes)) {
        const newNodes = { ...nodes }

        loadedNodes.nodes.forEach(loadedNode => {
          const newNode = {
            ...loadedNode,
            selected:
              loadedNode.id === selectedId ||
              (loadedNode.object &&
                _.isEqual(loadedNode.object, selectedObject)),
            expanded: !!expandedIds[loadedNode.id],
            children: loadedNode.children
              ? loadedNode.children.map(child => child.id)
              : []
          }
          newNodes[newNode.id] = newNode
          loadedNodesIds.push(newNode.id)
        })

        await this.context.setState({
          nodes: newNodes
        })

        const loadedNodesToExpand = Object.values(loadedNodesIds)
          .map(id => newNodes[id])
          .filter(node => node.expanded)

        if (!_.isEmpty(loadedNodesToExpand)) {
          await Promise.all(
            loadedNodesToExpand.map(node => this.nodeExpand(node.id))
          )
        }
      }

      await this.context.setState(state => ({
        nodes: {
          ...state.nodes,
          [nodeId]: {
            ...state.nodes[nodeId],
            loading: false,
            loaded: true,
            loadedCount: offset + limit,
            totalCount: loadedNodes.totalCount,
            children:
              offset === 0
                ? loadedNodesIds
                : node.children.concat(loadedNodesIds)
          }
        }
      }))
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
        },
        expandedIds: {
          ...state.expandedIds,
          [nodeId]: true
        }
      }))

      this._saveSettings()
    }
  }

  async nodeCollapse(nodeId) {
    const { nodes } = this.context.getState()

    const node = nodes[nodeId]

    if (node) {
      await this.context.setState(state => {
        const newExpandedIds = { ...state.expandedIds }
        delete newExpandedIds[nodeId]
        return {
          nodes: {
            ...state.nodes,
            [nodeId]: {
              ...state.nodes[nodeId],
              expanded: false
            }
          },
          expandedIds: newExpandedIds
        }
      })

      this._saveSettings()
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

  async _loadSettings() {
    const props = this.context.getProps()

    if (!props.loadSettings) {
      return {}
    }

    const loaded = await props.loadSettings()

    if (!loaded || !_.isObject(loaded)) {
      return {}
    }

    const settings = {}

    if (_.isObject(loaded.expandedIds)) {
      settings.expandedIds = loaded.expandedIds
    }

    return settings
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const { expandedIds } = this.context.getState()

      const settings = {
        expandedIds
      }

      onSettingsChange(settings)
    }
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
