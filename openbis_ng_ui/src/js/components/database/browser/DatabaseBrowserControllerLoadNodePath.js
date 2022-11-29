import _ from 'lodash'
import DatabaseBrowserConsts from '@src/js/components/database/browser/DatabaseBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserConstsLoadNodePath {
  async doLoadNodePath(params) {
    const { root, object } = params

    let path = []

    if (object.type === objectType.SPACE) {
      path = [{ type: DatabaseBrowserConsts.TYPE_SPACES }, object]
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
          path = [
            ...spacePath,
            { type: DatabaseBrowserConsts.TYPE_PROJECTS },
            object
          ]
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
          path = [
            ...projectPath,
            { type: DatabaseBrowserConsts.TYPE_COLLECTIONS },
            object
          ]
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
            path = [
              ...experimentPath,
              { type: DatabaseBrowserConsts.TYPE_OBJECTS },
              object
            ]
          }
        } else if (sample.getProject()) {
          const projectPath = await this.doLoadNodePath({
            object: {
              type: objectType.PROJECT,
              id: sample.getProject().getPermId().getPermId()
            }
          })
          if (projectPath) {
            path = [
              ...projectPath,
              { type: DatabaseBrowserConsts.TYPE_OBJECTS },
              object
            ]
          }
        } else if (sample.getSpace()) {
          const spacePath = await this.doLoadNodePath({
            object: {
              type: objectType.SPACE,
              id: sample.getSpace().getCode()
            }
          })
          if (spacePath) {
            path = [
              ...spacePath,
              { type: DatabaseBrowserConsts.TYPE_OBJECTS },
              object
            ]
          }
        } else {
          path = [{ type: DatabaseBrowserConsts.TYPE_OBJECTS }, object]
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
            path = [
              ...samplePath,
              { type: DatabaseBrowserConsts.TYPE_DATA_SETS },
              object
            ]
          }
        } else if (dataSet.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: dataSet.getExperiment().getPermId().getPermId()
            }
          })
          if (experimentPath) {
            path = [
              ...experimentPath,
              { type: DatabaseBrowserConsts.TYPE_DATA_SETS },
              object
            ]
          }
        }
      }
    }

    let pathFromRoot = [...path]

    if (root) {
      const index = pathFromRoot.findIndex(pathItem =>
        _.isEqual(pathItem, root.object)
      )
      if (index !== -1) {
        pathFromRoot = pathFromRoot.slice(index + 1)
      }
    }

    return pathFromRoot
  }
}
