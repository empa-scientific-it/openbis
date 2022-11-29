import _ from 'lodash'
import DatabaseBrowserConsts from '@src/js/components/database/browser/DatabaseBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserConstsLoadNodePath {
  async doLoadNodePath(params) {
    const { root, object } = params

    let path = []

    if (object.type === objectType.SPACE) {
      const id = new openbis.SpacePermId(object.id)
      const fetchOptions = new openbis.SpaceFetchOptions()

      const spaces = await openbis.getSpaces([id], fetchOptions)
      const space = spaces[id]

      if (space) {
        path = [
          {
            object: { type: DatabaseBrowserConsts.TYPE_SPACES },
            text: DatabaseBrowserConsts.TEXT_SPACES
          },
          {
            object,
            text: object.id,
            rootable: true
          }
        ]
      }
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
            {
              object: { type: DatabaseBrowserConsts.TYPE_PROJECTS },
              text: DatabaseBrowserConsts.TEXT_PROJECTS
            },
            {
              object,
              text: project.getCode(),
              rootable: true
            }
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
            {
              object: { type: DatabaseBrowserConsts.TYPE_COLLECTIONS },
              text: DatabaseBrowserConsts.TEXT_COLLECTIONS
            },
            {
              object,
              text: experiment.getCode(),
              rootable: true
            }
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
              {
                object: { type: DatabaseBrowserConsts.TYPE_OBJECTS },
                text: DatabaseBrowserConsts.TEXT_OBJECTS
              },
              {
                object,
                text: sample.getCode(),
                rootable: true
              }
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
              {
                object: { type: DatabaseBrowserConsts.TYPE_OBJECTS },
                text: DatabaseBrowserConsts.TEXT_OBJECTS
              },
              {
                object,
                text: sample.getCode(),
                rootable: true
              }
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
              {
                object: { type: DatabaseBrowserConsts.TYPE_OBJECTS },
                text: DatabaseBrowserConsts.TEXT_OBJECTS
              },
              {
                object,
                text: sample.getCode(),
                rootable: true
              }
            ]
          }
        } else {
          path = [
            {
              object: { type: DatabaseBrowserConsts.TYPE_OBJECTS },
              text: DatabaseBrowserConsts.TEXT_OBJECTS
            },
            {
              object,
              text: sample.getCode(),
              rootable: true
            }
          ]
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
              {
                object: { type: DatabaseBrowserConsts.TYPE_DATA_SETS },
                text: DatabaseBrowserConsts.TEXT_DATA_SETS
              },
              {
                object,
                text: dataSet.getCode(),
                rootable: true
              }
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
              {
                object: { type: DatabaseBrowserConsts.TYPE_DATA_SETS },
                text: DatabaseBrowserConsts.TEXT_DATA_SETS
              },
              {
                object,
                text: dataSet.getCode(),
                rootable: true
              }
            ]
          }
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
}
