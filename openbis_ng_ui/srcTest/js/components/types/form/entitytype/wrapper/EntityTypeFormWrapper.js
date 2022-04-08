import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import ConfirmationDialogWrapper from '@srcTest/js/components/common/dialog/wrapper/ConfirmationDialogWrapper.js'
import EntityTypeFormPreview from '@src/js/components/types/form/entitytype/EntityTypeFormPreview.jsx'
import EntityTypeFormParameters from '@src/js/components/types/form/entitytype/EntityTypeFormParameters.jsx'
import EntityTypeFormButtons from '@src/js/components/types/form/entitytype/EntityTypeFormButtons.jsx'
import EntityTypeFormPreviewWrapper from './EntityTypeFormPreviewWrapper.js'
import EntityTypeFormParametersWrapper from './EntityTypeFormParametersWrapper.js'
import EntityTypeFormButtonsWrapper from './EntityTypeFormButtonsWrapper.js'
import EntityTypeFormDialogRemoveProperty from '@src/js/components/types/form/entitytype/EntityTypeFormDialogRemoveProperty.jsx'
import EntityTypeFormDialogRemoveSection from '@src/js/components/types/form/entitytype/EntityTypeFormDialogRemoveSection.jsx'

export default class EntityTypeFormWrapper extends BaseWrapper {
  getPreview() {
    return new EntityTypeFormPreviewWrapper(
      this.findComponent(EntityTypeFormPreview)
    )
  }

  getParameters() {
    return new EntityTypeFormParametersWrapper(
      this.findComponent(EntityTypeFormParameters)
    )
  }

  getButtons() {
    return new EntityTypeFormButtonsWrapper(
      this.findComponent(EntityTypeFormButtons)
    )
  }

  getRemovePropertyDialog() {
    const propertyDialog = this.findComponent(
      EntityTypeFormDialogRemoveProperty
    )
    return new ConfirmationDialogWrapper(
      this.findComponent(ConfirmationDialog, propertyDialog)
    )
  }

  getRemoveSectionDialog() {
    const sectionDialog = this.findComponent(EntityTypeFormDialogRemoveSection)
    return new ConfirmationDialogWrapper(
      this.findComponent(ConfirmationDialog, sectionDialog)
    )
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON(),
      removePropertyDialog: this.getRemovePropertyDialog().toJSON(),
      removeSectionDialog: this.getRemoveSectionDialog().toJSON()
    }
  }
}
