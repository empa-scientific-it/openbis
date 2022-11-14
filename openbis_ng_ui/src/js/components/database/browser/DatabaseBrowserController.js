import _ from 'lodash'
import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

const TYPE_SPACES = 'spaces'
const TYPE_PROJECTS = 'projects'
const TYPE_COLLECTIONS = 'collections'
const TYPE_OBJECTS = 'objects'
const TYPE_OBJECT_CHILDREN = 'objectChildren'
const TYPE_DATA_SETS = 'dataSets'
const TYPE_DATA_SET_CHILDREN = 'dataSetChildren'

const TEXT_SPACES = 'Spaces'
const TEXT_PROJECTS = 'Projects'
const TEXT_COLLECTIONS = 'Collections'
const TEXT_OBJECTS = 'Objects'
const TEXT_OBJECT_CHILDREN = 'Children'
const TEXT_DATA_SETS = 'Data Sets'
const TEXT_DATA_SET_CHILDREN = 'Children'

const SORTINGS = {
  code_asc: {
    label: 'Code ASC',
    sortBy: 'code',
    sortDirection: 'asc',
    index: 0
  },
  code_desc: {
    label: 'Code DESC',
    sortBy: 'code',
    sortDirection: 'desc',
    index: 1
  },
  registration_date_asc: {
    label: 'Registration Date ASC',
    sortBy: 'registrationDate',
    sortDirection: 'asc',
    index: 2
  },
  registration_date_desc: {
    label: 'Registration Date DESC',
    sortBy: 'registrationDate',
    sortDirection: 'desc',
    index: 3
  }
}

