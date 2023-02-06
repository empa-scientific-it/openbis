import React from 'react'
import AppController from '@src/js/components/AppController.js'
import PageMode from '@src/js/components/common/page/PageMode.js'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import users from '@src/js/common/consts/users.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class VocabularyTypeFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyTypeFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, vocabulary } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={vocabulary.id ? onCancel : null}
        renderAdditionalButtons={params => this.renderAdditionalButtons(params)}
      />
    )
  }

  renderAdditionalButtons({ mode, classes }) {
    if (mode === PageMode.EDIT) {
      const { onAdd, onRemove } = this.props

      return (
        <React.Fragment>
          <Button
            name='addTerm'
            label={messages.get(messages.ADD_TERM)}
            styles={{ root: classes.button }}
            onClick={onAdd}
          />
          <Button
            name='removeTerm'
            label={messages.get(messages.REMOVE_TERM)}
            styles={{ root: classes.button }}
            disabled={
              !(
                this.isNonSystemInternalTermSelected() ||
                AppController.getInstance().isSystemUser()
              )
            }
            onClick={onRemove}
          />
        </React.Fragment>
      )
    } else {
      return null
    }
  }

  isNonSystemInternalTermSelected() {
    const { selection, vocabulary, terms } = this.props

    if (selection && selection.type === VocabularyTypeFormSelectionType.TERM) {
      const term = terms.find(term => term.id === selection.params.id)
      return !(
        vocabulary.internal.value && term.registrator.value === users.SYSTEM
      )
    } else {
      return false
    }
  }
}

export default VocabularyTypeFormButtons
