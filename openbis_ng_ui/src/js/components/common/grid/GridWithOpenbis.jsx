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
        onExportXLS={this.onExportXLS}
        onExportTSV={this.onExportTSV}
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

    const id = new openbis.Me()
    const fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      const person = map[id]
      const webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[settingsId]
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
    const settingsId = this.getSettingsId()

    if (!settingsId) {
      return
    }

    const gridSettings = new openbis.WebAppSettingCreation()
    gridSettings.setName(settingsId)
    gridSettings.setValue(JSON.stringify(settings))

    const update = new openbis.PersonUpdate()
    update.setUserId(new openbis.Me())
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    await openbis.updatePersons([update])
  }

  async onExportXLS({
    exportedFilePrefix,
    exportedIds,
    exportedProperties,
    exportedValues
  }) {
    try {
      AppController.getInstance().loadingChange(true)

      const serviceId = new openbis.CustomASServiceCode(ids.EXPORT_SERVICE)

      const serviceOptions = new openbis.CustomASServiceExecutionOptions()
      serviceOptions.withParameter('method', 'export')
      serviceOptions.withParameter('file_name', exportedFilePrefix)
      serviceOptions.withParameter('ids', exportedIds)
      serviceOptions.withParameter('export_referred', true)
      serviceOptions.withParameter('export_properties', exportedProperties)
      serviceOptions.withParameter('text_formatting', exportedValues)

      const result = await openbis.executeService(serviceId, serviceOptions)

      if (result.status === 'OK') {
        const filePath = result.result
        const fileName = filePath.substring(filePath.lastIndexOf('/') + 1)
        const fileUrl =
          '/openbis/download/?sessionID=' +
          encodeURIComponent(AppController.getInstance().getSessionToken()) +
          '&filePath=' +
          encodeURIComponent(filePath)

        const link = document.createElement('a')
        link.href = fileUrl
        link.download = fileName
        link.click()
      } else if (result.status === 'error') {
        AppController.getInstance().errorChange(result.message)
      } else {
        AppController.getInstance().errorChange(JSON.stringify(result))
      }
    } catch (e) {
      AppController.getInstance().errorChange(JSON.stringify(e.message))
    } finally {
      AppController.getInstance().loadingChange(false)
    }
  }

  async onExportTSV({ exportedFileDownload }) {
    try {
      AppController.getInstance().loadingChange(true)
      await exportedFileDownload()
    } catch (e) {
      AppController.getInstance().errorChange(JSON.stringify(e.message))
    } finally {
      AppController.getInstance().loadingChange(false)
    }
  }
}
