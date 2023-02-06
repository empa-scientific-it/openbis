import LoginComponentTest from '@srcTest/js/components/login/LoginComponentTest.js'

let common = null

beforeEach(() => {
  common = new LoginComponentTest()
  common.beforeEach()
})

describe(LoginComponentTest.SUITE, () => {
  test('login', testLogin)
})

async function testLogin() {
  const login = await common.mount()

  login.expectJSON({
    user: {
      value: null,
      focused: true,
      error: null
    },
    password: {
      value: null,
      focused: false,
      error: null
    },
    button: {
      enabled: true
    }
  })

  login.getButton().click()
  await login.update()

  login.expectJSON({
    user: {
      value: null,
      focused: true,
      error: 'User cannot be empty'
    },
    password: {
      value: null,
      focused: false,
      error: 'Password cannot be empty'
    },
    button: {
      enabled: true
    }
  })

  login.getUser().change('testUser')
  login.getButton().click()
  await login.update()

  login.expectJSON({
    user: {
      value: 'testUser',
      focused: false,
      error: null
    },
    password: {
      value: null,
      focused: true,
      error: 'Password cannot be empty'
    },
    button: {
      enabled: true
    }
  })

  login.getPassword().change('testPassword')
  login.getButton().click()
  await login.update()

  login.expectJSON({
    user: {
      value: 'testUser',
      focused: false,
      error: null
    },
    password: {
      value: 'testPassword',
      focused: true,
      error: null
    },
    button: {
      enabled: true
    }
  })
}
