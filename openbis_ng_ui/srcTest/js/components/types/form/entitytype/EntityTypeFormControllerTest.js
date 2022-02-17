import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import EntityTypeFormControler from '@src/js/components/types/form/entitytype/EntityTypeFormController.js'
import EntityTypeFormFacade from '@src/js/components/types/form/entitytype/EntityTypeFormFacade'

jest.mock('@src/js/components/types/form/entitytype/EntityTypeFormFacade')

export default class EntityTypeFormControllerTest {
  static SUITE = 'EntityTypeFormController'

  beforeEach() {
    jest.resetAllMocks()
  }

  init(object) {
    this.context = new ComponentContext()
    this.context.setProps({
      object
    })
    this.facade = new EntityTypeFormFacade()
    this.controller = new EntityTypeFormControler(this.facade)
    this.controller.init(this.context)
  }

  afterEach() {
    expect(this.facade.loadType).toHaveBeenCalledWith(
      this.context.getProps().object
    )
  }
}
