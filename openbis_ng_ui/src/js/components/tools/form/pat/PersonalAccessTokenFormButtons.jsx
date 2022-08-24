import React from 'react'
import PageMode from '@src/js/components/common/page/PageMode.js'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class PersonalAccessTokenFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'PersonalAccessTokenFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={onCancel}
        renderAdditionalButtons={params => this.renderAdditionalButtons(params)}
      />
    )
  }

  renderAdditionalButtons({ mode, classes }) {
    if (mode === PageMode.EDIT) {
      const { selection, onAdd, onRemove } = this.props

      return (
        <React.Fragment>
          <Button
            name='addToken'
            label={messages.get(messages.ADD_TOKEN)}
            styles={{ root: classes.button }}
            onClick={onAdd}
          />
          <Button
            name='removeToken'
            label={messages.get(messages.REMOVE_TOKEN)}
            styles={{ root: classes.button }}
            disabled={!selection}
            onClick={onRemove}
          />
        </React.Fragment>
      )
    } else {
      return null
    }
  }
}

export default PersonalAccessTokenFormButtons
