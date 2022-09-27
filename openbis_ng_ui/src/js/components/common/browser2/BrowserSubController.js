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

    const state = this.context.getState()
    const newState = { ...state }

    await this._setNodeLoading(ROOT.id, true)
    await this._doLoadNode(newState, ROOT.id, 0, LOAD_LIMIT)
    await this.context.setState(newState)
    await this._setNodeLoading(ROOT.id, false)

    this._saveSettings()
  }

  async _doLoadNode(state, nodeId, offset, limit) {
    const node = state.nodes[nodeId]

    if (!node) {
      return
    }

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

      const loadedNodesIds = []

      if (!_.isEmpty(loadedNodes.nodes)) {
        state.nodes = { ...state.nodes }

        loadedNodes.nodes.forEach(loadedNode => {
          const newNode = {
            ...loadedNode,
            selected:
              loadedNode.id === state.selectedId ||
              (loadedNode.object &&
                _.isEqual(loadedNode.object, state.selectedObject)),
            expanded: !!state.expandedIds[loadedNode.id],
            children: loadedNode.children
              ? loadedNode.children.map(child => child.id)
              : []
          }
          state.nodes[newNode.id] = newNode
          loadedNodesIds.push(newNode.id)
        })

        const loadedNodesToExpand = Object.values(loadedNodesIds)
          .map(id => state.nodes[id])
          .filter(node => node.expanded)

        if (!_.isEmpty(loadedNodesToExpand)) {
          await Promise.all(
            loadedNodesToExpand.map(node => this._doNodeExpand(state, node.id))
          )
        }
      }

      state.nodes[nodeId] = {
        ...state.nodes[nodeId],
        loaded: true,
        loadedCount: offset + limit,
        totalCount: loadedNodes.totalCount,
        children:
          offset === 0 ? loadedNodesIds : node.children.concat(loadedNodesIds)
      }
    })
  }

  async nodeLoadMore(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(newState, node.id, node.loadedCount, LOAD_LIMIT)
      this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)
    }
  }

  async nodeExpand(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doNodeExpand(newState, nodeId)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)
      this._saveSettings()
    }
  }

  async _doNodeExpand(state, nodeId) {
    const node = state.nodes[nodeId]

    if (node) {
      if (!node.loaded) {
        await this._doLoadNode(state, nodeId, 0, LOAD_LIMIT)
      }

      state.nodes = { ...state.nodes }
      state.nodes[nodeId] = {
        ...state.nodes[nodeId],
        expanded: true
      }

      state.expandedIds = { ...state.expandedIds }
      state.expandedIds[nodeId] = true
    }
  }

  async nodeCollapse(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._doNodeCollapse(newState, nodeId)
      await this.context.setState(newState)
      this._saveSettings()
    }
  }

  async _doNodeCollapse(state, nodeId) {
    const node = state.nodes[nodeId]

    if (node) {
      state.nodes = { ...state.nodes }
      state.nodes[nodeId] = {
        ...state.nodes[nodeId],
        expanded: false
      }

      state.expandedIds = { ...state.expandedIds }
      state.expandedIds[nodeId] = false
    }
  }

  async nodeSelect(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._doNodesSelect(newState, node.id, node.object)
      await this.context.setState(newState)

      const { onSelectedChange } = this.context.getState()
      if (onSelectedChange) {
        onSelectedChange(node.object)
      }
    }
  }

  async objectSelect(object) {
    const state = this.context.getState()
    const newState = { ...state }

    await this._doNodesSelect(newState, null, object)
    await this.context.setState(newState)
  }

  async _doNodesSelect(state, nodeId, nodeObject) {
    state.nodes = { ...state.nodes }

    Object.keys(state.nodes).forEach(id => {
      const node = state.nodes[id]
      const selected =
        nodeId === node.id ||
        (node.object && _.isEqual(nodeObject, node.object))

      if (selected ^ node.selected) {
        state.nodes[id] = {
          ...node,
          selected
        }
      }
    })

    state.selectedId = nodeId
    state.selectedObject = nodeObject
  }

  async _setNodeLoading(nodeId, loading) {
    await this.context.setState(state => ({
      nodes: {
        ...state.nodes,
        [nodeId]: {
          ...state.nodes[nodeId],
          loading: loading
        }
      }
    }))
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
