import _ from 'lodash'
import DatabaseBrowserCommon from '@src/js/components/database/browser/DatabaseBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { root, object } = params

    let path = []

    if (object.type === objectType.SPACE) {
      const space = await this.searchSpace(object.id)
      if (space) {
        const spacesFolderNode = DatabaseBrowserCommon.spacesFolderNode()
        const spaceNode = DatabaseBrowserCommon.spaceNode(object.id)
        path = [spacesFolderNode, spaceNode]
      }
    } else if (object.type === objectType.PROJECT) {
      const project = await this.searchProject(object.id)
      if (project) {
        const spacePath = await this.doLoadNodePath({
          object: {
            type: objectType.SPACE,
            id: project.getSpace().getCode()
          }
        })
        if (!_.isEmpty(spacePath)) {
          const projectsFolderNode = DatabaseBrowserCommon.projectsFolderNode()
          const projectNode = DatabaseBrowserCommon.projectNode(
            project.getPermId().getPermId(),
            project.getCode()
          )
          path = [...spacePath, projectsFolderNode, projectNode]
        }
      }
    } else if (object.type === objectType.COLLECTION) {
      const experiment = await this.searchExperiment(object.id)
      if (experiment) {
        const projectPath = await this.doLoadNodePath({
          object: {
            type: objectType.PROJECT,
            id: experiment.getProject().getPermId().getPermId()
          }
        })
        if (!_.isEmpty(projectPath)) {
          const experimentsFolderNode =
            DatabaseBrowserCommon.collectionsFolderNode()
          const experimentNode = DatabaseBrowserCommon.collectionNode(
            experiment.getPermId().getPermId(),
            experiment.getCode()
          )
          path = [...projectPath, experimentsFolderNode, experimentNode]
        }
      }
    } else if (object.type === objectType.OBJECT) {
      const sample = await this.searchSample(object.id)
      if (sample) {
        let sharedSample = false

        if (sample.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: sample.getExperiment().getPermId().getPermId()
            }
          })
          if (!_.isEmpty(experimentPath)) {
            path = [...experimentPath]
          }
        } else if (sample.getProject()) {
          const projectPath = await this.doLoadNodePath({
            object: {
              type: objectType.PROJECT,
              id: sample.getProject().getPermId().getPermId()
            }
          })
          if (!_.isEmpty(projectPath)) {
            path = [...projectPath]
          }
        } else if (sample.getSpace()) {
          const spacePath = await this.doLoadNodePath({
            object: {
              type: objectType.SPACE,
              id: sample.getSpace().getCode()
            }
          })
          if (!_.isEmpty(spacePath)) {
            path = [...spacePath]
          }
        } else {
          sharedSample = true
        }

        if (sharedSample || !_.isEmpty(path)) {
          const samplesFolderNode = DatabaseBrowserCommon.objectsFolderNode()
          const sampleNode = DatabaseBrowserCommon.objectNode(
            sample.getPermId().getPermId(),
            sample.getCode()
          )
          path = [...path, samplesFolderNode, sampleNode]
        }
      }
    } else if (object.type === objectType.DATA_SET) {
      const dataSet = await this.searchDataSet(object.id)
      if (dataSet) {
        if (dataSet.getSample()) {
          const samplePath = await this.doLoadNodePath({
            object: {
              type: objectType.OBJECT,
              id: dataSet.getSample().getPermId().getPermId()
            }
          })
          if (!_.isEmpty(samplePath)) {
            path = [...samplePath]
          }
        } else if (dataSet.getExperiment()) {
          const experimentPath = await this.doLoadNodePath({
            object: {
              type: objectType.COLLECTION,
              id: dataSet.getExperiment().getPermId().getPermId()
            }
          })
          if (!_.isEmpty(experimentPath)) {
            path = [...experimentPath]
          }
        }

        if (!_.isEmpty(path)) {
          const dataSetsFolderNode = DatabaseBrowserCommon.dataSetsFolderNode()
          const dataSetNode = DatabaseBrowserCommon.dataSetNode(
            dataSet.getCode()
          )
          path = [...path, dataSetsFolderNode, dataSetNode]
        }
      }
    }

    let pathFromRoot = [...path]

    if (root) {
      const index = pathFromRoot.findIndex(pathItem =>
        _.isEqual(pathItem.object, root.object)
      )
      if (index !== -1) {
        pathFromRoot = pathFromRoot.slice(index + 1)
      }
    }

    return pathFromRoot
  }

  async searchSpace(spaceCode) {
    const id = new openbis.SpacePermId(spaceCode)
    const fetchOptions = new openbis.SpaceFetchOptions()
    const spaces = await openbis.getSpaces([id], fetchOptions)
    return spaces[id]
  }

  async searchProject(projectPermId) {
    const id = new openbis.ProjectPermId(projectPermId)
    const fetchOptions = new openbis.ProjectFetchOptions()
    fetchOptions.withSpace()
    const projects = await openbis.getProjects([id], fetchOptions)
    return projects[id]
  }

  async searchExperiment(experimentPermId) {
    const id = new openbis.ExperimentPermId(experimentPermId)
    const fetchOptions = new openbis.ExperimentFetchOptions()
    fetchOptions.withProject()
    const experiments = await openbis.getExperiments([id], fetchOptions)
    return experiments[id]
  }

  async searchSample(samplePermId) {
    const id = new openbis.SamplePermId(samplePermId)
    const fetchOptions = new openbis.SampleFetchOptions()
    fetchOptions.withSpace()
    fetchOptions.withProject()
    fetchOptions.withExperiment()
    const samples = await openbis.getSamples([id], fetchOptions)
    return samples[id]
  }

  async searchDataSet(dataSetCode) {
    const id = new openbis.DataSetPermId(dataSetCode)
    const fetchOptions = new openbis.DataSetFetchOptions()
    fetchOptions.withExperiment()
    fetchOptions.withSample()
    const dataSets = await openbis.getDataSets([id], fetchOptions)
    return dataSets[id]
  }
}
