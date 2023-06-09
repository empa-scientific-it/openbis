import _ from 'lodash'
import autoBind from 'auto-bind'

export default class BrowserTreeController {
  static INTERNAL_ROOT_ID = 'internal_root_id'
  static INTERNAL_ROOT_TYPE = 'internal_root_type'
  static INTERNAL_CUSTOM_SORTING_ID = 'internal_custom_sorting_id'

  static INTERNAL_NODE_ID_SEPARATOR = ' >> '
  static INTERNAL_NODE_ID_SEPARATOR_REPLACEMENT = ' __ '

  getState() {
    throw 'Method not implemented'
  }

  async setState(state) {
    throw 'Method not implemented'
  }

  async loadNodePath(params) {
    throw 'Method not implemented'
  }

  async loadNodes(params) {
    throw 'Method not implemented'
  }

  async loadSettings() {
    throw 'Method not implemented'
  }

  onSettingsChange(settings) {
    throw 'Method not implemented'
  }

  onSelectedChange(params) {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)

    this.setState({
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

    this.lastTree = null
    this.lastLoadPromise = {}
    this.lastLoadCustomSortedResults = {}
  }

  async load(rootNode) {
    await this.setState({
      loading: true
    })

    const state = this.getState()

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
    await this.setState(newState)

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
      const customSortedNodesCount = children
        .slice(0, offset)
        .filter(childId => {
          return !_.isNil(customSortedNodes[childId])
        }).length
      return offset - customSortedNodesCount
    }

    return offset
  }

