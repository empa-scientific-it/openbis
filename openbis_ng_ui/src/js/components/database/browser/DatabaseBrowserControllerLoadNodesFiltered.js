import _ from 'lodash'
import DatabaseBrowserConsts from '@src/js/components/database/browser/DatabaseBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserConstsLoadNodesFiltered {
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
    spaces.forEach(space => {
      this.addSpace(entities, space)
    })

    const projects = await this.searchProjects(params)
    projects.forEach(project => {
      this.addProject(entities, project)
    })

    const experiments = await this.searchExperiments(params)
    experiments.forEach(experiment => {
      this.addExperiment(entities, experiment)
    })

    const samples = await this.searchSamples(params)
    samples.forEach(sample => {
      this.addSample(entities, sample)
    })

    const dataSets = await this.searchDataSets(params)
    dataSets.forEach(dataSet => {
      this.addDataSet(entities, dataSet)
    })

    if (node) {
      const result = {
        nodes: [],
        totalCount: 0
      }

      if (node.object.type === objectType.SPACE) {
        const nodeEntity = entities.spaces[node.object.id] || {}

        if (!_.isEmpty(nodeEntity.projects)) {
          const projectsNode = this.createProjectsNode(
            Object.values(nodeEntity.projects),
            node
          )
          result.nodes.push(projectsNode)
          result.totalCount++
        }

        if (!_.isEmpty(nodeEntity.samples)) {
          const samplesNode = this.createSamplesNode(
            Object.values(nodeEntity.samples),
            node
          )
          result.nodes.push(samplesNode)
          result.totalCount++
        }
      } else if (node.object.type === objectType.PROJECT) {
        const nodeEntity = entities.projects[node.object.id] || {}

        if (!_.isEmpty(nodeEntity.experiments)) {
          const experimentsNode = this.createExperimentsNode(
            Object.values(nodeEntity.experiments),
            node
          )
          result.nodes.push(experimentsNode)
          result.totalCount++
        }
        if (!_.isEmpty(nodeEntity.samples)) {
          const samplesNode = this.createSamplesNode(
            Object.values(nodeEntity.samples),
            node
          )
          result.nodes.push(samplesNode)
          result.totalCount++
        }
      } else if (node.object.type === objectType.COLLECTION) {
        const nodeEntity = entities.experiments[node.object.id] || {}

        if (!_.isEmpty(nodeEntity.samples)) {
          const samplesNode = this.createSamplesNode(
            Object.values(nodeEntity.samples),
            node
          )
          result.nodes.push(samplesNode)
          result.totalCount++
        }
        if (!_.isEmpty(nodeEntity.dataSets)) {
          const dataSetsNode = this.createDataSetsNode(
            Object.values(nodeEntity.dataSets),
            node
          )
          result.nodes.push(dataSetsNode)
          result.totalCount++
        }
      }

      return result
    } else {
      const root = {
        id: 'root',
        object: {
          id: 'root',
          type: 'root'
        },
        children: { nodes: [], totalCount: 0 },
        canHaveChildren: true
      }

      if (!_.isEmpty(entities.spaces)) {
        const spacesNode = this.createSpacesNode(
          Object.values(entities.spaces),
          root
        )
        root.children.nodes.push(spacesNode)
        root.children.totalCount++
      }

      if (!_.isEmpty(entities.sharedSamples)) {
        const sharedSamplesNode = this.createSamplesNode(
          Object.values(entities.sharedSamples),
          root
        )
        root.children.nodes.push(sharedSamplesNode)
        root.children.totalCount++
      }

      return {
        nodes: [root],
        totalCount: 1
      }
    }
  }

  async searchSpaces(params) {
    const { node, filter, offset, limit } = params

    if (node) {
      return []
    }

    const criteria = new openbis.SpaceSearchCriteria()
    criteria.withCode().thatContains(filter)
    const fetchOptions = new openbis.SpaceFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchProjects(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ProjectSearchCriteria()
    criteria.withCode().thatContains(filter)

    if (node) {
      if (node.object.type === objectType.SPACE) {
        criteria.withSpace().withCode().thatEquals(node.object.id)
      } else {
        return []
      }
    }

    const fetchOptions = new openbis.ProjectFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchExperiments(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    criteria.withCode().thatContains(filter)

    if (node) {
      if (node.object.type === objectType.SPACE) {
        criteria.withProject().withSpace().withCode().thatEquals(node.object.id)
      } else if (node.object.type === objectType.PROJECT) {
        criteria.withProject().withPermId().thatEquals(node.object.id)
      } else {
        return []
      }
    }

    const fetchOptions = new openbis.ExperimentFetchOptions()
    fetchOptions.withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchSamples(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withCode().thatContains(filter)

    if (node) {
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
        return []
      }
    }

    const fetchOptions = new openbis.SampleFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.withProject().withSpace()
    fetchOptions.withExperiment().withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSamples(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchDataSets(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withCode().thatContains(filter)

    if (node) {
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
        return []
      }
    }

    const fetchOptions = new openbis.DataSetFetchOptions()
    fetchOptions.withExperiment().withProject().withSpace()
    fetchOptions.withSample().withSpace()
    fetchOptions.withSample().withProject().withSpace()
    fetchOptions.withSample().withExperiment().withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchDataSets(criteria, fetchOptions)

    return result.getObjects()
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

  createSpacesNode(spaces, parent) {
    const spacesNode = {
      id: parent.id + '__' + DatabaseBrowserConsts.TYPE_SPACES,
      text: DatabaseBrowserConsts.TEXT_SPACES,
      object: {
        type: DatabaseBrowserConsts.TYPE_SPACES
      },
      canHaveChildren: true,
      children: { nodes: [], totalCount: 0 },
      selectable: false,
      expanded: true
    }

    spaces.forEach(space => {
      const spaceNode = {
        id: spacesNode.id + '__' + objectType.SPACE + '_' + space.code,
        text: space.code,
        object: {
          type: objectType.SPACE,
          id: space.code
        },
        children: { nodes: [], totalCount: 0 },
        expanded: true,
        rootable: true
      }

      spacesNode.children.nodes.push(spaceNode)
      spacesNode.children.totalCount++

      if (!_.isEmpty(space.projects)) {
        const projectsNode = this.createProjectsNode(
          Object.values(space.projects),
          spaceNode
        )
        spaceNode.canHaveChildren = true
        spaceNode.children.nodes.push(projectsNode)
        spaceNode.children.totalCount++
      }

      if (!_.isEmpty(space.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(space.samples),
          spaceNode
        )
        spaceNode.canHaveChildren = true
        spaceNode.children.nodes.push(samplesNode)
        spaceNode.children.totalCount++
      }
    })

    return spacesNode
  }

  createProjectsNode(projects, parent) {
    const projectsNode = {
      id: parent.id + '__' + DatabaseBrowserConsts.TYPE_PROJECTS,
      text: DatabaseBrowserConsts.TEXT_PROJECTS,
      object: {
        type: DatabaseBrowserConsts.TYPE_PROJECTS
      },
      canHaveChildren: true,
      children: { nodes: [], totalCount: 0 },
      selectable: false,
      expanded: true
    }

    projects.forEach(project => {
      const projectNode = {
        id: projectsNode.id + '__' + objectType.PROJECT + '_' + project.permId,
        text: project.code,
        object: {
          type: objectType.PROJECT,
          id: project.permId
        },
        children: { nodes: [], totalCount: 0 },
        expanded: true,
        rootable: true
      }

      projectsNode.children.nodes.push(projectNode)
      projectsNode.children.totalCount++

      if (!_.isEmpty(project.experiments)) {
        const experimentsNode = this.createExperimentsNode(
          Object.values(project.experiments),
          projectNode
        )
        projectNode.canHaveChildren = true
        projectNode.children.nodes.push(experimentsNode)
        projectNode.children.totalCount++
      }

      if (!_.isEmpty(project.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(project.samples),
          projectNode
        )
        projectNode.canHaveChildren = true
        projectNode.children.nodes.push(samplesNode)
        projectNode.children.totalCount++
      }
    })

    return projectsNode
  }

  createExperimentsNode(experiments, parent) {
    const experimentsNode = {
      id: parent.id + '__' + DatabaseBrowserConsts.TYPE_COLLECTIONS,
      text: DatabaseBrowserConsts.TEXT_COLLECTIONS,
      object: {
        type: DatabaseBrowserConsts.TYPE_COLLECTIONS
      },
      canHaveChildren: true,
      children: { nodes: [], totalCount: 0 },
      selectable: false,
      expanded: true
    }

    experiments.forEach(experiment => {
      const experimentNode = {
        id:
          experimentsNode.id +
          '__' +
          objectType.COLLECTION +
          '_' +
          experiment.permId,
        text: experiment.code,
        object: {
          type: objectType.COLLECTION,
          id: experiment.permId
        },
        children: { nodes: [], totalCount: 0 },
        expanded: true,
        rootable: true
      }

      experimentsNode.children.nodes.push(experimentNode)
      experimentsNode.children.totalCount++

      if (!_.isEmpty(experiment.samples)) {
        const samplesNode = this.createSamplesNode(
          Object.values(experiment.samples),
          experimentNode
        )
        experimentNode.canHaveChildren = true
        experimentNode.children.nodes.push(samplesNode)
        experimentNode.children.totalCount++
      }

      if (!_.isEmpty(experiment.dataSets)) {
        const dataSetsNode = this.createDataSetsNode(
          Object.values(experiment.dataSets),
          experimentNode
        )
        experimentNode.canHaveChildren = true
        experimentNode.children.nodes.push(dataSetsNode)
        experimentNode.children.totalCount++
      }
    })

    return experimentsNode
  }

  createSamplesNode(samples, parent) {
    const samplesNode = {
      id: parent.id + '__' + DatabaseBrowserConsts.TYPE_OBJECTS,
      text: DatabaseBrowserConsts.TEXT_OBJECTS,
      object: {
        type: DatabaseBrowserConsts.TYPE_OBJECTS
      },
      canHaveChildren: true,
      children: { nodes: [], totalCount: 0 },
      selectable: false,
      expanded: true
    }

    samples.forEach(sample => {
      const sampleNode = {
        id: samplesNode.id + '__' + objectType.OBJECT + '_' + sample.permId,
        text: sample.code,
        object: {
          type: objectType.OBJECT,
          id: sample.permId
        },
        children: { nodes: [], totalCount: 0 },
        expanded: true,
        rootable: true
      }

      samplesNode.children.nodes.push(sampleNode)
      samplesNode.children.totalCount++

      if (!_.isEmpty(sample.dataSets)) {
        const dataSetsNode = this.createDataSetsNode(
          Object.values(sample.dataSets),
          sampleNode
        )
        sampleNode.canHaveChildren = true
        sampleNode.children.nodes.push(dataSetsNode)
        sampleNode.children.totalCount++
      }
    })

    return samplesNode
  }

  createDataSetsNode(dataSets, parent) {
    const dataSetsNode = {
      id: parent.id + '__' + DatabaseBrowserConsts.TYPE_DATA_SETS,
      text: DatabaseBrowserConsts.TEXT_DATA_SETS,
      object: {
        type: DatabaseBrowserConsts.TYPE_DATA_SETS
      },
      canHaveChildren: true,
      children: { nodes: [], totalCount: 0 },
      selectable: false,
      expanded: true
    }

    dataSets.forEach(dataSet => {
      const dataSetNode = {
        id: dataSetsNode.id + '__' + objectType.DATA_SET + '_' + dataSet.code,
        text: dataSet.code,
        object: {
          type: objectType.DATA_SET,
          id: dataSet.code
        },
        children: { nodes: [], totalCount: 0 },
        expanded: true,
        rootable: true
      }

      dataSetsNode.children.nodes.push(dataSetNode)
      dataSetsNode.children.totalCount++
    })

    return dataSetsNode
  }
}
