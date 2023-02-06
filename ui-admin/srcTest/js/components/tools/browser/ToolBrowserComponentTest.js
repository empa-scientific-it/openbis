import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import ToolBrowser from '@src/js/components/tools/browser/ToolBrowser.jsx'

export default class ToolBrowserComponentTest extends ComponentTest {
  static SUITE = 'ToolBrowserComponent'

  constructor() {
    super(
      () => <ToolBrowser />,
      wrapper => new BrowserWrapper(wrapper)
    )
  }
}
