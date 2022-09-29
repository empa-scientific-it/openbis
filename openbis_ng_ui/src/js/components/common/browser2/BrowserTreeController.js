import _ from 'lodash'
import autoBind from 'auto-bind'

const LOAD_LIMIT = 50

export default class BrowserTreeController {
  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)
  }

  async init(context) {
    this.context = context
    await this.clear()
  }

  async clear() {
    const root = await this.doLoadNodes({})

    await this.context.setState({
      rootId: root.id,
      nodes: { [root.id]: root },
      selectedId: null,
      selectedObject: null,
      expandedIds: {},
      sortings: {}
    })

    this.lastTree = null
    this.lastLoadPromise = {}
  }

  async load() {
    const settings = await this._loadSettings()

    if (!_.isEmpty(settings)) {
      this.context.setState({ ...settings })
    }

    const state = this.context.getState()
    const newState = { ...state }

    await this._setNodeLoading(newState.rootId, true)
    await this._doExpandNode(newState, newState.rootId)
    await this.context.setState(newState)
    await this._setNodeLoading(newState.rootId, false)

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
            loadedNodesToExpand.map(node => this._doExpandNode(state, node.id))
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

  async loadMoreNodes(nodeId) {
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

  async expandNode(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doExpandNode(newState, nodeId)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)
      this._saveSettings()
    }
  }

  async _doExpandNode(state, nodeId) {
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

  async collapseNode(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._doCollapseNode(newState, nodeId)
      await this.context.setState(newState)
      this._saveSettings()
    }
  }

  async collapseAllNodes() {
    const state = this.context.getState()

    const newState = { ...state }
    newState.nodes = { ...newState.nodes }
    Object.values(newState.nodes).forEach(node => {
      if (node.id === newState.rootId) {
        return
      }
      newState.nodes[node.id] = {
        ...node,
        expanded: false
      }
    })
    newState.expandedIds = {}

    await this.context.setState(newState)
    this._saveSettings()
  }

  async _doCollapseNode(state, nodeId) {
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

  async selectNode(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._doSelectNodes(newState, node.id, node.object)
      await this.context.setState(newState)

      const { onSelectedChange } = this.context.getState()
      if (onSelectedChange) {
        onSelectedChange(node.object)
      }
    }
  }

  async selectObject(object) {
    const state = this.context.getState()
    const newState = { ...state }

    await this._doSelectNodes(newState, null, object)
    await this.context.setState(newState)
  }

  async _doSelectNodes(state, nodeId, nodeObject) {
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

  async changeSorting(nodeId, sorting) {
    const state = this.context.getState()

    await this._setNodeLoading(nodeId, true)
    const newState = { ...state }
    newState.nodes = { ...newState.nodes }
    newState.nodes[nodeId] = {
      ...newState.nodes[nodeId],
      sorting: sorting
    }
    newState.sortings = { ...newState.sortings }
    newState.sortings[nodeId] = sorting
    await this._doLoadNode(newState, nodeId, 0, LOAD_LIMIT)
    await this.context.setState(newState)
    await this._setNodeLoading(nodeId, false)

    this._saveSettings()
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

  isLoading() {
    const root = this.getRoot()
    return root && root.loading
  }

  getRoot() {
    const { rootId, nodes } = this.context.getState()
    return nodes[rootId]
  }

  getTree() {
    const { rootId, nodes } = this.context.getState()
    this.lastTree = this._getTree(this.lastTree, nodes, rootId)
    return this.lastTree
  }

  _getTree(lastTree, nodes, nodeId) {
    const node = nodes[nodeId]

    if (!node) {
      return null
    }

    const treeChanged = !lastTree || lastTree._node !== node

    if (_.isEmpty(node.children)) {
      if (!treeChanged) {
        return lastTree
      } else {
        return {
          ...node,
          _node: node
        }
      }
    } else {
      const subTrees = []
      let subTreesChanged = false

      for (let i = 0; i < node.children.length; i++) {
        const childId = node.children[i]
        let lastSubTree = null

        if (
          lastTree &&
          lastTree.children &&
          lastTree.children.length > i &&
          lastTree.children[i].id === childId
        ) {
          lastSubTree = lastTree.children[i]
        }

        const subTree = this._getTree(lastSubTree, nodes, childId)

        if (subTree) {
          subTrees.push(subTree)
          if (subTree !== lastSubTree) {
            subTreesChanged = true
          }
        }
      }

      if (!treeChanged && !subTreesChanged) {
        return lastTree
      } else if (subTreesChanged) {
        return {
          ...node,
          _node: node,
          children: subTrees
        }
      } else if (treeChanged) {
        return {
          ...node,
          _node: node,
          children: lastTree.children
        }
      }
    }
  }
}
