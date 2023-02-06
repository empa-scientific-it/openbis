import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import UserSearchWrapper from '@srcTest/js/components/users/search/wrapper/UserSearchWrapper.js'
import UserSearch from '@src/js/components/users/search/UserSearch.jsx'

export default class UserSearchComponentTest extends ComponentTest {
  static SUITE = 'UserSearchComponent'

  constructor() {
    super(
      ({ searchText, objectType }) => (
        <UserSearch searchText={searchText} objectType={objectType} />
      ),
      wrapper => new UserSearchWrapper(wrapper)
    )
  }
}
