import React from 'react'
import autoBind from 'auto-bind'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class GridWithOpenbis extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    if (props.id === undefined || props.id === null) {
      throw new Error('Grid id cannot be null or undefined!')
    }

    if (props.settingsId === undefined) {
      throw new Error('Grid settingsId cannot be undefined!')
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridWithOpenbis.render')

    return (
      <Grid
        {...this.props}
        loadSettings={this.loadSettings}
        onSettingsChange={this.onSettingsChange}
        onError={this.onError}
        exportXLS={this.exportXLS}
      />
    )
  }

  getSettingsId() {
    return this.props.settingsId
  }

  async loadSettings() {
    const settingsId = this.getSettingsId()

    if (!settingsId) {
      return null
    }

    return await AppController.getInstance().getSetting(settingsId)
  }

  async onSettingsChange(settings) {
    const settingsId = this.getSettingsId()

    if (!settingsId) {
      return
    }

    await AppController.getInstance().setSetting(settingsId, settings)
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  async exportXLS({
    exportedFilePrefix,
    exportedIds,
    exportedFields,
    exportedValues,
    exportedReferredMasterData,
    exportedImportCompatible
  }) {
    const serviceId = new openbis.CustomASServiceCode(ids.EXPORT_SERVICE)

    const serviceOptions = new openbis.CustomASServiceExecutionOptions()
    serviceOptions.withParameter('method', 'export')
    serviceOptions.withParameter('file_name', exportedFilePrefix)
    serviceOptions.withParameter('ids', exportedIds)
    serviceOptions.withParameter(
      'export_referred_master_data',
      exportedReferredMasterData
    )
    serviceOptions.withParameter('export_fields', exportedFields)
    serviceOptions.withParameter('text_formatting', exportedValues)
    serviceOptions.withParameter('compatible_with_import', exportedImportCompatible)

    const sessionToken = AppController.getInstance().getSessionToken()
    const exportResult = await openbis.executeService(serviceId, serviceOptions)

    return { sessionToken, exportResult }
  }
}
