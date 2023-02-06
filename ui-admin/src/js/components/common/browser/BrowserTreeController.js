import _ from 'lodash'
import autoBind from 'auto-bind'

export default class BrowserTreeController {
  static INTERNAL_ROOT_ID = 'internal_root_id'
  static INTERNAL_ROOT_TYPE = 'internal_root_type'
  static INTERNAL_CUSTOM_SORTING_ID = 'internal_custom_sorting_id'

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
      undoCollapseAllIds: {},
      sortingIds: {},
      customSortings: {}
    })
    this.context = context
    this.lastTree = null
    this.lastLoadPromise = {}
    this.lastLoadCustomSortedResults = {}
  }

  async load(rootNode) {
    await this.context.setState({
      loading: true
    })

    const state = this.context.getState()

    const newState = {
      loaded: true,
      loading: false,
      rootId: null,
      nodes: {},
      selectedObject: state.selectedObject,
      expandedIds: {},
      undoCollapseAllIds: {},
      sortingIds: {},
      customSortings: {}
    }

    this.lastTree = null
    this.lastLoadPromise = {}
    this.lastLoadCustomSortedResults = {}

    const settings = await this._loadSettings()
    _.merge(newState, settings)

    await this._doLoadRoot(newState, rootNode)
    await this.context.setState(newState)

    this._saveSettings()
  }

  async _doLoadRoot(state, rootNode) {
    if (rootNode) {
      state.nodes[rootNode.id] = rootNode
      await this._doLoadNode(
        state,
        rootNode.id,
        0,
        rootNode.childrenLoadLimit,
        false
      )
      state.rootId = rootNode.id
    } else {
      state.nodes[BrowserTreeController.INTERNAL_ROOT_ID] = {
        id: BrowserTreeController.INTERNAL_ROOT_ID,
        object: {
          type: BrowserTreeController.INTERNAL_ROOT_TYPE
        },
        canHaveChildren: true,
        internalRoot: true
      }
      await this._doLoadNode(
        state,
        BrowserTreeController.INTERNAL_ROOT_ID,
        0,
        null,
        false
      )
      const internalRoot = state.nodes[BrowserTreeController.INTERNAL_ROOT_ID]
      delete state.nodes[BrowserTreeController.INTERNAL_ROOT_ID]

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
  }

  _getNodeForLoad(node) {
    const nodeForLoad = { ...node }
    if (node.sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID) {
      if (!_.isNil(node.customSorting)) {
        nodeForLoad.sortingId = node.customSorting.baseSortingId
      } else {
        nodeForLoad.sortingId = null
      }
    }
    return nodeForLoad
  }

  _getSortingIdsForLoad(state) {
    const sortingIdsForLoad = { ...state.sortingIds }
    Object.entries(sortingIdsForLoad).forEach(([nodeId, sortingId]) => {
      if (sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID) {
        const customSorting = state.customSortings[nodeId]
        if (!_.isNil(customSorting)) {
          sortingIdsForLoad[nodeId] = customSorting.baseSortingId
        } else {
          sortingIdsForLoad[nodeId] = null
        }
      }
    })
    return sortingIdsForLoad
  }

  _getCustomSortingForLoad(node) {
    if (node.sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID) {
      return node.customSorting
    } else {
      return null
    }
  }

  _getNonCustomSortedOffsetForLoad(node, offset) {
    if (
      node.sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID &&
      !_.isNil(node.customSorting)
    ) {
      const children = node.children || []
      const customSortedNodes = node.customSorting.customSortedNodes || {}
      return children.slice(0, offset).filter(childId => {
        return _.isNil(customSortedNodes[childId])
      }).length
    }

    return offset
  }

  async loadNode(nodeId, offset, limit, append) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(newState, node.id, offset, limit, append)
      this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)
    }
  }

  async _doLoadNode(state, nodeId, offset, limit, append) {
    const node = state.nodes[nodeId]

    if (!node || !node.canHaveChildren) {
      return
    }

    const nodeForLoad = this._getNodeForLoad(node)
    const customSortingForLoad = this._getCustomSortingForLoad(node)
    const sortingIdsForLoad = this._getSortingIdsForLoad(state)

    let loadResult = null

    // case 1: load all nodes at once to be set as new children
    if (offset === 0 && _.isNil(limit) && append === false) {
      loadResult = await this._doLoadNodeWithChecks({
        node: nodeForLoad,
        offset,
        limit,
        sortingIds: sortingIdsForLoad
      })

      if (!loadResult.latest) {
        return
      }

      if (
        !_.isNil(customSortingForLoad) &&
        !_.isEmpty(customSortingForLoad.customSortedNodes)
      ) {
        loadResult.nodes = this._doCustomSortNodes(
          loadResult.nodes,
          0,
          customSortingForLoad
        )
      }
      // case 2: load a range of nodes to be appended to the existing children or set as the new children
    } else if (!_.isNil(offset) && !_.isNil(limit) && !_.isNil(append)) {
      if (
        _.isNil(customSortingForLoad) ||
        _.isEmpty(customSortingForLoad.customSortedNodes)
      ) {
        loadResult = await this._doLoadNodeWithChecks({
          node: nodeForLoad,
          offset,
          limit,
          sortingIds: sortingIdsForLoad
        })

        if (!loadResult.latest) {
          return
        }
      } else {
        let loadCustomSortedResult = this.lastLoadCustomSortedResults[node.id]

        if (
          _.isNil(loadCustomSortedResult) ||
          loadCustomSortedResult.version !== customSortingForLoad.version
        ) {
          loadCustomSortedResult = await this._doLoadNodeWithChecks({
            node: nodeForLoad,
            childrenIn: Object.values(customSortingForLoad.customSortedNodes),
            offset: 0,
            limit: null,
            sortingIds: sortingIdsForLoad
          })

          if (!loadCustomSortedResult.latest) {
            return
          }

          loadCustomSortedResult.version = customSortingForLoad.version

          this.lastLoadCustomSortedResults[node.id] = loadCustomSortedResult
        }

        const nonCustomSortedOffsetForLoad =
          this._getNonCustomSortedOffsetForLoad(node, offset)

        const loadNonCustomSortedResult = await this._doLoadNodeWithChecks({
          node: nodeForLoad,
          childrenNotIn: Object.values(customSortingForLoad.customSortedNodes),
          offset: nonCustomSortedOffsetForLoad,
          limit,
          sortingIds: sortingIdsForLoad
        })

        if (!loadNonCustomSortedResult.latest) {
          return
        }

        const sortedNodes = this._doCustomSortNodes(
          [...loadNonCustomSortedResult.nodes, ...loadCustomSortedResult.nodes],
          offset,
          customSortingForLoad
        )

        loadResult = {
          nodes: sortedNodes.slice(offset, offset + limit),
          totalCount:
            loadNonCustomSortedResult.totalCount +
            loadCustomSortedResult.totalCount
        }
      }
    } else {
      console.error(
        `ERROR: cannot load nodes because an incorrect combination of offset, limit and append values was used (offset: ${offset}, limit: ${limit}, append: ${append})`
      )
      return
    }

    state.nodes = { ...state.nodes }

    const accumulator = { allLoadedNodesIds: [] }
    this._doProcessLoadResult(state, nodeId, loadResult, append, accumulator)

    const loadedNodesToExpand = Object.values(accumulator.allLoadedNodesIds)
      .map(id => state.nodes[id])
      .filter(node => node.expanded)

    if (!_.isEmpty(loadedNodesToExpand)) {
      await Promise.all(
        loadedNodesToExpand.map(node => this._doExpandNode(state, node.id))
      )
    }
  }

  async _doLoadNodeWithChecks(loadParams) {
    const { node } = loadParams

    if (this.lastLoadPromise[node.id]) {
      delete this.lastLoadPromise[node.id]
    }

    const loadPromise = this.doLoadNodes(loadParams)
    this.lastLoadPromise[node.id] = loadPromise
    const loadResult = await loadPromise

    const errors = []
    this._doCheckLoadResult(node, loadResult, errors)

    if (!_.isEmpty(errors)) {
      const message =
        'ERRORS:\n' +
        errors.map((error, index) => index + 1 + ') ' + error).join('\n')
      console.error(message)
    }

    return {
      ...loadResult,
      latest: loadPromise === this.lastLoadPromise[node.id]
    }
  }

  _doCheckLoadResult(node, loadResult, errors) {
    if (_.isNil(loadResult)) {
      errors.push(`Load result cannot be null (parent id: ${node.id})`)
    }
    if (_.isNil(loadResult.nodes)) {
      errors.push(`Load result nodes cannot be null (parent id: ${node.id})`)
    }

    loadResult.nodes.forEach(child => {
      // id
      if (_.isNil(child.id)) {
        errors.push(`Node id is null (parent id: ${node.id})`)
      }
      if (
        node.id !== BrowserTreeController.INTERNAL_ROOT_ID &&
        (!child.id.startsWith(node.id) || child.id === node.id)
      ) {
        errors.push(
          `Node id should be prefixed with parent id (parent id: ${node.id}, node id: ${child.id})`
        )
      }

      // text, message
      if (_.isNil(child.text) && _.isNil(child.message)) {
        errors.push(`Node text and message is null (node id: ${child.id})`)
      }

      if (
        !_.isNil(child.message) &&
        (_.isNil(child.message.type) || _.isNil(child.message.text))
      ) {
        errors.push(
          `Node message is incorrect (node id: ${
            child.id
          }, message: ${JSON.stringify(child.message)})`
        )
      }

      // object
      if (
        _.isNil(child.object) ||
        _.isNil(child.object.id) ||
        _.isNil(child.object.type)
      ) {
        errors.push(
          `Node object, object.id or object.type is null (node id: ${
            child.id
          }, object: ${JSON.stringify(child.object)})`
        )
      }

      // flags
      const flags = ['canHaveChildren', 'rootable', 'selectable', 'draggable']

      flags.forEach(flag => {
        if (!_.isNil(child[flag]) && !_.isBoolean(child[flag])) {
          errors.push(
            `Node '${flag}' value should be a boolean (node id: ${child.id}, field value: ${child[flag]})`
          )
        }
      })

      // childrenLoadLimit
      if (
        !_.isNil(child.childrenLoadLimit) &&
        (!_.isNumber(child.childrenLoadLimit) || _.isNil(child.canHaveChildren))
      ) {
        errors.push(
          `Node 'childrenLoadLimit' value is incorrect (node id: ${child.id}, childrenLoadLimit: ${child.childrenLoadLimit})`
        )
      }

      // sortingId, sortings
      if (!_.isNil(child.sortingId)) {
        const sorting = _.isNil(child.sortings)
          ? null
          : child.sortings[child.sortingId]

        if (
          _.isNil(sorting) ||
          _.isNil(sorting.label) ||
          _.isNil(sorting.index) ||
          !_.isNumber(sorting.index)
        ) {
          errors.push(
            `Node sorting is incorrect (node id: ${child.id}, sortingId: ${
              child.sortingId
            }, sortings: ${JSON.stringify(child.sortings)}`
          )
        }
      }

      if (child.canHaveChildren && child.children) {
        this._doCheckLoadResult(child, child.children, errors)
      }
    })
  }

  _doProcessLoadResult(state, nodeId, loadResult, append, accumulator) {
    const loadedNodesIds = []

    if (!_.isEmpty(loadResult) && !_.isEmpty(loadResult.nodes)) {
      loadResult.nodes.forEach(loadedNode => {
        const draggable = loadedNode.draggable !== false
        const selectable = loadedNode.selectable !== false

        state.nodes[loadedNode.id] = {
          ...loadedNode,
          draggable: draggable,
          selectable: selectable,
          selected:
            selectable &&
            loadedNode.object &&
            _.isEqual(loadedNode.object, state.selectedObject),
          expandedOnLoad: !!loadedNode.expanded,
          expanded:
            state.expandedIds[loadedNode.id] !== undefined
              ? state.expandedIds[loadedNode.id]
              : !!loadedNode.expanded,
          sortingId: state.sortingIds[loadedNode.id] || loadedNode.sortingId,
          customSorting: state.customSortings[loadedNode.id],
          children: []
        }

        if (!_.isEmpty(loadedNode.children)) {
          this._doProcessLoadResult(
            state,
            loadedNode.id,
            loadedNode.children,
            false,
            accumulator
          )
        }

        loadedNodesIds.push(loadedNode.id)
      })
    }

    const node = (state.nodes[nodeId] = {
      ...state.nodes[nodeId]
    })

    if (append) {
      node.children = node.children.concat(loadedNodesIds)
    } else {
      node.children = loadedNodesIds
    }

    node.childrenTotalCount = loadResult.totalCount
    node.loaded = true

    accumulator.allLoadedNodesIds.push(...loadedNodesIds)
  }

  async loadMoreNodes(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (
      node &&
      node.loaded &&
      !_.isNil(node.childrenLoadLimit) &&
      !_.isNil(node.childrenTotalCount) &&
      node.children.length < node.childrenTotalCount
    ) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(
        newState,
        node.id,
        node.children.length,
        node.childrenLoadLimit,
        true
      )
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
      if (!node.loaded && node.canHaveChildren) {
        await this._doLoadNode(state, nodeId, 0, node.childrenLoadLimit, false)
      }

      const newNode = {
        ...state.nodes[nodeId],
        expanded: true
      }

      state.nodes = { ...state.nodes }
      state.nodes[nodeId] = newNode

      state.expandedIds = { ...state.expandedIds }
      if (newNode.expanded !== newNode.expandedOnLoad) {
        state.expandedIds[nodeId] = newNode.expanded
      } else {
        delete state.expandedIds[nodeId]
      }

      state.undoCollapseAllIds = { ...state.undoCollapseAllIds }
      Object.keys(state.undoCollapseAllIds).forEach(undoNodeId => {
        if (nodeId.startsWith(undoNodeId)) {
          delete state.undoCollapseAllIds[undoNodeId]
        }
      })
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
      const newState = { ...state }
      newState.nodes = { ...newState.nodes }

      if (nodeId === newState.rootId) {
        newState.expandedIds = {}
        const root = newState.nodes[newState.rootId]
        if (root && root.children) {
          root.children.forEach(childId => {
            this._doCollapseNode(newState, childId, true)
            this._doCollapseNode(newState, childId, false)
          })
        }
      } else {
        newState.expandedIds = { ...newState.expandedIds }

        Object.keys(newState.expandedIds).forEach(expandedId => {
          if (expandedId.startsWith(nodeId)) {
            delete newState.expandedIds[expandedId]
          }
        })

        this._doCollapseNode(newState, node.id, true)
        this._doCollapseNode(newState, node.id, false)
      }

      const collapsedIds = _.difference(
        Object.keys(state.expandedIds),
        Object.keys(newState.expandedIds)
      )

      if (!_.isEmpty(collapsedIds)) {
        const newUndoCollapseAllIds = {
          ...newState.undoCollapseAllIds,
          [nodeId]: collapsedIds
        }
        newState.undoCollapseAllIds = newUndoCollapseAllIds
      }

      await this.context.setState(newState)
      this._saveSettings()
    }
  }

  async _doCollapseNode(state, nodeId, recursive) {
    const node = state.nodes[nodeId]

    if (node) {
      const newNode = {
        ...node
      }

      if (recursive) {
        newNode.expanded = node.expandedOnLoad
      } else {
        newNode.expanded = false
      }

      state.nodes = { ...state.nodes }
      state.nodes[nodeId] = newNode

      state.expandedIds = { ...state.expandedIds }
      if (newNode.expanded !== newNode.expandedOnLoad) {
        state.expandedIds[nodeId] = newNode.expanded
      } else {
        delete state.expandedIds[nodeId]
      }

      if (recursive && !_.isEmpty(node.children)) {
        node.children.forEach(child => {
          this._doCollapseNode(state, child, true)
        })
      }
    }
  }

  async undoCollapseAllNodes(nodeId) {
    const state = this.context.getState()
    const collapsedIds = state.undoCollapseAllIds[nodeId]

    if (!_.isEmpty(collapsedIds)) {
      const newState = { ...state }

      collapsedIds.forEach(collapsedId => {
        this._doExpandNode(newState, collapsedId)
      })

      newState.undoCollapseAllIds = { ...newState.undoCollapseAllIds }
      delete newState.undoCollapseAllIds[nodeId]

      this.context.setState(newState)
    }
  }

  canUndoCollapseAllNodes(nodeId) {
    const state = this.context.getState()
    const collapsedIds = state.undoCollapseAllIds[nodeId]
    return !_.isEmpty(collapsedIds)
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

      if (!node.selectable) {
        return
      }

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

  async showSelectedObject(showLoadMore) {
    const state = this.context.getState()

    if (state.loading) {
      return
    }

    const root = this.getRoot()

    if (!root) {
      return
    }

    const selectedObject = state.selectedObject

    if (!selectedObject) {
      return
    }

    const pathWithoutRoot = await this.doLoadNodePath({
      root: root,
      object: selectedObject
    })

    if (!pathWithoutRoot || _.isEmpty(pathWithoutRoot)) {
      return
    }

    const _this = this
    const newState = { ...state }
    const path = [root, ...pathWithoutRoot]

    let currentPathNode = path.shift()
    let currentNode = root

    const scrollTo = (nodeId, object) => {
      const scrollToId = _.uniqueId()
      return {
        id: scrollToId,
        object: object,
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

    while (currentPathNode && currentNode) {
      let nextPathNode = path.shift()
      let nextNode = null

      if (nextPathNode) {
        await this._doExpandNode(newState, currentNode.id)
        currentNode = newState.nodes[currentNode.id]
        if (currentNode.children) {
          for (let i = 0; i < currentNode.children.length; i++) {
            const childId = currentNode.children[i]
            const child = newState.nodes[childId]
            if (child && _.isEqual(child.object, nextPathNode.object)) {
              nextNode = child
              break
            }
          }
        }
      }

      if (!nextNode) {
        if (!_.isEqual(currentNode.object, selectedObject) && !showLoadMore) {
          return
        }
        newState.nodes = { ...newState.nodes }
        newState.nodes[currentNode.id] = {
          ...newState.nodes[currentNode.id],
          scrollTo: scrollTo(currentNode.id, selectedObject)
        }
      }

      currentPathNode = nextPathNode
      currentNode = nextNode
    }

    await this.context.setState(newState)
    this._saveSettings()
  }

  async changeSorting(nodeId, sortingId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this._setNodeLoading(nodeId, true)
      const newState = { ...state }
      newState.nodes = { ...newState.nodes }
      newState.nodes[nodeId] = {
        ...newState.nodes[nodeId],
        sortingId: sortingId
      }
      newState.sortingIds = { ...newState.sortingIds }
      newState.sortingIds[nodeId] = sortingId
      await this._doLoadNode(newState, nodeId, 0, node.childrenLoadLimit, false)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)

      this._saveSettings()
    }
  }

  async changeCustomSorting(nodeId, oldIndex, newIndex) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (
      node &&
      node.children &&
      oldIndex < node.children.length &&
      newIndex < node.children.length
    ) {
      const childId = node.children[oldIndex]
      const child = state.nodes[childId]

      if (childId && child) {
        const customSorting = state.customSortings[nodeId]
        const sortingId = state.sortingIds[nodeId]

        let newCustomSorting = null
        if (
          !customSorting ||
          sortingId !== BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID
        ) {
          newCustomSorting = {
            baseSortingId: sortingId,
            customSortedNodes: {
              [childId]: {
                id: childId,
                object: child.object,
                index: newIndex
              }
            },
            version: _.uniqueId()
          }
        } else {
          const newCustomSortedNodes = { ...customSorting.customSortedNodes }

          /*
          Example:
          {a,b,c,d,e} {}
          move a 0->1
          {b,a,c,d,e} {a:1}
          move c 2->1 (move back)
          {b,c,a,d,e} {c:1, a:2}
          move e 4->2 (move back) - increase custom indexes of elements in range [newIndex, oldIndex)
          {b,c,e,a,d} {c:1, e:2, a:3}
          move c 1->2 (move forward) - decrease custom indexes of elements in range (oldIndex, newIndex]
          {b,e,c,a,d} {e:1, c:2, a:3}
          */

          if (newIndex > oldIndex) {
            Object.entries(newCustomSortedNodes).forEach(([entryId, entry]) => {
              if (entry.index > oldIndex && entry.index <= newIndex) {
                newCustomSortedNodes[entryId] = {
                  ...entry,
                  index: entry.index - 1
                }
              }
            })
          } else if (newIndex < oldIndex) {
            Object.entries(newCustomSortedNodes).forEach(([entryId, entry]) => {
              if (entry.index >= newIndex && entry.index < oldIndex) {
                newCustomSortedNodes[entryId] = {
                  ...entry,
                  index: entry.index + 1
                }
              }
            })
          }

          newCustomSortedNodes[childId] = {
            id: childId,
            object: child.object,
            index: newIndex
          }

          newCustomSorting = {
            ...customSorting,
            customSortedNodes: newCustomSortedNodes,
            version: _.uniqueId()
          }
        }

        const newNode = { ...node, children: [...node.children] }
        newNode.customSorting = newCustomSorting
        newNode.sortingId = BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID
        newNode.children = this._doCustomSortIds(
          newNode.children,
          0,
          newCustomSorting
        )

        const newState = { ...state }
        newState.customSortings = { ...newState.customSortings }
        newState.sortingIds = { ...newState.sortingIds }
        newState.nodes = { ...newState.nodes }

        newState.customSortings[nodeId] = newCustomSorting
        newState.sortingIds[nodeId] =
          BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID
        newState.nodes[nodeId] = newNode

        await this.context.setState(newState)

        this._saveSettings()
      }
    }
  }

  async clearCustomSorting(nodeId) {
    const state = this.context.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this._setNodeLoading(nodeId, true)

      const newState = { ...state }
      newState.customSortings = { ...newState.customSortings }
      newState.nodes = { ...newState.nodes }

      let newSortingId = null
      let newSorting = null

      if (!_.isEmpty(node.sortings)) {
        Object.entries(node.sortings).forEach(([sortingId, sorting]) => {
          if (newSorting === null || sorting.index < newSorting.index) {
            newSortingId = sortingId
            newSorting = sorting
          }
        })
      }

      const newNode = { ...node }
      newNode.customSorting = null
      newNode.sortingId = newSortingId

      delete newState.customSortings[nodeId]
      newState.sortingIds[nodeId] = newSortingId
      newState.nodes[nodeId] = newNode

      await this._doLoadNode(newState, nodeId, 0, node.childrenLoadLimit, false)
      await this.context.setState(newState)
      await this._setNodeLoading(nodeId, false)

      this._saveSettings()
    }
  }

  _doCustomSortNodes(nodes, nodesOffset, customSorting) {
    const nodesMap = {}
    nodes.forEach(node => {
      nodesMap[node.id] = node
    })

    const sortedIds = this._doCustomSortIds(
      Object.keys(nodesMap),
      nodesOffset,
      customSorting
    )

    const sortedNodes = []

    for (let index = 0; index < sortedIds.length; index++) {
      const sortedId = sortedIds[index]
      if (!_.isNil(sortedId)) {
        const sortedNode = nodesMap[sortedId]
        if (!_.isNil(sortedNode)) {
          sortedNodes[index] = sortedNode
        }
      }
    }

    return sortedNodes
  }

  _doCustomSortIds(nodeIds, nodesOffset, customSorting) {
    const sortedIds = []
    const alreadySorted = {}

    // insert nodes with custom indexes
    nodeIds.forEach(nodeId => {
      const customSortedNode = customSorting.customSortedNodes[nodeId]
      if (!_.isNil(customSortedNode)) {
        sortedIds[customSortedNode.index] = nodeId
        alreadySorted[nodeId] = true
      }
    })

    // fill in gaps with remaining nodes starting from the given offset
    let index = 0
    nodeIds.forEach(nodeId => {
      if (!alreadySorted[nodeId]) {
        while (!_.isNil(sortedIds[index + nodesOffset])) {
          index++
        }
        sortedIds[index + nodesOffset] = nodeId
      }
    })

    return sortedIds
  }

  async _setNodeLoading(nodeId, loading) {
    const state = this.context.getState()

    if (nodeId === state.rootId) {
      await this.context.setState(() => ({
        loading: loading
      }))
    } else {
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

    if (_.isObject(loaded.customSortings)) {
      settings.customSortings = loaded.customSortings
    }

    if (!_.isNil(settings.sortingIds)) {
      Object.entries(settings.sortingIds).forEach(([nodeId, sortingId]) => {
        if (sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID) {
          const customSorting = !_.isNil(settings.customSortings)
            ? settings.customSortings[nodeId]
            : null
          if (_.isNil(customSorting)) {
            delete settings.sortingIds[nodeId]
          }
        }
      })
    }

    return settings
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const { expandedIds, sortingIds, customSortings } =
        this.context.getState()

      const settings = {
        expandedIds,
        sortingIds,
        customSortings
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
