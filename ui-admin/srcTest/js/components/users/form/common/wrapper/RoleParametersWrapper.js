import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class RoleParametersWrapper extends PageParametersPanelWrapper {
  getLevel() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'level' })
    )
  }

  getSpace() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'space' })
    )
  }

  getProject() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'project' })
    )
  }

  getRole() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'role' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      level: this.getLevel().toJSON(),
      space: this.getSpace().toJSON(),
      project: this.getProject().toJSON(),
      role: this.getRole().toJSON()
    }
  }
}
