import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ActiveUserReportController from '@src/js/components/tools/form/activeUserReport/ActiveUserReportController.js'
import ActiveUserReportFacade from '@src/js/components/tools/form/activeUserReport/ActiveUserReportFacade.js'

jest.mock('@src/js/components/tools/form/activeUserReport/ActiveUserReportFacade.js')

const SUITE = 'ActiveUserReportFormComponentTest'

let facade = null
let context = null
let controller = null

beforeEach(() => {
  facade = new ActiveUserReportFacade()
  context = new ComponentContext()
  controller = new ActiveUserReportController(facade)
  controller.init(context)
})

describe(SUITE, () => {
  test('sendReport', testSendReport)
  test('loadActiveUsersCount', testLoadActiveUsersCount)
  test('loadOpenbisSupportEmail', testLoadOpenbisSupportEmail)
})

async function testSendReport() {
  const good = {"status": "OK"}
  facade.sendReport.mockReturnValue(Promise.resolve(good))
  await controller.sendReport()

  expect(context.getState()).toMatchObject({result: {success: true, output: good}})

  const bad = {"status": "error", "message": "bad output"}
  facade.sendReport.mockReturnValue(Promise.resolve(bad))
  await controller.sendReport()

  expect(context.getState()).toMatchObject({result: {success: false, output: bad}})
}

async function testLoadActiveUsersCount() {
  const one = {"activeUsersCount": 1}

  facade.loadActiveUsersCount.mockReturnValue(Promise.resolve(1))
  await controller.loadActiveUsersCount()

  expect(context.getState()).toMatchObject(one)
}

async function testLoadOpenbisSupportEmail() {
  const withoutEmail = {"openbisSupportEmail": null}

  facade.loadOpenbisSupportEmail.mockReturnValue(Promise.resolve(null))
  await controller.loadOpenbisSupportEmail()

  expect(context.getState()).toMatchObject(withoutEmail)

  const withEmail = {"openbisSupportEmail": "test.com"}

  facade.loadOpenbisSupportEmail.mockReturnValue(Promise.resolve("test.com"))
  await controller.loadOpenbisSupportEmail()

  expect(context.getState()).toMatchObject(withEmail)
}