export default class DatabaseBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.SPACE) {
      return [{ type: TYPE_SPACES }, object]
    } else if (object.type === objectType.PROJECT) {
      const id = new openbis.ProjectPermId(object.id)
      const fetchOptions = new openbis.ProjectFetchOptions()
      fetchOptions.withSpace()

      const projects = await openbis.getProjects([id], fetchOptions)
      const project = projects[id]

      if (project) {
        const spacePath = await this.doLoadNodePath({
          object: {
            type: objectType.SPACE,
            id: project.getSpace().getCode()
          }
        })
        if (spacePath) {
          return [...spacePath, { type: TYPE_PROJECTS }, object]
        }
      }
    } else if (object.type === objectType.COLLECTION) {
      const id = new openbis.ExperimentPermId(object.id)
      const fetchOptions = new openbis.ExperimentFetchOptions()
      fetchOptions.withProject()

      const experiments = await openbis.getExperiments([id], fetchOptions)
      const experiment = experiments[id]

      if (experiment) {
        const projectPath = await this.doLoadNodePath({
          object: {
            type: objectType.PROJECT,
            id: experiment.getProject().getPermId().getPermId()
          }
        })
        if (projectPath) {
          return [...projectPath, { type: TYPE_COLLECTIONS }, object]
        }
      }
    } else if (object.type === objectType.OBJECT) {
      const id = new openbis.SamplePermId(object.id)
      const fetchOptions = new openbis.SampleFetchOptions()
      fetchOptions.withSpace()
      fetchOptions.withProject()
      fetchOptions.withExperiment()

      const samples = await openbis.getSamples([id], fetchOptions)
      const sample = samples[id]

      if (sample) {
        if (sample.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: sample.getExperiment().getPermId().getPermId()
            }
          })
          if (experimentPath) {
            return [...experimentPath, { type: TYPE_OBJECTS }, object]
          }
        } else if (sample.getProject()) {
          const projectPath = await this.doLoadNodePath({
            object: {
              type: objectType.PROJECT,
              id: sample.getProject().getPermId().getPermId()
            }
          })
          if (projectPath) {
            return [...projectPath, { type: TYPE_OBJECTS }, object]
          }
        } else if (sample.getSpace()) {
          const spacePath = await this.doLoadNodePath({
            object: {
              type: objectType.SPACE,
              id: sample.getSpace().getCode()
            }
          })
          if (spacePath) {
            return [...spacePath, { type: TYPE_OBJECTS }, object]
          }
        } else {
          return [{ type: TYPE_OBJECTS }, object]
        }
      }
    } else if (object.type === objectType.DATA_SET) {
      const id = new openbis.DataSetPermId(object.id)
      const fetchOptions = new openbis.DataSetFetchOptions()
      fetchOptions.withExperiment()
      fetchOptions.withSample()

      const dataSets = await openbis.getDataSets([id], fetchOptions)
      const dataSet = dataSets[id]

      if (dataSet) {
        if (dataSet.getSample()) {
          const samplePath = await this.doLoadNodePath({
            object: {
              type: objectType.OBJECT,
              id: dataSet.getSample().getPermId().getPermId()
            }
          })
          if (samplePath) {
            return [...samplePath, { type: TYPE_DATA_SETS }, object]
          }
        } else if (dataSet.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: dataSet.getExperiment().getPermId().getPermId()
            }
          })
          if (experimentPath) {
            return [...experimentPath, { type: TYPE_DATA_SETS }, object]
          }
        }
      }
    }

    return null
  }

  async doLoadNodes(params) {
    const { filter } = params

    if (filter) {
      return await this.doLoadFilteredNodes(params)
    } else {
      return await this.doLoadUnfilteredNodes(params)
    }
  }

  async doLoadUnfilteredNodes(params) {
    const _this = this
    const { node } = params

    async function addSpacesNode(nodes) {
      const spacesNode = {
        id: node.id + '__' + TYPE_SPACES,
        text: TEXT_SPACES,
        object: {
          type: TYPE_SPACES
        },
        parent: node,
        canHaveChildren: true,
        selectable: false,
        sortings: SORTINGS,
        sortingId: 'code_asc'
      }

      const spaces = await _this.searchSpaces({ ...params, node: spacesNode })

      if (spaces) {
        spacesNode.children = spaces
        nodes.push(spacesNode)
      }
    }

    async function addProjectsNode(nodes) {
      const projectsNode = {
        id: node.id + '__' + TYPE_PROJECTS,
        text: TEXT_PROJECTS,
        object: {
          type: TYPE_PROJECTS
        },
        parent: node,
        canHaveChildren: true,
        selectable: false,
        sortings: SORTINGS,
        sortingId: 'code_asc'
      }

      const projects = await _this.searchProjects({
        ...params,
        node: projectsNode
      })

      if (projects) {
        projectsNode.children = projects
        nodes.push(projectsNode)
      }
    }

    async function addExperimentsNode(nodes) {
      const experimentsNode = {
        id: node.id + '__' + TYPE_COLLECTIONS,
        text: TEXT_COLLECTIONS,
        object: {
          type: TYPE_COLLECTIONS
        },
        parent: node,
        canHaveChildren: true,
        selectable: false,
        sortings: SORTINGS,
        sortingId: 'code_asc'
      }

      const experiments = await _this.searchExperiments({
        ...params,
        node: experimentsNode
      })

      if (experiments) {
        experimentsNode.children = experiments
        nodes.push(experimentsNode)
      }
    }

    async function addSamplesNode(nodes) {
      let samplesNode = {
        parent: node,
        canHaveChildren: true,
        selectable: false,
        sortings: SORTINGS,
        sortingId: 'code_asc'
      }

      if (node.object.type === objectType.OBJECT) {
        samplesNode = {
          ...samplesNode,
          id: node.id + '__' + TYPE_OBJECT_CHILDREN,
          text: TEXT_OBJECT_CHILDREN,
          object: {
            type: TYPE_OBJECT_CHILDREN
          }
        }
      } else {
        samplesNode = {
          ...samplesNode,
          id: node.id + '__' + TYPE_OBJECTS,
          text: TEXT_OBJECTS,
          object: {
            type: TYPE_OBJECTS
          }
        }
      }

      const samples = await _this.searchSamples({
        ...params,
        node: samplesNode
      })

      if (samples) {
        samplesNode.children = samples
        nodes.push(samplesNode)
      }
    }

    async function addDataSetsNode(nodes) {
      let dataSetsNode = {
        parent: node,
        canHaveChildren: true,
        selectable: false,
        sortings: SORTINGS,
        sortingId: 'code_asc'
      }

      if (node.object.type === objectType.DATA_SET) {
        dataSetsNode = {
          ...dataSetsNode,
          id: node.id + '__' + TYPE_DATA_SET_CHILDREN,
          text: TEXT_DATA_SET_CHILDREN,
          object: {
            type: TYPE_DATA_SET_CHILDREN
          }
        }
      } else {
        dataSetsNode = {
          ...dataSetsNode,
          id: node.id + '__' + TYPE_DATA_SETS,
          text: TEXT_DATA_SETS,
          object: {
            type: TYPE_DATA_SETS
          }
        }
      }

      const dataSets = await _this.searchDataSets({
        ...params,
        node: dataSetsNode
      })

      if (dataSets) {
        dataSetsNode.children = dataSets
        nodes.push(dataSetsNode)
      }
    }

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

      await addSpacesNode(nodes)
      await addSamplesNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.SPACE) {
      const nodes = []

      await addProjectsNode(nodes)
      await addSamplesNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.PROJECT) {
      const nodes = []

      await addExperimentsNode(nodes)
      await addSamplesNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.COLLECTION) {
      const nodes = []

      await addSamplesNode(nodes)
      await addDataSetsNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.OBJECT) {
      const nodes = []

      await addSamplesNode(nodes)
      await addDataSetsNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === objectType.DATA_SET) {
      const nodes = []

      await addDataSetsNode(nodes)

      return {
        nodes: nodes,
        totalCount: nodes.length
      }
    } else if (node.object.type === TYPE_SPACES) {
      return await this.searchSpaces(params)
    } else if (node.object.type === TYPE_PROJECTS) {
      return await this.searchProjects(params)
    } else if (node.object.type === TYPE_COLLECTIONS) {
      return await this.searchExperiments(params)
    } else if (node.object.type === TYPE_OBJECTS) {
      return await this.searchSamples(params)
    } else if (node.object.type === TYPE_OBJECT_CHILDREN) {
      return await this.searchSamples(params)
    } else if (node.object.type === TYPE_DATA_SETS) {
      return await this.searchDataSets(params)
    } else if (node.object.type === TYPE_DATA_SET_CHILDREN) {
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
      canHaveChildren: true
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
      canHaveChildren: true
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
      canHaveChildren: true
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
      canHaveChildren: true
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
      canHaveChildren: true
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

  async doLoadFilteredNodes(params) {
    function addSpace(entities, space) {
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

    function addProject(entities, project) {
      const existingProject = entities.projects[project.getPermId().getPermId()]
      if (!existingProject) {
        const newProject = {
          code: project.getCode(),
          permId: project.getPermId().getPermId(),
          experiments: {},
          samples: {}
        }
        const space = addSpace(entities, project.getSpace())
        space.projects[newProject.permId] = newProject
        entities.projects[newProject.permId] = newProject
        return newProject
      } else {
        return existingProject
      }
    }

    function addExperiment(entities, experiment) {
      const existingExperiment =
        entities.experiments[experiment.getPermId().getPermId()]
      if (!existingExperiment) {
        const newExperiment = {
          code: experiment.getCode(),
          permId: experiment.getPermId().getPermId(),
          samples: {},
          dataSets: {}
        }
        const project = addProject(entities, experiment.getProject())
        project.experiments[newExperiment.permId] = newExperiment
        entities.experiments[newExperiment.permId] = newExperiment
        return newExperiment
      } else {
        return existingExperiment
      }
    }

    function addSample(entities, sample) {
      const existingSample = entities.samples[sample.getPermId().getPermId()]
      if (!existingSample) {
        const newSample = {
          code: sample.getCode(),
          permId: sample.getPermId().getPermId(),
          dataSets: {}
        }
        if (sample.getExperiment()) {
          const experiment = addExperiment(entities, sample.getExperiment())
          experiment.samples[newSample.permId] = newSample
        } else if (sample.getProject()) {
          const project = addProject(entities, sample.getProject())
          project.samples[newSample.permId] = newSample
        } else if (sample.getSpace()) {
          const space = addSpace(entities, sample.getSpace())
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

    function addDataSet(entities, dataSet) {
      const existingDataSet = entities.dataSets[dataSet.getCode()]
      if (!existingDataSet) {
        const newDataSet = {
          code: dataSet.getCode()
        }
        if (dataSet.getSample()) {
          const sample = addSample(entities, dataSet.getSample())
          sample.dataSets[newDataSet.code] = newDataSet
        } else if (dataSet.getExperiment()) {
          const experiment = addExperiment(entities, dataSet.getExperiment())
          experiment.dataSets[newDataSet.code] = newDataSet
        }
        entities.dataSets[newDataSet.code] = newDataSet
        return newDataSet
      } else {
        return existingDataSet
      }
    }

    function createSpacesNode(spaces, parent) {
      const spacesNode = {
        id: parent.id + '__' + TYPE_SPACES,
        text: TEXT_SPACES,
        object: {
          type: TYPE_SPACES
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
          expanded: true
        }

        spacesNode.children.nodes.push(spaceNode)
        spacesNode.children.totalCount++

        if (!_.isEmpty(space.projects)) {
          const projectsNode = createProjectsNode(
            Object.values(space.projects),
            spaceNode
          )
          spaceNode.canHaveChildren = true
          spaceNode.children.nodes.push(projectsNode)
          spaceNode.children.totalCount++
        }

        if (!_.isEmpty(space.samples)) {
          const samplesNode = createSamplesNode(
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

    function createProjectsNode(projects, parent) {
      const projectsNode = {
        id: parent.id + '__' + TYPE_PROJECTS,
        text: TEXT_PROJECTS,
        object: {
          type: TYPE_PROJECTS
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        selectable: false,
        expanded: true
      }

      projects.forEach(project => {
        const projectNode = {
          id:
            projectsNode.id + '__' + objectType.PROJECT + '_' + project.permId,
          text: project.code,
          object: {
            type: objectType.PROJECT,
            id: project.permId
          },
          children: { nodes: [], totalCount: 0 },
          expanded: true
        }

        projectsNode.children.nodes.push(projectNode)
        projectsNode.children.totalCount++

        if (!_.isEmpty(project.experiments)) {
          const experimentsNode = createExperimentsNode(
            Object.values(project.experiments),
            projectNode
          )
          projectNode.canHaveChildren = true
          projectNode.children.nodes.push(experimentsNode)
          projectNode.children.totalCount++
        }

        if (!_.isEmpty(project.samples)) {
          const samplesNode = createSamplesNode(
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

    function createExperimentsNode(experiments, parent) {
      const experimentsNode = {
        id: parent.id + '__' + TYPE_COLLECTIONS,
        text: TEXT_COLLECTIONS,
        object: {
          type: TYPE_COLLECTIONS
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
          expanded: true
        }

        experimentsNode.children.nodes.push(experimentNode)
        experimentsNode.children.totalCount++

        if (!_.isEmpty(experiment.samples)) {
          const samplesNode = createSamplesNode(
            Object.values(experiment.samples),
            experimentNode
          )
          experimentNode.canHaveChildren = true
          experimentNode.children.nodes.push(samplesNode)
          experimentNode.children.totalCount++
        }

        if (!_.isEmpty(experiment.dataSets)) {
          const dataSetsNode = createDataSetsNode(
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

    function createSamplesNode(samples, parent) {
      const samplesNode = {
        id: parent.id + '__' + TYPE_OBJECTS,
        text: TEXT_OBJECTS,
        object: {
          type: TYPE_OBJECTS
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
          expanded: true
        }

        samplesNode.children.nodes.push(sampleNode)
        samplesNode.children.totalCount++

        if (!_.isEmpty(sample.dataSets)) {
          const dataSetsNode = createDataSetsNode(
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

    function createDataSetsNode(dataSets, parent) {
      const dataSetsNode = {
        id: parent.id + '__' + TYPE_DATA_SETS,
        text: TEXT_DATA_SETS,
        object: {
          type: TYPE_DATA_SETS
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
          expanded: true
        }

        dataSetsNode.children.nodes.push(dataSetNode)
        dataSetsNode.children.totalCount++
      })

      return dataSetsNode
    }

    const root = {
      id: 'root',
      object: {
        id: 'root',
        type: 'root'
      },
      children: { nodes: [], totalCount: 0 },
      canHaveChildren: true
    }

    const entities = {
      spaces: {},
      projects: {},
      experiments: {},
      samples: {},
      sharedSamples: {},
      dataSets: {}
    }

    const spaces = await this.searchSpacesFiltered(params)
    spaces.forEach(space => {
      const addedSpace = addSpace(entities, space)
      addedSpace.matching = true
    })

    const projects = await this.searchProjectsFiltered(params)
    projects.forEach(project => {
      const addedProject = addProject(entities, project)
      addedProject.matching = true
    })

    const experiments = await this.searchExperimentsFiltered(params)
    experiments.forEach(experiment => {
      const addedExperiment = addExperiment(entities, experiment)
      addedExperiment.matching = true
    })

    const samples = await this.searchSamplesFiltered(params)
    samples.forEach(sample => {
      const addedSample = addSample(entities, sample)
      addedSample.matching = true
    })

    const dataSets = await this.searchDataSetsFiltered(params)
    dataSets.forEach(dataSet => {
      const addedDataSet = addDataSet(entities, dataSet)
      addedDataSet.matching = true
    })

    if (!_.isEmpty(entities.spaces)) {
      const spacesNode = createSpacesNode(Object.values(entities.spaces), root)
      root.children.nodes.push(spacesNode)
      root.children.totalCount++
    }

    if (!_.isEmpty(entities.sharedSamples)) {
      const sharedSamplesNode = createSamplesNode(
        Object.values(entities.sharedSamples),
        root
      )
      root.children.nodes.push(sharedSamplesNode)
      root.children.totalCount++
    }

    const result = {
      nodes: [root],
      totalCount: 1
    }

    return result
  }

  async searchSpacesFiltered(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.SpaceSearchCriteria()
    criteria.withCode().thatContains(filter)
    const fetchOptions = new openbis.SpaceFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchProjectsFiltered(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.ProjectSearchCriteria()
    criteria.withCode().thatContains(filter)
    const fetchOptions = new openbis.ProjectFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchExperimentsFiltered(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    criteria.withCode().thatContains(filter)
    const fetchOptions = new openbis.ExperimentFetchOptions()
    fetchOptions.withProject().withSpace()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    return result.getObjects()
  }

  async searchSamplesFiltered(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withCode().thatContains(filter)
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

  async searchDataSetsFiltered(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withCode().thatContains(filter)
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
}
