import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import openbis from '@src/js/services/openbis.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodes(params) {
    const { node, filter } = params

    if (node.object.type === 'root') {
      if (filter === null) {
        return await this.searchSpaces(params)
      } else {
        const [spaces, projects, experiments, samples] = await Promise.all([
          this.searchSpaces(params),
          this.searchProjects(params),
          this.searchExperiments(params),
          this.searchSamples(params)
        ])
        return {
          nodes: [
            ...spaces.nodes,
            ...projects.nodes,
            ...experiments.nodes,
            ...samples.nodes
          ],
          totalCount:
            spaces.totalCount +
            projects.totalCount +
            experiments.totalCount +
            samples.totalCount
        }
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
    const { node, filter, offset, limit } = params

    const criteria = new openbis.SpaceSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }

    const fetchOptions = new openbis.SpaceFetchOptions()
    fetchOptions.sortBy().code().asc()
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    const nodes = result.getObjects().map(space => ({
      id: 'space_' + space.getCode() + '_in_' + node.id,
      text: space.getCode() + (filter ? ' (space)' : ''),
      object: {
        type: 'space',
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
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ProjectSearchCriteria()
    if (node.object.type === 'space') {
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

    const nodes = result.getObjects().map(project => ({
      id: 'project_' + project.getPermId().getPermId() + '_in_' + node.id,
      text: project.getCode() + (filter ? ' (project)' : ''),
      object: {
        type: 'project',
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
    const { node, filter, offset, limit } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    if (node.object.type === 'project') {
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

    const nodes = result.getObjects().map(experiment => ({
      id: 'experiment_' + experiment.getPermId().getPermId() + '_in_' + node.id,
      text: experiment.getCode() + (filter ? ' (experiment)' : ''),
      object: {
        type: 'experiment',
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
    const { node, filter, offset, limit } = params

    const criteria = new openbis.SampleSearchCriteria()
    if (node.object.type === 'experiment') {
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

    const nodes = result.getObjects().map(sample => ({
      id: 'sample_' + sample.getPermId().getPermId() + '_in_' + node.id,
      text: sample.getCode() + (filter ? ' (sample)' : ''),
      object: {
        type: 'sample',
        id: sample.getPermId().getPermId()
      }
    }))

    return {
      nodes: nodes,
      totalCount: result.getTotalCount()
    }
  }
}
