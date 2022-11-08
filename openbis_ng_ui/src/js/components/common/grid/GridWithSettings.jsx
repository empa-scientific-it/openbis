import React from 'react'
import autoBind from 'auto-bind'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class GridWithSettings extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    if (!props.id) {
      throw new Error('Grid id cannot be empty!')
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridWithSettings.render')
    return (
      <Grid
        {...this.props}
        loadSettings={this.loadSettings}
        onSettingsChange={this.onSettingsChange}
        scheduleExport={this.props.exportable ? this.scheduleExport : null}
        loadExported={this.props.exportable ? this.loadExported : null}
      />
    )
  }

  async loadSettings() {
    const id = new openbis.Me()
    const fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      const person = map[id]
      const webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[this.props.id]
        if (gridSettings) {
          let settings = JSON.parse(gridSettings.value)
          if (settings) {
            return settings
          } else {
            return null
          }
        }
      }
    })
  }

  async onSettingsChange(settings) {
    const gridSettings = new openbis.WebAppSettingCreation()
    gridSettings.setName(this.props.id)
    gridSettings.setValue(JSON.stringify(settings))

    const update = new openbis.PersonUpdate()
    update.setUserId(new openbis.Me())
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    await openbis.updatePersons([update])
  }

  async scheduleExport({ exportedIds, exportedProperties, exportedValues }) {
    try {
      AppController.getInstance().loadingChange(true)

      const serviceId = new openbis.CustomASServiceCode(ids.EXPORT_SERVICE)

      const serviceOptions = new openbis.CustomASServiceExecutionOptions()
      serviceOptions.withParameter('method', 'export')
      serviceOptions.withParameter('file_name', this.props.id)
      serviceOptions.withParameter('ids', exportedIds)
      serviceOptions.withParameter('export_referred', true)
      serviceOptions.withParameter('export_properties', exportedProperties)
      serviceOptions.withParameter('text_formatting', exportedValues)

      const result = await openbis.executeService(serviceId, serviceOptions)

      if (result.status === 'OK') {
        let downloadUrl =
          '/openbis/download/?sessionID=' +
          encodeURIComponent(AppController.getInstance().getSessionToken()) +
          '&filePath=' +
          encodeURIComponent(result.result)
        window.open(downloadUrl, '_blank').focus()
      } else if (result.status === 'error') {
        AppController.getInstance().errorChange(result.message)
      } else {
        AppController.getInstance().errorChange(JSON.stringify(result))
      }
    } catch (error) {
      AppController.getInstance().errorChange(JSON.stringify(error))
    } finally {
      AppController.getInstance().loadingChange(false)
    }
  }

  async loadExported() {}
}
