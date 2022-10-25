import React from 'react'
import autoBind from 'auto-bind'
import Browser from '@src/js/components/common/browser2/Browser.jsx'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class BrowserWithSettings extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    if (!props.id) {
      throw new Error('Browser id cannot be empty!')
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserWithSettings.render')
    return (
      <Browser
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
        let browserSettings = webAppSettings.settings[this.props.id]
        if (browserSettings) {
          let settings = JSON.parse(browserSettings.value)
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
    const browserSettings = new openbis.WebAppSettingCreation()
    browserSettings.setName(this.props.id)
    browserSettings.setValue(JSON.stringify(settings))

    const update = new openbis.PersonUpdate()
    update.setUserId(new openbis.Me())
    update.getWebAppSettings(ids.WEB_APP_ID).add(browserSettings)

    await openbis.updatePersons([update])
  }
}
