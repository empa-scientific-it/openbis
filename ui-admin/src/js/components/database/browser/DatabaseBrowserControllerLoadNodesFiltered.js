import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import DatabaseBrowserCommon from '@src/js/components/database/browser/DatabaseBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

export default class DatabaseBrowserControllerLoadNodesFiltered {
  constructor(controller) {
    this.controller = controller
  }

  async doLoadFilteredNodes(params) {
    const { node } = params

    const entities = {
      spaces: {},
      projects: {},
      experiments: {},
      samples: {},
      sharedSamples: {},
      dataSets: {}
    }

    const spaces = await this.searchSpaces(params)
    spaces.getObjects().forEach(space => {
      this.addSpace(entities, space)
    })

    const projects = await this.searchProjects(params)
    projects.getObjects().forEach(project => {
      this.addProject(entities, project)
    })

    const experiments = await this.searchExperiments(params)
    experiments.getObjects().forEach(experiment => {
      this.addExperiment(entities, experiment)
    })

    const samples = await this.searchSamples(params)
    samples.getObjects().forEach(sample => {
      this.addSample(entities, sample)
    })

    const dataSets = await this.searchDataSets(params)
    dataSets.getObjects().forEach(dataSet => {
      this.addDataSet(entities, dataSet)
    })

    const loadedCount =
      spaces.getObjects().length +
      projects.getObjects().length +
      experiments.getObjects().length +
      samples.getObjects().length +
      dataSets.getObjects().length

    const totalCount =
      spaces.getTotalCount() +
      projects.getTotalCount() +
      experiments.getTotalCount() +
      samples.getTotalCount() +
      dataSets.getTotalCount()

    if (totalCount > DatabaseBrowserCommon.TOTAL_LOAD_LIMIT) {
      return {
        nodes: [BrowserCommon.tooManyResultsFound()]
      }
    }

    const result = {
      nodes: [],
      totalCount: totalCount
    }

    if (node.internalRoot) {
      const root = BrowserCommon.rootNode()
      result.nodes.push(root)
    } else if (node.root) {
      if (!_.isEmpty(entities.spaces)) {
        const spacesNode = this.createSpacesNode(Object.values(entities.spaces))
        result.nodes.push(spacesNode)
      }

      if (!_.isEmpty(entities.sharedSamples)) {
        const sharedSamplesNode = this.createSamplesNode(
          Object.values(entities.sharedSamples)
        )
        result.nodes.push(sharedSamplesNode)
      }
    } else if (node.object.type === objectType.SPACE) {
      const nodeEntity = entities.spaces[node.object.id] || {}

      if (!_.isEmpty(nodeEntity.projects)) {
        const projectsNode = this.createProjectsNode(
          Object.values(nodeEntity.projects)
        )
        result.nodes.push(projectsNode)
      }

      if (!_.isEmpty(nodeEntity.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(nodeEntity.samples),
          node
        )
        result.nodes.push(samplesNode)
      }
    } else if (node.object.type === objectType.PROJECT) {
      const nodeEntity = entities.projects[node.object.id] || {}

      if (!_.isEmpty(nodeEntity.experiments)) {
        const experimentsNode = this.createExperimentsNode(
          Object.values(nodeEntity.experiments)
        )
        result.nodes.push(experimentsNode)
      }
      if (!_.isEmpty(nodeEntity.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(nodeEntity.samples),
          node
        )
        result.nodes.push(samplesNode)
      }
    } else if (node.object.type === objectType.COLLECTION) {
      const nodeEntity = entities.experiments[node.object.id] || {}

      if (!_.isEmpty(nodeEntity.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(nodeEntity.samples),
          node
        )
        result.nodes.push(samplesNode)
      }
      if (!_.isEmpty(nodeEntity.dataSets)) {
        const dataSetsNode = this.createDataSetsNode(
          Object.values(nodeEntity.dataSets)
        )
        result.nodes.push(dataSetsNode)
      }
    }

    if (loadedCount < totalCount) {
      const loadMoreNode = BrowserCommon.loadMoreResults(
        totalCount - loadedCount
      )
      loadMoreNode.onClick = () => {
        this.controller.loadNode(
          node.id,
          0,
          DatabaseBrowserCommon.TOTAL_LOAD_LIMIT,
          false
        )
      }
      result.nodes.push(loadMoreNode)
    }

    return result
  }

