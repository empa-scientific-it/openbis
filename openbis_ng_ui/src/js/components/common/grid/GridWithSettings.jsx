import React from 'react'
import autoBind from 'auto-bind'
import Grid from '@src/js/components/common/grid/Grid.jsx'
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
}
