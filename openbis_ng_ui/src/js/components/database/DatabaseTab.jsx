import React from 'react'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import pages from '@src/js/common/consts/pages.js'
import logger from '@src/js/common/logger.js'

class DatabaseTab extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      text: ''
    }
  }

  async componentDidMount() {
    try {
      const { tab } = this.props
      const { object } = tab

      let typeText = null
      let idText = null

      if (object.type === objectType.SPACE) {
        typeText = 'Space'
        idText = object.id
      } else if (object.type === objectType.PROJECT) {
        typeText = 'Project'
        const projects = await openbis.getProjects(
          [new openbis.ProjectPermId(object.id)],
          new openbis.ProjectFetchOptions()
        )

        if (projects[object.id]) {
          idText = projects[object.id].getCode()
        }
      } else if (object.type === objectType.COLLECTION) {
        typeText = 'Collection'
        const experiments = await openbis.getExperiments(
          [new openbis.ExperimentPermId(object.id)],
          new openbis.ExperimentFetchOptions()
        )
        if (experiments[object.id]) {
          idText = experiments[object.id].getCode()
        }
      } else if (object.type === objectType.OBJECT) {
        typeText = 'Object'
        const samples = await openbis.getSamples(
          [new openbis.SamplePermId(object.id)],
          new openbis.SampleFetchOptions()
        )
        if (samples[object.id]) {
          idText = samples[object.id].getCode()
        }
      } else if (object.type === objectType.DATA_SET) {
        typeText = 'Data Set'
        idText = object.id
      }

      const tabWithLabel = {
        ...tab,
        label: (typeText || object.type) + ': ' + (idText || object.id)
      }

      AppController.getInstance().replaceOpenTab(
        pages.DATABASE,
        tabWithLabel.id,
        tabWithLabel
      )
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DatabaseTab.render')
    return this.props.tab.label || ''
  }
}

export default DatabaseTab
