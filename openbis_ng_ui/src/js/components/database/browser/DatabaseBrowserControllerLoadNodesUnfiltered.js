import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import DatabaseBrowserCommon from '@src/js/components/database/browser/DatabaseBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

const LOAD_LIMIT = 50

export default class DatabaseBrowserControllerLoadNodesUnfiltered {
  async doLoadUnfilteredNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      const nodes = []

      await this.addSpacesNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.SPACE) {
      const nodes = []

      await this.addProjectsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.PROJECT) {
      const nodes = []

      await this.addExperimentsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.COLLECTION) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OBJECT) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.DATA_SET) {
      const nodes = []

      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_SPACES) {
      return await this.searchSpaces(params)
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_PROJECTS) {
      return await this.searchProjects(params)
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_COLLECTIONS) {
      return await this.searchExperiments(params)
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_OBJECTS) {
      return await this.searchSamples(params)
    } else if (
      node.object.type === DatabaseBrowserCommon.TYPE_OBJECT_CHILDREN
    ) {
      return await this.searchSamples(params)
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_DATA_SETS) {
      return await this.searchDataSets(params)
    } else if (
      node.object.type === DatabaseBrowserCommon.TYPE_DATA_SET_CHILDREN
    ) {
      return await this.searchDataSets(params)
    }

    return null
  }

  async searchSpaces(params) {
    const { node, offset } = params

    const criteria = new openbis.SpaceSearchCriteria()
    const fetchOptions = new openbis.SpaceFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(LOAD_LIMIT)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    const nodes = result.getObjects().map(space => {
      const spaceNode = DatabaseBrowserCommon.spaceNode(node, space.getCode())
      spaceNode.canHaveChildren = true
      return spaceNode
    })

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async searchProjects(params) {
    const { node, offset } = params

    const criteria = new openbis.ProjectSearchCriteria()
    if (node.parent.object.type === objectType.SPACE) {
      criteria.withSpace().withCode().thatEquals(node.parent.object.id)
    }

    const fetchOptions = new openbis.ProjectFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(LOAD_LIMIT)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    const nodes = result.getObjects().map(project => {
      const projectNode = DatabaseBrowserCommon.projectNode(
        node,
        project.getPermId().getPermId(),
        project.getCode()
      )
      projectNode.canHaveChildren = true
      return projectNode
    })

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async searchExperiments(params) {
    const { node, offset } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    if (node.parent.object.type === objectType.PROJECT) {
      criteria.withProject().withPermId().thatEquals(node.parent.object.id)
    }

    const fetchOptions = new openbis.ExperimentFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(LOAD_LIMIT)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    const nodes = result.getObjects().map(experiment => {
      const experimentNode = DatabaseBrowserCommon.collectionNode(
        node,
        experiment.getPermId().getPermId(),
        experiment.getCode()
      )
      experimentNode.canHaveChildren = true
      return experimentNode
    })

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async searchSamples(params) {
    const { node, offset } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withAndOperator()

    if (node.parent.object.type === BrowserCommon.TYPE_ROOT) {
      criteria.withoutSpace()
      criteria.withoutProject()
      criteria.withoutExperiment()
    }
    if (node.parent.object.type === objectType.SPACE) {
      criteria.withSpace().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutProject()
    }
    if (node.parent.object.type === objectType.PROJECT) {
      criteria.withProject().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutExperiment()
    }
    if (node.parent.object.type === objectType.COLLECTION) {
      criteria.withExperiment().withPermId().thatEquals(node.parent.object.id)
    }
    if (node.parent.object.type === objectType.OBJECT) {
      criteria.withParents().withPermId().thatEquals(node.parent.object.id)
    }

    const fetchOptions = new openbis.SampleFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(LOAD_LIMIT)

    const result = await openbis.searchSamples(criteria, fetchOptions)

    const nodes = result.getObjects().map(sample => {
      const sampleNode = DatabaseBrowserCommon.objectNode(
        node,
        sample.getPermId().getPermId(),
        sample.getCode()
      )
      sampleNode.canHaveChildren = true
      return sampleNode
    })

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async searchDataSets(params) {
    const { node, offset } = params

    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withAndOperator()

    if (node.parent.object.type === objectType.COLLECTION) {
      criteria.withExperiment().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutSample()
    }
    if (node.parent.object.type === objectType.OBJECT) {
      criteria.withSample().withPermId().thatEquals(node.parent.object.id)
    }

    if (node.parent.object.type === objectType.DATA_SET) {
      criteria.withParents().withPermId().thatEquals(node.parent.object.id)
    }

    const fetchOptions = new openbis.DataSetFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(LOAD_LIMIT)

    const result = await openbis.searchDataSets(criteria, fetchOptions)

    const nodes = result.getObjects().map(dataSet => {
      const dataSetNode = DatabaseBrowserCommon.dataSetNode(
        node,
        dataSet.getCode()
      )
      dataSetNode.canHaveChildren = true
      return dataSetNode
    })

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async addSpacesNode(params, nodes) {
    const { node, sortingIds } = params

    const spacesNode = DatabaseBrowserCommon.spacesFolderNode(node)
    spacesNode.sortings = DatabaseBrowserCommon.SORTINGS
    spacesNode.sortingId =
      sortingIds[spacesNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const spaces = await this.searchSpaces({ ...params, node: spacesNode })

    if (spaces) {
      spacesNode.children = spaces
      nodes.push(spacesNode)
    }
  }

  async addProjectsNode(params, nodes) {
    const { node, sortingIds } = params

    const projectsNode = DatabaseBrowserCommon.projectsFolderNode(node)
    projectsNode.sortings = DatabaseBrowserCommon.SORTINGS
    projectsNode.sortingId =
      sortingIds[projectsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const projects = await this.searchProjects({
      ...params,
      node: projectsNode
    })

    if (projects) {
      projectsNode.children = projects
      nodes.push(projectsNode)
    }
  }

  async addExperimentsNode(params, nodes) {
    const { node, sortingIds } = params

    const experimentsNode = DatabaseBrowserCommon.collectionsFolderNode(node)
    experimentsNode.sortings = DatabaseBrowserCommon.SORTINGS
    experimentsNode.sortingId =
      sortingIds[experimentsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const experiments = await this.searchExperiments({
      ...params,
      node: experimentsNode
    })

    if (experiments) {
      experimentsNode.children = experiments
      nodes.push(experimentsNode)
    }
  }

  async addSamplesNode(params, nodes) {
    const { node, sortingIds } = params

    let samplesNode = null

    if (node.object.type === objectType.OBJECT) {
      samplesNode = DatabaseBrowserCommon.objectsChildrenFolderNode(node)
    } else {
      samplesNode = DatabaseBrowserCommon.objectsFolderNode(node)
    }

    samplesNode.sortings = DatabaseBrowserCommon.SORTINGS
    samplesNode.sortingId =
      sortingIds[samplesNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const samples = await this.searchSamples({
      ...params,
      node: samplesNode
    })

    if (samples) {
      samplesNode.children = samples
      nodes.push(samplesNode)
    }
  }

  async addDataSetsNode(params, nodes) {
    const { node, sortingIds } = params

    let dataSetsNode = null

    if (node.object.type === objectType.DATA_SET) {
      dataSetsNode = DatabaseBrowserCommon.dataSetsChildrenFolderNode(node)
    } else {
      dataSetsNode = DatabaseBrowserCommon.dataSetsFolderNode(node)
    }

    dataSetsNode.sortings = DatabaseBrowserCommon.SORTINGS
    dataSetsNode.sortingId =
      sortingIds[dataSetsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const dataSets = await this.searchDataSets({
      ...params,
      node: dataSetsNode
    })

    if (dataSets) {
      dataSetsNode.children = dataSets
      nodes.push(dataSetsNode)
    }
  }
}
