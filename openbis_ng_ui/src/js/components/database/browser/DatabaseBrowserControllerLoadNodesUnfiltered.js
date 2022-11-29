import _ from 'lodash'
import DatabaseBrowserConsts from '@src/js/components/database/browser/DatabaseBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserConstsLoadNodesUnfiltered {
  async doLoadUnfilteredNodes(params) {
    const { node } = params

    if (!node) {
      return {
        nodes: [
          {
            id: 'root',
            object: {
              id: 'root',
              type: 'root'
            },
            canHaveChildren: true
          }
        ],
        totalCount: 1
      }
    } else if (node.object.type === 'root') {
      const nodes = []

      await this.addSpacesNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.SPACE) {
      const nodes = []

      await this.addProjectsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.PROJECT) {
      const nodes = []

      await this.addExperimentsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.COLLECTION) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.OBJECT) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.DATA_SET) {
      const nodes = []

      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === DatabaseBrowserConsts.TYPE_SPACES) {
      return await this.searchSpaces(params)
    } else if (node.object.type === DatabaseBrowserConsts.TYPE_PROJECTS) {
      return await this.searchProjects(params)
    } else if (node.object.type === DatabaseBrowserConsts.TYPE_COLLECTIONS) {
      return await this.searchExperiments(params)
    } else if (node.object.type === DatabaseBrowserConsts.TYPE_OBJECTS) {
      return await this.searchSamples(params)
    } else if (
      node.object.type === DatabaseBrowserConsts.TYPE_OBJECT_CHILDREN
    ) {
      return await this.searchSamples(params)
    } else if (node.object.type === DatabaseBrowserConsts.TYPE_DATA_SETS) {
      return await this.searchDataSets(params)
    } else if (
      node.object.type === DatabaseBrowserConsts.TYPE_DATA_SET_CHILDREN
    ) {
      return await this.searchDataSets(params)
    } else {
      return null
    }
  }

  async searchSpaces(params) {
    const { node, offset, limit } = params

    const criteria = new openbis.SpaceSearchCriteria()
    const fetchOptions = new openbis.SpaceFetchOptions()
    if (node.sortings && node.sortingId) {
      const sorting = node.sortings[node.sortingId]
      if (sorting) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    const nodes = result.getObjects().map(space => ({
      id: node.id + '__' + objectType.SPACE + '_' + space.getCode(),
      text: space.getCode(),
      object: {
        type: objectType.SPACE,
        id: space.getCode()
      },
      canHaveChildren: true,
      rootable: true
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        totalCount: result.getTotalCount()
      }
    }
  }

  async searchProjects(params) {
    const { node, offset, limit } = params

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
    fetchOptions.count(limit)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    const nodes = result.getObjects().map(project => ({
      id:
        node.id +
        '__' +
        objectType.PROJECT +
        '_' +
        project.getPermId().getPermId(),
      text: project.getCode(),
      object: {
        type: objectType.PROJECT,
        id: project.getPermId().getPermId()
      },
      canHaveChildren: true,
      rootable: true
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        totalCount: result.getTotalCount()
      }
    }
  }

  async searchExperiments(params) {
    const { node, offset, limit } = params

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
    fetchOptions.count(limit)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    const nodes = result.getObjects().map(experiment => ({
      id:
        node.id +
        '__' +
        objectType.COLLECTION +
        '_' +
        experiment.getPermId().getPermId(),
      text: experiment.getCode(),
      object: {
        type: objectType.COLLECTION,
        id: experiment.getPermId().getPermId()
      },
      canHaveChildren: true,
      rootable: true
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        totalCount: result.getTotalCount()
      }
    }
  }

  async searchSamples(params) {
    const { node, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withAndOperator()

    if (node.parent.object.type === 'root') {
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
    fetchOptions.count(limit)

    const result = await openbis.searchSamples(criteria, fetchOptions)

    const nodes = result.getObjects().map(sample => ({
      id:
        node.id +
        '__' +
        objectType.OBJECT +
        '_' +
        sample.getPermId().getPermId(),
      text: sample.getCode(),
      object: {
        type: objectType.OBJECT,
        id: sample.getPermId().getPermId()
      },
      canHaveChildren: true,
      rootable: true
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        totalCount: result.getTotalCount()
      }
    }
  }

  async searchDataSets(params) {
    const { node, offset, limit } = params

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
    fetchOptions.count(limit)

    const result = await openbis.searchDataSets(criteria, fetchOptions)

    const nodes = result.getObjects().map(dataSet => ({
      id:
        node.id +
        '__' +
        objectType.DATA_SET +
        '_' +
        dataSet.getPermId().getPermId(),
      text: dataSet.getCode(),
      object: {
        type: objectType.DATA_SET,
        id: dataSet.getPermId().getPermId()
      },
      canHaveChildren: true,
      rootable: true
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        totalCount: result.getTotalCount()
      }
    }
  }

  async addSpacesNode(params, nodes) {
    const { node } = params

    const spacesNode = {
      id: node.id + '__' + DatabaseBrowserConsts.TYPE_SPACES,
      text: DatabaseBrowserConsts.TEXT_SPACES,
      object: {
        type: DatabaseBrowserConsts.TYPE_SPACES
      },
      parent: node,
      canHaveChildren: true,
      selectable: false,
      sortings: DatabaseBrowserConsts.SORTINGS,
      sortingId: 'code_asc'
    }

    const spaces = await this.searchSpaces({ ...params, node: spacesNode })

    if (spaces) {
      spacesNode.children = spaces
      nodes.push(spacesNode)
    }
  }

  async addProjectsNode(params, nodes) {
    const { node } = params

    const projectsNode = {
      id: node.id + '__' + DatabaseBrowserConsts.TYPE_PROJECTS,
      text: DatabaseBrowserConsts.TEXT_PROJECTS,
      object: {
        type: DatabaseBrowserConsts.TYPE_PROJECTS
      },
      parent: node,
      canHaveChildren: true,
      selectable: false,
      sortings: DatabaseBrowserConsts.SORTINGS,
      sortingId: 'code_asc'
    }

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
    const { node } = params

    const experimentsNode = {
      id: node.id + '__' + DatabaseBrowserConsts.TYPE_COLLECTIONS,
      text: DatabaseBrowserConsts.TEXT_COLLECTIONS,
      object: {
        type: DatabaseBrowserConsts.TYPE_COLLECTIONS
      },
      parent: node,
      canHaveChildren: true,
      selectable: false,
      sortings: DatabaseBrowserConsts.SORTINGS,
      sortingId: 'code_asc'
    }

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
    const { node } = params

    let samplesNode = {
      parent: node,
      canHaveChildren: true,
      selectable: false,
      sortings: DatabaseBrowserConsts.SORTINGS,
      sortingId: 'code_asc'
    }

    if (node.object.type === objectType.OBJECT) {
      samplesNode = {
        ...samplesNode,
        id: node.id + '__' + DatabaseBrowserConsts.TYPE_OBJECT_CHILDREN,
        text: DatabaseBrowserConsts.TEXT_OBJECT_CHILDREN,
        object: {
          type: DatabaseBrowserConsts.TYPE_OBJECT_CHILDREN
        }
      }
    } else {
      samplesNode = {
        ...samplesNode,
        id: node.id + '__' + DatabaseBrowserConsts.TYPE_OBJECTS,
        text: DatabaseBrowserConsts.TEXT_OBJECTS,
        object: {
          type: DatabaseBrowserConsts.TYPE_OBJECTS
        }
      }
    }

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
    const { node } = params

    let dataSetsNode = {
      parent: node,
      canHaveChildren: true,
      selectable: false,
      sortings: DatabaseBrowserConsts.SORTINGS,
      sortingId: 'code_asc'
    }

    if (node.object.type === objectType.DATA_SET) {
      dataSetsNode = {
        ...dataSetsNode,
        id: node.id + '__' + DatabaseBrowserConsts.TYPE_DATA_SET_CHILDREN,
        text: DatabaseBrowserConsts.TEXT_DATA_SET_CHILDREN,
        object: {
          type: DatabaseBrowserConsts.TYPE_DATA_SET_CHILDREN
        }
      }
    } else {
      dataSetsNode = {
        ...dataSetsNode,
        id: node.id + '__' + DatabaseBrowserConsts.TYPE_DATA_SETS,
        text: DatabaseBrowserConsts.TEXT_DATA_SETS,
        object: {
          type: DatabaseBrowserConsts.TYPE_DATA_SETS
        }
      }
    }

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
