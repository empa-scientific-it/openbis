import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import openbis from '@src/js/services/openbis.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodes(params) {
    const { node, filter } = params

    if (node === null) {
      if (filter === null) {
        return await this.searchSpaces(params)
      } else {
        const spaces = await this.searchSpaces(params)
        const projects = await this.searchProjects(params)
        const experiments = await this.searchExperiments(params)
        const samples = await this.searchSamples(params)
        return [...spaces, ...projects, ...experiments, ...samples]
      }
    } else if (node.object.type === 'space') {
      return await this.searchProjects(params)
    } else if (node.object.type === 'project') {
      return await this.searchExperiments(params)
    } else if (node.object.type === 'experiment') {
      return await this.searchSamples(params)
    } else {
      return null
    }
  }

  async searchSpaces(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.SpaceSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }

    const fetchOptions = new openbis.SpaceFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    return result.getObjects().map(space => ({
      id: 'space_' + space.getCode(),
      text: space.getCode() + (filter ? ' (space)' : ''),
      object: {
        type: 'space',
        id: space.getCode()
      },
      canHaveChildren: true
    }))
  }

  async searchProjects(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ProjectSearchCriteria()
    if (node) {
      criteria.withSpace().withCode().thatEquals(node.object.id)
    }
    if (filter) {
      criteria.withCode().thatContains(filter)
    }

    const fetchOptions = new openbis.ProjectFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchProjects(criteria, fetchOptions)

    return result.getObjects().map(project => ({
      id: 'project_' + project.getPermId().getPermId(),
      text: project.getCode() + (filter ? ' (project)' : ''),
      object: {
        type: 'project',
        id: project.getPermId().getPermId()
      },
      canHaveChildren: true
    }))
  }

  async searchExperiments(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    if (node) {
      criteria.withProject().withPermId().thatEquals(node.object.id)
    }
    if (filter) {
      criteria.withCode().thatContains(filter)
    }

    const fetchOptions = new openbis.ExperimentFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchExperiments(criteria, fetchOptions)

    return result.getObjects().map(experiment => ({
      id: 'experiment_' + experiment.getPermId().getPermId(),
      text: experiment.getCode() + (filter ? ' (experiment)' : ''),
      object: {
        type: 'experiment',
        id: experiment.getPermId().getPermId()
      },
      canHaveChildren: true
    }))
  }

  async searchSamples(params) {
    const { node, filter, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    if (node) {
      criteria.withExperiment().withPermId().thatEquals(node.object.id)
    }
    if (filter) {
      criteria.withCode().thatContains(filter)
    }

    const fetchOptions = new openbis.SampleFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSamples(criteria, fetchOptions)

    return result.getObjects().map(sample => ({
      id: 'sample_' + sample.getPermId().getPermId(),
      text: sample.getCode() + (filter ? ' (sample)' : ''),
      object: {
        type: 'sample',
        id: sample.getPermId().getPermId()
      }
    }))
  }
}
