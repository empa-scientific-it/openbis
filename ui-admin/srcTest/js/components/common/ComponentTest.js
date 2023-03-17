import React from 'react'
import { mount } from 'enzyme'
import AppController from '@src/js/components/AppController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ThemeProvider from '@srcTest/js/components/common/theme/ThemeProvider.jsx'
import DatePickerProvider from '@src/js/components/common/date/DatePickerProvider.jsx'
import openbis from '@srcTest/js/services/openbis.js'

export default class ComponentTest {
  constructor(createComponentFn, createComponentWrapperFn) {
    this.store = null
    this.createComponentFn = createComponentFn
    this.createComponentWrapperFn = createComponentWrapperFn
  }

  beforeEach() {
    jest.resetAllMocks()
    openbis.mockGetMe()
  }

  async mount() {
    document.body.innerHTML = '<div></div>'

    const appController = new AppController.AppController()
    appController.init(new ComponentContext())
    await appController.load()
    AppController.setInstance(appController)

    const reactWrapper = mount(
      <ThemeProvider>
        <DatePickerProvider>
          {this.createComponentFn.apply(null, arguments)}
        </DatePickerProvider>
      </ThemeProvider>,
      {
        attachTo: document.getElementsByTagName('div')[0]
      }
    )

    const componentWrapper = this.createComponentWrapperFn(reactWrapper)
    return componentWrapper.update().then(() => componentWrapper)
  }

  getStore() {
    return this.store
  }
}
