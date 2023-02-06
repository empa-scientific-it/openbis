import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import Login from '@src/js/components/login/Login.jsx'
import LoginWrapper from '@srcTest/js/components/login/wrapper/LoginWrapper.js'

export default class LoginComponentTest extends ComponentTest {
  static SUITE = 'LoginComponent'

  constructor() {
    super(
      () => <Login />,
      wrapper => new LoginWrapper(wrapper)
    )
  }
}
