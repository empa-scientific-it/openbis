import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import UserBrowser from '@src/js/components/users/browser/UsersBrowser.jsx'
import UserBrowserController from '@src/js/components/users/browser/UsersBrowserController.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let store = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  controller = new UserBrowserController()
})

describe('browser', () => {
  test('test', done => {
    const searchPersonResult = new openbis.SearchResult()
    searchPersonResult.setObjects([
      fixture.TEST_USER_DTO,
      fixture.ANOTHER_USER_DTO
    ])

    const searchGroupsResult = new openbis.SearchResult()
    searchGroupsResult.setObjects([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    openbis.searchPersons.mockReturnValue(Promise.resolve(searchPersonResult))
    openbis.searchAuthorizationGroups.mockReturnValue(
      Promise.resolve(searchGroupsResult)
    )

    store.dispatch(actions.init())

    let wrapper = mount(
      <Provider store={store}>
        <UserBrowser controller={controller} />
      </Provider>
    )

    setTimeout(() => {
      wrapper.update()

      expectFilter(wrapper, '')
      expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 0, text: 'Groups' }
      ])

      simulateNodeIconClick(wrapper, 'users')
      wrapper.update()

      expectFilter(wrapper, '')
      expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 1, text: fixture.TEST_USER_DTO.userId },
        { level: 0, text: 'Groups' }
      ])

      simulateFilterChange(
        wrapper,
        fixture.ANOTHER_GROUP_DTO.code.toUpperCase()
      )
      wrapper.update()

      expectFilter(wrapper, fixture.ANOTHER_GROUP_DTO.code.toUpperCase())
      expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 2, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 0, text: 'Groups' },
        { level: 1, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 2, text: fixture.ANOTHER_USER_DTO.userId }
      ])

      done()
    })
  })
})

function simulateNodeIconClick(wrapper, id) {
  wrapper
    .findWhere(node => {
      return node.name() === 'BrowserNode' && node.prop('node').id === id
    })
    .find('svg')
    .first()
    .simulate('click')
}

function simulateFilterChange(wrapper, filter) {
  let input = wrapper.find('FilterField').find('input')
  input.instance().value = filter
  input.simulate('change')
}

function expectFilter(wrapper, expectedFilter) {
  const actualFilter = wrapper.find('FilterField').map(node => {
    return node.prop('filter')
  })[0]
  expect(actualFilter).toEqual(expectedFilter)
}

function expectNodes(wrapper, expectedNodes) {
  const actualNodes = wrapper.find('BrowserNode').map(node => {
    const text = node.prop('node').text
    const selected = node.prop('node').selected
    const level = node.prop('level')
    return {
      text,
      level,
      selected
    }
  })
  expect(actualNodes).toMatchObject(expectedNodes)
}
