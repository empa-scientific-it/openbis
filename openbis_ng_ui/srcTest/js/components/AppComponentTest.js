import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import AppWrapper from '@srcTest/js/components/wrapper/AppWrapper.js'
import App from '@src/js/components/App.jsx'
import openbis from '@srcTest/js/services/openbis.js'

export default class AppComponentTest extends ComponentTest {
  static SUITE = 'AppComponent'

  constructor() {
    super(
      () => <App />,
      wrapper => new AppWrapper(wrapper)
    )
  }

  async beforeEach() {
    super.beforeEach()

    openbis.login.mockReturnValue(Promise.resolve('testSession'))
    openbis.mockSearchSampleTypes([])
    openbis.mockSearchExperimentTypes([])
    openbis.mockSearchDataSetTypes([])
    openbis.mockSearchMaterialTypes([])
    openbis.mockSearchVocabularies([])
    openbis.mockSearchPersons([])
    openbis.mockSearchGroups([])
    openbis.mockSearchPlugins([])
    openbis.mockSearchQueries([])
  }

  async login(app) {
    app.getLogin().getUser().change('testUser')
    app.getLogin().getPassword().change('testPassword')
    app.getLogin().getButton().click()
    await app.update()
  }
}
