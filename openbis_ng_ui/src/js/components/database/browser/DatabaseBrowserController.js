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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
            sortingId: 'code_asc'
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
}