  async searchSpaces(params) {
    const { node, filter, offset, limit } = params

    if (node && !node.root) {
      return new openbis.SearchResult([], 0)
    }

    const criteria = new openbis.SpaceSearchCriteria()
    criteria.withCode().thatStartsWith(filter)
    const fetchOptions = new openbis.SpaceFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit || DatabaseBrowserCommon.LOAD_LIMIT)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    return result
  }

  async searchProjects(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ProjectSearchCriteria()
    criteria.withCode().thatStartsWith(filter)

    if (node && !node.root) {
      if (node.object.type === objectType.SPACE) {
        criteria.withSpace().withCode().thatEquals(node.object.id)
      } else {
        return new openbis.SearchResult([], 0)
      }
    }

    const fetchOptions = new openbis.ProjectFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit || DatabaseBrowserCommon.LOAD_LIMIT)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    return result
  }

  async searchExperiments(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    criteria.withCode().thatStartsWith(filter)

    if (node && !node.root) {
      if (node.object.type === objectType.SPACE) {
        criteria.withProject().withSpace().withCode().thatEquals(node.object.id)
      } else if (node.object.type === objectType.PROJECT) {
        criteria.withProject().withPermId().thatEquals(node.object.id)
      } else {
        return new openbis.SearchResult([], 0)
      }
    }

    const fetchOptions = new openbis.ExperimentFetchOptions()
    fetchOptions.withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit || DatabaseBrowserCommon.LOAD_LIMIT)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    return result
  }

  async searchSamples(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withCode().thatStartsWith(filter)

    if (node && !node.root) {
      if (node.object.type === objectType.SPACE) {
        const subcriteria = criteria.withSubcriteria()
        subcriteria.withOrOperator()
        subcriteria.withSpace().withCode().thatEquals(node.object.id)
        subcriteria
          .withProject()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
        subcriteria
          .withExperiment()
          .withProject()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
      } else if (node.object.type === objectType.PROJECT) {
        const subcriteria = criteria.withSubcriteria()
        subcriteria.withOrOperator()
        subcriteria.withProject().withPermId().thatEquals(node.object.id)
        subcriteria
          .withExperiment()
          .withProject()
          .withPermId()
          .thatEquals(node.object.id)
      } else if (node.object.type === objectType.COLLECTION) {
        criteria.withExperiment().withPermId().thatEquals(node.object.id)
      } else {
        return new openbis.SearchResult([], 0)
      }
    }

    const fetchOptions = new openbis.SampleFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.withProject().withSpace()
    fetchOptions.withExperiment().withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit || DatabaseBrowserCommon.LOAD_LIMIT)

    const result = await openbis.searchSamples(criteria, fetchOptions)

    return result
  }

  async searchDataSets(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withCode().thatStartsWith(filter)

    if (node && !node.root) {
      if (node.object.type === objectType.SPACE) {
        const subcriteria = criteria.withSubcriteria()
        subcriteria.withOrOperator()
        subcriteria
          .withExperiment()
          .withProject()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withProject()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withExperiment()
          .withProject()
          .withSpace()
          .withCode()
          .thatEquals(node.object.id)
      } else if (node.object.type === objectType.PROJECT) {
        const subcriteria = criteria.withSubcriteria()
        subcriteria.withOrOperator()
        subcriteria
          .withExperiment()
          .withProject()
          .withPermId()
          .thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withProject()
          .withPermId()
          .thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withExperiment()
          .withProject()
          .withPermId()
          .thatEquals(node.object.id)
      } else if (node.object.type === objectType.COLLECTION) {
        const subcriteria = criteria.withSubcriteria()
        subcriteria.withOrOperator()
        subcriteria.withExperiment().withPermId().thatEquals(node.object.id)
        subcriteria
          .withSample()
          .withExperiment()
          .withPermId()
          .thatEquals(node.object.id)
      } else {
        return new openbis.SearchResult([], 0)
      }
    }

    const fetchOptions = new openbis.DataSetFetchOptions()
    fetchOptions.withExperiment().withProject().withSpace()
    fetchOptions.withSample().withSpace()
    fetchOptions.withSample().withProject().withSpace()
    fetchOptions.withSample().withExperiment().withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit || DatabaseBrowserCommon.LOAD_LIMIT)

    const result = await openbis.searchDataSets(criteria, fetchOptions)

    return result
  }

  addSpace(entities, space) {
    const existingSpace = entities.spaces[space.getCode()]
    if (!existingSpace) {
      const newSpace = {
        code: space.getCode(),
        projects: {},
        samples: {}
      }
      entities.spaces[space.getCode()] = newSpace
      return newSpace
    } else {
      return existingSpace
    }
  }

  addProject(entities, project) {
    const existingProject = entities.projects[project.getPermId().getPermId()]
    if (!existingProject) {
      const newProject = {
        code: project.getCode(),
        permId: project.getPermId().getPermId(),
        experiments: {},
        samples: {}
      }
      const space = this.addSpace(entities, project.getSpace())
      space.projects[newProject.permId] = newProject
      entities.projects[newProject.permId] = newProject
      return newProject
    } else {
      return existingProject
    }
  }

  addExperiment(entities, experiment) {
    const existingExperiment =
      entities.experiments[experiment.getPermId().getPermId()]
    if (!existingExperiment) {
      const newExperiment = {
        code: experiment.getCode(),
        permId: experiment.getPermId().getPermId(),
        samples: {},
        dataSets: {}
      }
      const project = this.addProject(entities, experiment.getProject())
      project.experiments[newExperiment.permId] = newExperiment
      entities.experiments[newExperiment.permId] = newExperiment
      return newExperiment
    } else {
      return existingExperiment
    }
  }

  addSample(entities, sample) {
    const existingSample = entities.samples[sample.getPermId().getPermId()]
    if (!existingSample) {
      const newSample = {
        code: sample.getCode(),
        permId: sample.getPermId().getPermId(),
        dataSets: {}
      }
      if (sample.getExperiment()) {
        const experiment = this.addExperiment(entities, sample.getExperiment())
        experiment.samples[newSample.permId] = newSample
      } else if (sample.getProject()) {
        const project = this.addProject(entities, sample.getProject())
        project.samples[newSample.permId] = newSample
      } else if (sample.getSpace()) {
        const space = this.addSpace(entities, sample.getSpace())
        space.samples[newSample.permId] = newSample
      } else {
        entities.sharedSamples[newSample.permId] = newSample
      }
      entities.samples[newSample.permId] = newSample
      return newSample
    } else {
      return existingSample
    }
  }

  addDataSet(entities, dataSet) {
    const existingDataSet = entities.dataSets[dataSet.getCode()]
    if (!existingDataSet) {
      const newDataSet = {
        code: dataSet.getCode()
      }
      if (dataSet.getSample()) {
        const sample = this.addSample(entities, dataSet.getSample())
        sample.dataSets[newDataSet.code] = newDataSet
      } else if (dataSet.getExperiment()) {
        const experiment = this.addExperiment(entities, dataSet.getExperiment())
        experiment.dataSets[newDataSet.code] = newDataSet
      }
      entities.dataSets[newDataSet.code] = newDataSet
      return newDataSet
    } else {
      return existingDataSet
    }
  }

  createSpacesNode(spaces) {
    const spacesNode = DatabaseBrowserCommon.spacesFolderNode()
    spacesNode.children = { nodes: [] }
    spacesNode.expanded = true

    spaces.sort((s1, s2) => compare(s1.code, s2.code))

    spaces.forEach(space => {
      const spaceNode = DatabaseBrowserCommon.spaceNode(space.code)
      spaceNode.children = { nodes: [] }
      spaceNode.expanded = true

      spacesNode.children.nodes.push(spaceNode)

      if (!_.isEmpty(space.projects)) {
        const projectsNode = this.createProjectsNode(
          Object.values(space.projects)
        )
        spaceNode.canHaveChildren = true
        spaceNode.children.nodes.push(projectsNode)
      }

      if (!_.isEmpty(space.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(space.samples),
          spaceNode
        )
        spaceNode.canHaveChildren = true
        spaceNode.children.nodes.push(samplesNode)
      }
    })

    return spacesNode
  }

  createProjectsNode(projects) {
    const projectsNode = DatabaseBrowserCommon.projectsFolderNode()
    projectsNode.children = { nodes: [] }
    projectsNode.expanded = true

    projects.sort((p1, p2) => compare(p1.code, p2.code))

    projects.forEach(project => {
      const projectNode = DatabaseBrowserCommon.projectNode(
        project.permId,
        project.code
      )
      projectNode.children = { nodes: [] }
      projectNode.expanded = true

      projectsNode.children.nodes.push(projectNode)

      if (!_.isEmpty(project.experiments)) {
        const experimentsNode = this.createExperimentsNode(
          Object.values(project.experiments),
          projectNode
        )
        projectNode.canHaveChildren = true
        projectNode.children.nodes.push(experimentsNode)
      }

      if (!_.isEmpty(project.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(project.samples),
          projectNode
        )
        projectNode.canHaveChildren = true
        projectNode.children.nodes.push(samplesNode)
      }
    })

    return projectsNode
  }

  createExperimentsNode(experiments) {
    const experimentsNode = DatabaseBrowserCommon.collectionsFolderNode()
    experimentsNode.children = { nodes: [] }
    experimentsNode.expanded = true

    experiments.sort((e1, e2) => compare(e1.code, e2.code))

    experiments.forEach(experiment => {
      const experimentNode = DatabaseBrowserCommon.collectionNode(
        experiment.permId,
        experiment.code
      )
      experimentNode.children = { nodes: [] }
      experimentNode.expanded = true

      experimentsNode.children.nodes.push(experimentNode)

      if (!_.isEmpty(experiment.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(experiment.samples),
          experimentNode
        )
        experimentNode.canHaveChildren = true
        experimentNode.children.nodes.push(samplesNode)
      }

      if (!_.isEmpty(experiment.dataSets)) {
        const dataSetsNode = this.createDataSetsNode(
          Object.values(experiment.dataSets),
          experimentNode
        )
        experimentNode.canHaveChildren = true
        experimentNode.children.nodes.push(dataSetsNode)
      }
    })

    return experimentsNode
  }

  createSamplesNode(samples) {
    const samplesNode = DatabaseBrowserCommon.objectsFolderNode()
    samplesNode.children = { nodes: [] }
    samplesNode.expanded = true

    samples.sort((s1, s2) => compare(s1.code, s2.code))

    samples.forEach(sample => {
      const sampleNode = DatabaseBrowserCommon.objectNode(
        sample.permId,
        sample.code
      )
      sampleNode.children = { nodes: [] }
      sampleNode.expanded = true

      samplesNode.children.nodes.push(sampleNode)

      if (!_.isEmpty(sample.dataSets)) {
        const dataSetsNode = this.createDataSetsNode(
          Object.values(sample.dataSets),
          sampleNode
        )
        sampleNode.canHaveChildren = true
        sampleNode.children.nodes.push(dataSetsNode)
      }
    })

    return samplesNode
  }

  createDataSetsNode(dataSets) {
    const dataSetsNode = DatabaseBrowserCommon.dataSetsFolderNode()
    dataSetsNode.children = { nodes: [] }
    dataSetsNode.expanded = true

    dataSets.sort((d1, d2) => compare(d1.code, d2.code))

    dataSets.forEach(dataSet => {
      const dataSetNode = DatabaseBrowserCommon.dataSetNode(dataSet.code)
      dataSetNode.children = { nodes: [] }
      dataSetNode.expanded = true

      dataSetsNode.children.nodes.push(dataSetNode)
    })

    return dataSetsNode
  }
}
