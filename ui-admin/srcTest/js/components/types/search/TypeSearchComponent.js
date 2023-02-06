import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import TypeSearchWrapper from '@srcTest/js/components/types/search/wrapper/TypeSearchWrapper.js'
import TypeSearch from '@src/js/components/types/search/TypeSearch.jsx'

export default class TypeSearchComponentTest extends ComponentTest {
  static SUITE = 'TypeSearchComponent'

  constructor() {
    super(
      ({ searchText, objectType }) => (
        <TypeSearch searchText={searchText} objectType={objectType} />
      ),
      wrapper => new TypeSearchWrapper(wrapper)
    )
  }
}