  async loadNode(nodeId, offset, limit, append) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(newState, node.id, offset, limit, append)
      await this.setState(newState)
      await this._setNodeLoading(nodeId, false)
    }
  }

  async reloadNode(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      var limit = null

      if (!_.isNil(node.childrenLoadLimit)) {
        limit = node.childrenLoadLimit

        if (!_.isNil(node.children)) {
          limit = Math.max(limit, node.children.length)
        }
      }

      const newState = { ...state, nodes: {} }

      // remove descendant nodes before reload
      Object.values(state.nodes).forEach(node => {
        if(!this._isDescendantNodeId(nodeId, node.id)){
            newState.nodes[node.id] = node
        }
      })

      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(newState, node.id, 0, limit, false)
      await this.setState(newState)
      await this._setNodeLoading(nodeId, false)
    }
  }

  async _doLoadNode(state, nodeId, offset, limit, append) {
    const node = state.nodes[nodeId]

    if (!node || !node.canHaveChildren) {
      return
    }

    state.nodes = { ...state.nodes }

    const { loadParams, loadResult } = await this._doLoadNodeResult(
      node,
      offset,
      limit,
      append
    )

    const accumulator = { allLoadedNodesIds: [] }

    this._doProcessLoadResult(
      state,
      loadParams,
      loadResult,
      append,
      accumulator
    )

    const loadedNodesToExpand = Object.values(accumulator.allLoadedNodesIds)
      .map(id => state.nodes[id])
      .filter(node => node.expanded)

    if (!_.isEmpty(loadedNodesToExpand)) {
      await Promise.all(
        loadedNodesToExpand.map(node => this._doExpandNode(state, node.id))
      )
    }
  }

  async _doLoadNodeResult(node, offset, limit, append) {
    if (
      !_.isNil(offset) &&
      !_.isNil(limit) &&
      !_.isNil(node.childrenLoadRepeatLimitForFullBatch)
    ) {
      let nodes = []
      let totalCount = 0
      let loadCount = 0

      while (true) {
        const { loadResult } = await this._doLoadNodeResultOnce(
          node,
          offset,
          limit,
          append
        )

        nodes = nodes.concat(loadResult.nodes)
        totalCount = loadResult.totalCount

        if (
          loadCount >= node.childrenLoadRepeatLimitForFullBatch ||
          nodes.length >= limit ||
          offset + nodes.length >= totalCount ||
          offset + limit > totalCount
        ) {
          break
        } else {
          offset += limit
          loadCount++
        }
      }

      return {
        loadParams: {
          node,
          offset,
          limit
        },
        loadResult: {
          nodes,
          totalCount
        }
      }
    } else {
      return await this._doLoadNodeResultOnce(node, offset, limit, append)
    }
  }

  async _doLoadNodeResultOnce(node, offset, limit, append) {
    const nodeForLoad = this._getNodeForLoad(node)
    const customSortingForLoad = this._getCustomSortingForLoad(node)

    let loadParams = null
    let loadResult = null

    // case 1: load all nodes at once to be set as new children
    if (offset === 0 && _.isNil(limit) && append === false) {
      loadParams = {
        node: nodeForLoad,
        offset,
        limit
      }
      loadResult = await this._doCallLoadNodesWithChecks(loadParams)

      if (!loadResult.latest) {
        return {
          loadParams,
          loadResult: {
            nodes: [],
            totalCount: 0
          }
        }
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
        loadParams = {
          node: nodeForLoad,
          offset,
          limit
        }
        loadResult = await this._doCallLoadNodesWithChecks(loadParams)

        if (!loadResult.latest) {
          return {
            loadParams,
            loadResult: {
              nodes: [],
              totalCount: 0
            }
          }
        }
      } else {
        let loadCustomSortedResult = this.lastLoadCustomSortedResults[node.id]

        if (
          _.isNil(loadCustomSortedResult) ||
          loadCustomSortedResult.version !== customSortingForLoad.version
        ) {
          loadCustomSortedResult = await this._doCallLoadNodesWithChecks({
            node: nodeForLoad,
            childrenIn: Object.values(customSortingForLoad.customSortedNodes),
            offset: 0,
            limit: null
          })

          if (!loadCustomSortedResult.latest) {
            return {
              loadParams,
              loadResult: {
                nodes: [],
                totalCount: 0
              }
            }
          }

          loadCustomSortedResult.version = customSortingForLoad.version

          this.lastLoadCustomSortedResults[node.id] = loadCustomSortedResult
        }

        const nonCustomSortedOffsetForLoad =
          this._getNonCustomSortedOffsetForLoad(node, offset)

        const loadNonCustomSortedResult = await this._doCallLoadNodesWithChecks(
          {
            node: nodeForLoad,
            childrenNotIn: Object.values(
              customSortingForLoad.customSortedNodes
            ),
            offset: nonCustomSortedOffsetForLoad,
            limit
          }
        )

        if (!loadNonCustomSortedResult.latest) {
          return {
            loadParams,
            loadResult: {
              nodes: [],
              totalCount: 0
            }
          }
        }

        const sortedNodes = this._doCustomSortNodes(
          [...loadNonCustomSortedResult.nodes, ...loadCustomSortedResult.nodes],
          offset,
          customSortingForLoad
        )

        loadParams = {
          node: nodeForLoad,
          offset: offset,
          limit: limit
        }
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
      return {
        loadParams,
        loadResult: {
          nodes: [],
          totalCount: 0
        }
      }
    }

    return {
      loadParams,
      loadResult
    }
  }

  async _doCallLoadNodesWithChecks(loadParams) {
    const { node } = loadParams

    if (this.lastLoadPromise[node.id]) {
      delete this.lastLoadPromise[node.id]
    }

    const loadPromise = this.loadNodes(loadParams)
    this.lastLoadPromise[node.id] = loadPromise
    const loadResult = await loadPromise

    this._doCreateNodeIds(node, loadResult)

    const errors = []
    this._doCheckLoadResult(node, loadParams, loadResult, errors)
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

  _doCreateNodeIds(node, loadResult) {
    if (!_.isNil(loadResult) && !_.isEmpty(loadResult.nodes)) {
      loadResult.nodes.forEach(child => {
        child.id = this._createNodeId(node.id, child.object)
        if (child.canHaveChildren && !_.isEmpty(child.children)) {
          this._doCreateNodeIds(child, child.children)
        }
      })
    }
  }

  _doCheckLoadResult(node, loadParams, loadResult, errors) {
    if (_.isNil(loadResult)) {
      errors.push(`Load result cannot be null (parent id: ${node.id})`)
    }

    if (_.isNil(loadResult.nodes)) {
      errors.push(`Load result nodes cannot be null (parent id: ${node.id})`)
    }

    if (
      !_.isNil(loadParams.offset) &&
      !_.isNil(loadParams.limit) &&
      _.isNil(loadResult.totalCount)
    ) {
      errors.push(
        `Load result totalCount cannot be null when offset and limit are specified (parent id: ${node.id})`
      )
    }

    loadResult.nodes.forEach(child => {
      // text
      if (_.isNil(child.text) && _.isNil(child.render)) {
        errors.push(
          `Node text and render function is null (node id: ${child.id})`
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
      } else if (Object.keys(child.object).length > 2) {
        errors.push(
          `Node object can only contain id and type (node id: ${
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
      if (_.isNil(child.sortings)) {
        if (!_.isNil(child.sortingId)) {
          errors.push(
            `Node sortings are missing (node id: ${child.id}, sortingId: ${
              child.sortingId
            }, sortings: ${JSON.stringify(child.sortings)}`
          )
        }
      } else {
        let foundSorting = null
        let foundDefaultSorting = null

        child.sortings.forEach(sorting => {
          if (
            _.isNil(sorting) ||
            _.isNil(sorting.id) ||
            _.isNil(sorting.label)
          ) {
            errors.push(
              `Node sortings are incorrect (node id: ${
                child.id
              }, sortings: ${JSON.stringify(child.sortings)}`
            )
          }
          if (sorting.default) {
            foundDefaultSorting = sorting
          }
          if (sorting.id === child.sortingId) {
            foundSorting = sorting
          }
        })

        if (_.isNil(foundDefaultSorting)) {
          errors.push(
            `Node default sorting is missing (node id: ${
              child.id
            }, sortings: ${JSON.stringify(child.sortings)}`
          )
        }

        if (!_.isNil(child.sortingId) && _.isNil(foundSorting)) {
          errors.push(
            `Node sortingId not found in sortings (node id: ${
              child.id
            }, sortingId: ${child.sortingId}, sortings: ${JSON.stringify(
              child.sortings
            )}`
          )
        }
      }

      // children
      if (child.canHaveChildren && !_.isEmpty(child.children)) {
        this._doCheckLoadResult(child, {}, child.children, errors)
      }
    })
  }

  _doProcessLoadResult(state, loadParams, loadResult, append, accumulator) {
    const loadedNodesIds = []

    if (!_.isEmpty(loadResult) && !_.isEmpty(loadResult.nodes)) {
      loadResult.nodes.forEach(originalLoadedNode => {
        var loadedNode = this._doProcessLoadedNode(
          state,
          loadParams.node,
          originalLoadedNode
        )

        state.nodes[loadedNode.id] = loadedNode

        if (!_.isEmpty(loadedNode.children)) {
          this._doProcessLoadResult(
            state,
            {
              node: loadedNode,
              offset: 0,
              limit: null
            },
            loadedNode.children,
            false,
            accumulator
          )
        }

        loadedNodesIds.push(loadedNode.id)
      })
    }

    const node = (state.nodes[loadParams.node.id] = {
      ...state.nodes[loadParams.node.id]
    })

    if (append) {
      node.children = node.children.concat(loadedNodesIds)
    } else {
      node.children = loadedNodesIds
    }

    if (!_.isNil(loadResult.totalCount)) {
      node.childrenTotalCount = loadResult.totalCount
      node.childrenLoadOffset =
        !_.isNil(loadParams.offset) && !_.isNil(loadParams.limit)
          ? Math.min(
              loadParams.offset + loadParams.limit,
              loadResult.totalCount
            )
          : loadResult.totalCount
    }

    node.loaded = true

    accumulator.allLoadedNodesIds.push(...loadedNodesIds)
  }

  _doProcessLoadedNode(state, parentNode, loadedNode) {
    const draggable = loadedNode.draggable !== false
    const selectable = loadedNode.selectable !== false
    const expanded = loadedNode.expanded === true

    let sortingId = state.sortingIds[loadedNode.id]
    let sorting = null

    if (!_.isNil(sortingId)) {
      if (sortingId === BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID) {
        sorting = state.customSortings[loadedNode.id]
      } else if (!_.isEmpty(loadedNode.sortings)) {
        sorting = loadedNode.sortings.find(sorting => sorting.id === sortingId)
      }
    }

    if (
      (_.isNil(sortingId) || _.isNil(sorting)) &&
      !_.isEmpty(loadedNode.sortings)
    ) {
      const defaultSorting = loadedNode.sortings.find(
        sorting => !!sorting.default
      )
      if (!_.isNil(defaultSorting)) {
        sortingId = defaultSorting.id
      }
    }

    return {
      ...loadedNode,
      draggable: draggable,
      selectable: selectable,
      selected:
        selectable &&
        loadedNode.object &&
        _.isEqual(loadedNode.object, state.selectedObject),
      expandedOnLoad: expanded,
      expanded: !_.isNil(state.expandedIds[loadedNode.id])
        ? state.expandedIds[loadedNode.id]
        : expanded,
      sortingId: sortingId,
      customSorting: state.customSortings[loadedNode.id],
      parentId: parentNode.id,
      parentObject: parentNode.object,
      children: !_.isEmpty(loadedNode.children) ? loadedNode.children : []
    }
  }

  _createNodeId(parentId, nodeObject) {
    let objectId = nodeObject.id

    if (objectId.includes(BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR)) {
      objectId = objectId.replaceAll(
        BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR,
        BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR_REPLACEMENT
      )
    }

    return (
      parentId + BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR + objectId
    )
  }

  _isAscendantNodeId(nodeId, ascendantNodeId) {
    if (ascendantNodeId.length < nodeId.length) {
      return nodeId.startsWith(
        ascendantNodeId + BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR
      )
    } else {
      return false
    }
  }

  _isDescendantNodeId(nodeId, descendantNodeId) {
    if (descendantNodeId.length > nodeId.length) {
      return descendantNodeId.startsWith(
        nodeId + BrowserTreeController.INTERNAL_NODE_ID_SEPARATOR
      )
    } else {
      return false
    }
  }

  async loadMoreNodes(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (
      node &&
      node.loaded &&
      !_.isNil(node.childrenLoadOffset) &&
      !_.isNil(node.childrenLoadLimit) &&
      !_.isNil(node.childrenTotalCount) &&
      node.childrenLoadOffset < node.childrenTotalCount
    ) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doLoadNode(
        newState,
        node.id,
        node.childrenLoadOffset,
        node.childrenLoadLimit,
        true
      )
      this.setState(newState)
      await this._setNodeLoading(nodeId, false)
    }
  }

  async expandNode(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      await this._setNodeLoading(nodeId, true)
      await this._doExpandNode(newState, nodeId)
      await this.setState(newState)
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
        if (
          nodeId === undoNodeId ||
          this._isAscendantNodeId(nodeId, undoNodeId) ||
          this._isDescendantNodeId(nodeId, undoNodeId)
        ) {
          delete state.undoCollapseAllIds[undoNodeId]
        }
      })
    }
  }

  async collapseNode(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      this._doCollapseNode(newState, nodeId)
      await this.setState(newState)
      this._saveSettings()
    }
  }

  async collapseAllNodes(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      const newState = { ...state }
      newState.nodes = { ...newState.nodes }
      newState.expandedIds = { ...newState.expandedIds }

      Object.keys(newState.expandedIds).forEach(expandedId => {
        if (this._isDescendantNodeId(nodeId, expandedId)) {
          delete newState.expandedIds[expandedId]
        }
      })

      if (nodeId === newState.rootId) {
        const root = newState.nodes[newState.rootId]
        if (root && root.children) {
          root.children.forEach(childId => {
            this._doCollapseNode(newState, childId, true)
            this._doCollapseNode(newState, childId, false)
          })
        }
      } else {
        this._doCollapseNode(newState, node.id, true)
        this._doCollapseNode(newState, node.id, false)
      }

      const changedToExpandedIds = []
      const changedToCollapsedIds = []
      const expandPromises = []

      const changedIds = _.xor(
        Object.keys(state.expandedIds),
        Object.keys(newState.expandedIds)
      )

      changedIds.forEach(changedId => {
        const changedToExpanded =
          state.expandedIds[changedId] === false ||
          newState.expandedIds[changedId] === true

        if (changedToExpanded) {
          const changedNode = newState.nodes[changedId]

          if (changedNode && !changedNode.loaded) {
            expandPromises.push(this._doExpandNode(newState, changedId))
          }

          changedToExpandedIds.push(changedId)
        }

        const changedToCollapsed =
          state.expandedIds[changedId] === true ||
          newState.expandedIds[changedId] === false

        if (changedToCollapsed) {
          changedToCollapsedIds.push(changedId)
        }
      })

      if (!_.isEmpty(expandPromises)) {
        await Promise.all(expandPromises)
      }

      if (
        !_.isEmpty(changedToCollapsedIds) ||
        !_.isEmpty(changedToExpandedIds)
      ) {
        const newUndoCollapseAllIds = {
          ...newState.undoCollapseAllIds,
          [nodeId]: {
            collapsedIds: changedToCollapsedIds,
            expandedIds: changedToExpandedIds
          }
        }
        newState.undoCollapseAllIds = newUndoCollapseAllIds
      }

      await this.setState(newState)
      this._saveSettings()
    }
  }

  _doCollapseNode(state, nodeId, recursive) {
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
    const state = this.getState()
    const undoCollapseAll = state.undoCollapseAllIds[nodeId]

    if (!_.isNil(undoCollapseAll)) {
      const newState = { ...state }
      const { collapsedIds, expandedIds } = undoCollapseAll
      const expandPromises = []

      collapsedIds.forEach(collapsedId => {
        expandPromises.push(this._doExpandNode(newState, collapsedId))
      })

      if (!_.isEmpty(expandPromises)) {
        await Promise.all(expandPromises)
      }

      expandedIds.forEach(expandedId => {
        this._doCollapseNode(newState, expandedId)
      })

      newState.undoCollapseAllIds = { ...newState.undoCollapseAllIds }
      delete newState.undoCollapseAllIds[nodeId]

      await this.setState(newState)
      this._saveSettings()
    }
  }

  canUndoCollapseAllNodes(nodeId) {
    const state = this.getState()
    const undoCollapseAll = state.undoCollapseAllIds[nodeId]
    return !_.isNil(undoCollapseAll)
  }

  async selectObject(nodeObject, event) {
    const state = this.getState()

    const newState = { ...state }
    await this._doSelectObject(newState, nodeObject)
    await this.setState(newState)

    const selectedNodes = Object.values(newState.nodes).filter(node =>
      _.isEqual(node.object, nodeObject)
    )

    this.onSelectedChange({
      object: nodeObject,
      nodes: selectedNodes,
      event: event
    })
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
    const state = this.getState()

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

    const pathWithoutRoot = await this.loadNodePath({
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
          _this.setState(state => {
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

    await this.setState(newState)
    this._saveSettings()
  }

  async changeSorting(nodeId, sortingId) {
    const state = this.getState()
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
      await this.setState(newState)
      await this._setNodeLoading(nodeId, false)

      this._saveSettings()
    }
  }

  async changeCustomSorting(nodeId, oldIndex, newIndex) {
    const state = this.getState()
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

        await this.setState(newState)

        this._saveSettings()
      }
    }
  }

  async clearCustomSorting(nodeId) {
    const state = this.getState()
    const node = state.nodes[nodeId]

    if (node) {
      await this._setNodeLoading(nodeId, true)

      const newState = { ...state }
      newState.customSortings = { ...newState.customSortings }
      newState.nodes = { ...newState.nodes }

      let newSorting = null

      if (!_.isEmpty(node.sortings)) {
        newSorting = node.sortings.find(sorting => !!sorting.default)
      }

      const newNode = { ...node }
      newNode.customSorting = null
      newNode.sortingId = newSorting ? newSorting.id : null

      delete newState.customSortings[nodeId]
      newState.sortingIds[nodeId] = newSorting ? newSorting.id : null
      newState.nodes[nodeId] = newNode

      await this._doLoadNode(newState, nodeId, 0, node.childrenLoadLimit, false)
      await this.setState(newState)
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
    const state = this.getState()

    if (nodeId === state.rootId) {
      await this.setState(() => ({
        loading: loading
      }))
    } else {
      await this.setState(state => ({
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
    const loaded = await this.loadSettings()

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
    const { expandedIds, sortingIds, customSortings } = this.getState()

    const settings = {
      expandedIds,
      sortingIds,
      customSortings
    }

    this.onSettingsChange(settings)
  }

  isLoading() {
    const { loading } = this.getState()
    return loading
  }

  getRoot() {
    const { rootId, nodes } = this.getState()
    return nodes[rootId]
  }

  getSelectedObject() {
    const { selectedObject } = this.getState()
    return selectedObject
  }

  getNode(nodeId) {
    const state = this.getState()
    return state.nodes[nodeId]
  }

  getNodes() {
    const state = this.getState()
    return Object.values(state.nodes)
  }

  getTree() {
    const { rootId, nodes } = this.getState()
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
