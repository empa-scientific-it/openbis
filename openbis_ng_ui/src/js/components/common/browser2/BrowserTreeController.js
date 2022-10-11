import _ from 'lodash'
import autoBind from 'auto-bind'

const INTERNAL_ROOT_ID = 'internal_root_id'
const LOAD_LIMIT = 50

export default class BrowserTreeController {
  async doLoadNodePath() {
    throw 'Method not implemented'
  }

  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)
  }

  async init(context) {
    context.initState({
      loaded: false,
      loading: false,
      rootId: null,
      nodes: {},
      selectedObject: null,
      expandedIds: {},
      sortingIds: {}
    })
    this.context = context
    this.lastTree = null
    this.lastLoadPromise = {}
  }

  async load() {
    await this.context.setState({
      loading: true
    })

    const state = this.context.getState()
    const newState = {
      ...state,
      rootId: null,
      nodes: {}
    }
    this.lastTree = null
    this.lastLoadPromise = {}

    const settings = await this._loadSettings()
    _.merge(newState, settings)

    await this._doLoadRoot(newState)
    await this.context.setState(newState)

    await this.context.setState({
      loaded: true,
      loading: false
    })

    this._saveSettings()
  }

  async _doLoadRoot(state) {
    state.nodes[INTERNAL_ROOT_ID] = { id: INTERNAL_ROOT_ID }
    await this._doLoadNode(state, INTERNAL_ROOT_ID, 0, LOAD_LIMIT)
    const internalRoot = state.nodes[INTERNAL_ROOT_ID]
    delete state.nodes[INTERNAL_ROOT_ID]

    if (
      internalRoot.children.length === 0 ||
      internalRoot.children.length > 1
    ) {
      throw new Error(
        'Found ' +
          internalRoot.children.length +
          ' root candidates. Expected to find 1.'
      )
    }

    const rootId = internalRoot.children[0]
    state.rootId = rootId
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
      node: node.id === INTERNAL_ROOT_ID ? null : node,
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
              loadedNode.object &&
              _.isEqual(loadedNode.object, state.selectedObject),
            expanded: !!state.expandedIds[loadedNode.id] || loadedNode.expanded,
            children: loadedNode.children
              ? loadedNode.children.map(child => child.id)
              : [],
            sortingId: state.sortingIds[loadedNode.id] || loadedNode.sortingId
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
        ...node,
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
      await this.context.setState({
        loading: true
      })

      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(newState, node.id, node.loadedCount, LOAD_LIMIT)
      this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)

      await this.context.setState({
        loading: false
      })
    }
  }

  async expandNode(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this.context.setState({
        loading: true
      })

      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doExpandNode(newState, nodeId)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)

      await this.context.setState({
        loading: false
      })

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

  async collapseAllNodes(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this.context.setState({
        loading: true
      })

      const newState = { ...state }
      newState.nodes = { ...newState.nodes }

      if (nodeId === newState.rootId) {
        const root = newState.nodes[newState.rootId]
        if (root && root.children) {
          root.children.forEach(childId => {
            this._doCollapseNode(newState, childId, true)
          })
        }
        newState.expandedIds = {}
      } else {
        newState.expandedIds = { ...newState.expandedIds }
        this._doCollapseNode(newState, node.id, true)
      }

      await this.context.setState(newState)
      await this.context.setState({
        loading: false
      })

      this._saveSettings()
    }
  }

  async _doCollapseNode(state, nodeId, recursive) {
    const node = state.nodes[nodeId]

    if (node) {
      state.nodes = { ...state.nodes }
      state.nodes[nodeId] = {
        ...state.nodes[nodeId],
        expanded: false
      }

      state.expandedIds = { ...state.expandedIds }
      state.expandedIds[nodeId] = false

      if (recursive && !_.isEmpty(node.children)) {
        node.children.forEach(child => {
          this._doCollapseNode(state, child, true)
        })
      }
    }
  }

  async selectObject(nodeObject) {
    const state = this.context.getState()

    const newState = { ...state }
    await this._doSelectObject(newState, nodeObject)
    await this.context.setState(newState)

    const { onSelectedChange } = this.context.getProps()
    if (onSelectedChange) {
      onSelectedChange(nodeObject)
    }
  }

  async _doSelectObject(state, nodeObject) {
    state.nodes = { ...state.nodes }

    Object.keys(state.nodes).forEach(id => {
      const node = state.nodes[id]
      const selected = node.object && _.isEqual(nodeObject, node.object)

      if (selected ^ node.selected) {
        state.nodes[id] = {
          ...node,
          selected
        }
      }
    })

    state.selectedObject = nodeObject
  }

  async showSelectedObject() {
    const state = this.context.getState()

    if (!state.rootId && !state.selectedObject) {
      return
    }

    const pathWithoutRoot = await this.doLoadNodePath({
      object: state.selectedObject
    })

    if (!pathWithoutRoot || _.isEmpty(pathWithoutRoot)) {
      return
    }

    const root = state.nodes[state.rootId]
    if (!root) {
      return
    }

    await this.context.setState({
      loading: true
    })

    const _this = this
    const newState = { ...state }
    const path = [root.object, ...pathWithoutRoot]

    let currentObject = path.shift()
    let currentNode = root

    const scrollTo = (ref, nodeId) => {
      const scrollToId = _.uniqueId()
      return {
        id: scrollToId,
        ref: ref,
        clear: () => {
          _this.context.setState(state => {
            const node = state.nodes[nodeId]
            if (node && node.scrollTo && node.scrollTo.id === scrollToId) {
              const newNode = { ...node }
              delete newNode.scrollTo
              return {
                nodes: {
                  ...state.nodes,
                  [newNode.id]: newNode
                }
              }
            }
          })
        }
      }
    }

    while (currentObject && currentNode) {
      let nextObject = path.shift()
      let nextNode = null

      if (nextObject) {
        await this._doExpandNode(newState, currentNode.id)
        currentNode = newState.nodes[currentNode.id]
        if (currentNode.children) {
          for (let i = 0; i < currentNode.children.length; i++) {
            const childId = currentNode.children[i]
            const child = newState.nodes[childId]
            if (child && _.isEqual(child.object, nextObject)) {
              nextNode = child
              break
            }
          }
        }
      }

      if (!nextNode) {
        newState.nodes = { ...newState.nodes }
        newState.nodes[currentNode.id] = {
          ...newState.nodes[currentNode.id],
          scrollTo: scrollTo(nextObject ? 'loadMore' : 'node', currentNode.id)
        }
      }

      currentObject = nextObject
      currentNode = nextNode
    }

    await this.context.setState(newState)
    await this.context.setState({
      loading: false
    })

    this._saveSettings()
  }

  async changeSorting(nodeId, sortingId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this.context.setState({
        loading: true
      })

      await this._setNodeLoading(nodeId, true)
      const newState = { ...state }
      newState.nodes = { ...newState.nodes }
      newState.nodes[nodeId] = {
        ...newState.nodes[nodeId],
        sortingId: sortingId
      }
      newState.sortingIds = { ...newState.sortingIds }
      newState.sortingIds[nodeId] = sortingId
      await this._doLoadNode(newState, nodeId, 0, LOAD_LIMIT)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)

      await this.context.setState({
        loading: false
      })

      this._saveSettings()
    }
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

    if (_.isObject(loaded.sortingIds)) {
      settings.sortingIds = loaded.sortingIds
    }

    return settings
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const { expandedIds, sortingIds } = this.context.getState()

      const settings = {
        expandedIds,
        sortingIds
      }

      onSettingsChange(settings)
    }
  }

  isLoading() {
    const { loading } = this.context.getState()
    return loading
  }

  getRoot() {
    const { rootId, nodes } = this.context.getState()
    return nodes[rootId]
  }

  getSelectedObject() {
    const { selectedObject } = this.context.getState()
    return selectedObject
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
