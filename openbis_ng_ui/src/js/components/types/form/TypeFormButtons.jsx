import React from 'react'
import { connect } from 'react-redux'
import PageMode from '@src/js/components/common/page/PageMode.js'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import TypeFormControllerStrategies from '@src/js/components/types/form/TypeFormControllerStrategies.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import users from '@src/js/common/consts/users.js'
import messages from '@src/js/common/messages.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class TypeFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, object } = this.props

    const strategy = new TypeFormControllerStrategies().getStrategy(object.type)
    const existing = object.type === strategy.getExistingObjectType()

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={existing ? onCancel : null}
        renderAdditionalButtons={params => this.renderAdditionalButtons(params)}
      />
    )
  }

  renderAdditionalButtons({ mode, classes }) {
    if (mode === PageMode.EDIT) {
      const { onAddSection, onAddProperty, onRemove } = this.props

      return (
        <React.Fragment>
          <Button
            name='addSection'
            label={messages.get(messages.ADD_SECTION)}
            styles={{ root: classes.button }}
            onClick={onAddSection}
          />
          <Button
            name='addProperty'
            label={messages.get(messages.ADD_PROPERTY)}
            styles={{ root: classes.button }}
            disabled={!this.isSectionOrPropertySelected()}
            onClick={onAddProperty}
          />
          <Button
            name='remove'
            label={messages.get(messages.REMOVE)}
            styles={{ root: classes.button }}
            disabled={
              !(
                this.isNonSystemInternalSectionSelected() ||
                this.isNonSystemInternalPropertySelected() ||
                this.isSystemUser()
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

  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === TypeFormSelectionType.PROPERTY ||
        selection.type === TypeFormSelectionType.SECTION)
    )
  }

  isNonSystemInternalSectionSelected() {
    const { selection, sections, properties } = this.props

    if (selection && selection.type === TypeFormSelectionType.SECTION) {
      const section = sections.find(
        section => section.id === selection.params.id
      )
      return !section.properties.some(propertyId => {
        const property = properties.find(property => property.id === propertyId)
        return property.assignmentInternal.value
      })
    } else {
      return false
    }
  }

  isNonSystemInternalPropertySelected() {
    const { selection, properties } = this.props

    if (selection && selection.type === TypeFormSelectionType.PROPERTY) {
      const property = properties.find(
        property => property.id === selection.params.id
      )
      return !property.assignmentInternal.value
    } else {
      return false
    }
  }

  isSystemUser() {
    return this.props.session && this.props.session.userName === users.SYSTEM
  }
}

export default connect(mapStateToProps)(TypeFormButtons)
