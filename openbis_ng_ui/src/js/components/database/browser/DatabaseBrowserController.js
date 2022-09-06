import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import openbis from '@src/js/services/openbis.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodes({ node, filter, offset, limit }) {
    if (node === null) {
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
        text: space.getCode(),
        object: {
          type: 'space',
          id: space.getCode()
        },
        canHaveChildren: true
      }))
    } else if (node.object.type === 'space') {
      const criteria = new openbis.ProjectSearchCriteria()
      criteria.withSpace().withCode().thatEquals(node.object.id)
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
        text: project.getCode(),
        object: {
          type: 'project',
          id: project.getPermId().getPermId()
        },
        canHaveChildren: true
      }))
    } else if (node.object.type === 'project') {
      const criteria = new openbis.ExperimentSearchCriteria()
      criteria.withProject().withPermId().thatEquals(node.object.id)
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
        text: experiment.getCode(),
        object: {
          type: 'experiment',
          id: experiment.getPermId().getPermId()
        },
        canHaveChildren: true
      }))
    } else if (node.object.type === 'experiment') {
      const criteria = new openbis.SampleSearchCriteria()
      criteria.withExperiment().withPermId().thatEquals(node.object.id)
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
        text: sample.getCode(),
        object: {
          type: 'sample',
          id: sample.getPermId().getPermId()
        }
      }))
    } else {
      return null
    }
  }
}
