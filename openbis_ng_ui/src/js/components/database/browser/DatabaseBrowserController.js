import _ from 'lodash'
import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

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
      return [object]
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
          return [...spacePath, object]
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
          return [...projectPath, object]
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
            return [...experimentPath, object]
          }
        } else if (sample.getProject()) {
          const projectPath = await this.doLoadNodePath({
            object: {
              type: objectType.PROJECT,
              id: sample.getProject().getPermId().getPermId()
            }
          })
          if (projectPath) {
            return [...projectPath, object]
          }
        } else if (sample.getSpace()) {
          const spacePath = await this.doLoadNodePath({
            object: {
              type: objectType.SPACE,
              id: sample.getSpace().getCode()
            }
          })
          if (spacePath) {
            return [...spacePath, object]
          }
        } else {
          return [object]
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
            return [...samplePath, object]
          }
        } else if (dataSet.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: dataSet.getExperiment().getPermId().getPermId()
            }
          })
          if (experimentPath) {
            return [...experimentPath, object]
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
      return {
        nodes: [
          {
            id: 'spaces_in_' + node.id,
            text: 'Spaces',
            object: {
              type: 'spaces'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'objects_in_' + node.id,
            text: 'Objects',
            object: {
              type: 'objects'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 2
      }
    } else if (node.object.type === objectType.SPACE) {
      return {
        nodes: [
          {
            id: 'projects_in_' + node.id,
            text: 'Projects',
            object: {
              type: 'projects'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'objects_in_' + node.id,
            text: 'Objects',
            object: {
              type: 'objects'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 2
      }
    } else if (node.object.type === objectType.PROJECT) {
      return {
        nodes: [
          {
            id: 'collections_in_' + node.id,
            text: 'Collections',
            object: {
              type: 'collections'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'objects_in_' + node.id,
            text: 'Objects',
            object: {
              type: 'objects'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 2
      }
    } else if (node.object.type === objectType.COLLECTION) {
      return {
        nodes: [
          {
            id: 'objects_in_' + node.id,
            text: 'Objects',
            object: {
              type: 'objects'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'datasets_in_' + node.id,
            text: 'Data Sets',
            object: {
              type: 'dataSets'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 2
      }
    } else if (node.object.type === objectType.OBJECT) {
      return {
        nodes: [
          {
            id: 'children_in_' + node.id,
            text: 'Children',
            object: {
              type: 'objectChildren'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'components_in_' + node.id,
            text: 'Components',
            object: {
              type: 'objectComponents'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'datasets_in_' + node.id,
            text: 'Data Sets',
            object: {
              type: 'dataSets'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 3
      }
    } else if (node.object.type === objectType.DATA_SET) {
      return {
        nodes: [
          {
            id: 'children_in_' + node.id,
            text: 'Children',
            object: {
              type: 'dataSetChildren'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          },
          {
            id: 'components_in_' + node.id,
            text: 'Components',
            object: {
              type: 'dataSetComponents'
            },
            parent: node,
            canHaveChildren: true,
            sortings: SORTINGS,
            sortingId: 'code_asc',
            expanded: true
          }
        ],
        totalCount: 2
      }
    } else if (node.object.type === 'spaces') {
      return this.searchSpaces(params)
    } else if (node.object.type === 'projects') {
      return this.searchProjects(params)
    } else if (node.object.type === 'collections') {
      return this.searchExperiments(params)
    } else if (node.object.type === 'objects') {
      return this.searchSamples(params)
    } else if (node.object.type === 'objectChildren') {
      return this.searchSamples(params)
    } else if (node.object.type === 'objectComponents') {
      return this.searchSamples(params)
    } else if (node.object.type === 'dataSets') {
      return this.searchDataSets(params)
    } else if (node.object.type === 'dataSetChildren') {
      return this.searchDataSets(params)
    } else if (node.object.type === 'dataSetComponents') {
      return this.searchDataSets(params)
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
      id: objectType.SPACE + '_' + space.getCode() + '_in_' + node.parent.id,
      text: space.getCode(),
      object: {
        type: objectType.SPACE,
        id: space.getCode()
      },
      canHaveChildren: true
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
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
        objectType.PROJECT +
        '_' +
        project.getPermId().getPermId() +
        '_in_' +
        node.parent.id,
      text: project.getCode(),
      object: {
        type: objectType.PROJECT,
        id: project.getPermId().getPermId()
      },
      canHaveChildren: true
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
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
        objectType.COLLECTION +
        '_' +
        experiment.getPermId().getPermId() +
        '_in_' +
        node.parent.id,
      text: experiment.getCode(),
      object: {
        type: objectType.COLLECTION,
        id: experiment.getPermId().getPermId()
      },
      canHaveChildren: true
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
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
      if (node.object.type === 'objectChildren') {
        criteria.withParents().withPermId().thatEquals(node.parent.object.id)
      }
      if (node.object.type === 'objectComponents') {
        criteria.withContainer().withPermId().thatEquals(node.parent.object.id)
      }
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
        objectType.OBJECT +
        '_' +
        sample.getPermId().getPermId() +
        '_in_' +
        node.parent.id,
      text: sample.getCode(),
      object: {
        type: objectType.OBJECT,
        id: sample.getPermId().getPermId()
      },
      canHaveChildren: true
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
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
      if (node.object.type === 'dataSetChildren') {
        criteria.withParents().withPermId().thatEquals(node.parent.object.id)
      }
      if (node.object.type === 'dataSetComponents') {
        criteria.withContainer().withPermId().thatEquals(node.parent.object.id)
      }
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
        objectType.DATA_SET +
        '_' +
        dataSet.getPermId().getPermId() +
        '_in_' +
        node.parent.id,
      text: dataSet.getCode(),
      object: {
        type: objectType.DATA_SET,
        id: dataSet.getPermId().getPermId()
      },
      canHaveChildren: true
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
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
        id: 'spaces_in_' + parent.id,
        text: 'Spaces',
        object: {
          type: 'spaces'
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        sortings: SORTINGS,
        sortingId: 'code_asc',
        expanded: true
      }

      spaces.forEach(space => {
        const spaceNode = {
          id: objectType.SPACE + '_' + space.code + '_in_' + spacesNode.id,
          text: space.code + (space.matching ? ' (*)' : ''),
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
        id: 'projects_in_' + parent.id,
        text: 'Projects',
        object: {
          type: 'projects'
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        sortings: SORTINGS,
        sortingId: 'code_asc',
        expanded: true
      }

      projects.forEach(project => {
        const projectNode = {
          id:
            objectType.PROJECT +
            '_' +
            project.permId +
            '_in_' +
            projectsNode.id,
          text: project.code + (project.matching ? ' (*)' : ''),
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
        id: 'collections_in_' + parent.id,
        text: 'Collections',
        object: {
          type: 'collections'
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        sortings: SORTINGS,
        sortingId: 'code_asc',
        expanded: true
      }

      experiments.forEach(experiment => {
        const experimentNode = {
          id:
            objectType.COLLECTION +
            '_' +
            experiment.permId +
            '_in_' +
            experimentsNode.id,
          text: experiment.code + (experiment.matching ? ' (*)' : ''),
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
        id: 'objects_in_' + parent.id,
        text: 'Objects',
        object: {
          type: 'objects'
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        sortings: SORTINGS,
        sortingId: 'code_asc',
        expanded: true
      }

      samples.forEach(sample => {
        const sampleNode = {
          id: objectType.OBJECT + '_' + sample.permId + '_in_' + samplesNode.id,
          text: sample.code + (sample.matching ? ' (*)' : ''),
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
        id: 'datasets_in_' + parent.id,
        text: 'Data Sets',
        object: {
          type: 'dataSets'
        },
        canHaveChildren: true,
        children: { nodes: [], totalCount: 0 },
        sortings: SORTINGS,
        sortingId: 'code_asc',
        expanded: true
      }

      dataSets.forEach(dataSet => {
        const dataSetNode = {
          id:
            objectType.DATA_SET + '_' + dataSet.code + '_in_' + dataSetsNode.id,
          text: dataSet.code + (dataSet.matching ? ' (*)' : ''),
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
      children: { nodes: [], totalCount: 0 }
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
      root.canHaveChildren = true
      root.children.nodes.push(spacesNode)
      root.children.totalCount++
    }

    if (!_.isEmpty(entities.sharedSamples)) {
      const sharedSamplesNode = createSamplesNode(
        Object.values(entities.sharedSamples),
        root
      )
      root.canHaveChildren = true
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
