import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import ToolSearchWrapper from '@srcTest/js/components/tools/search/wrapper/ToolSearchWrapper.js'
import ToolSearch from '@src/js/components/tools/search/ToolSearch.jsx'

export default class ToolSearchComponentTest extends ComponentTest {
  static SUITE = 'ToolSearchComponent'

  constructor() {
    super(
      ({ searchText, objectType }) => (
        <ToolSearch searchText={searchText} objectType={objectType} />
      ),
      wrapper => new ToolSearchWrapper(wrapper)
    )
  }
}
