import React from 'react'
import { connect } from 'react-redux'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import users from '@src/js/common/consts/users.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class PropertyTypeFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'PropertyTypeFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, propertyType } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={this.isInternal() && !this.isSystemUser() ? null : onEdit}
        onSave={onSave}
        onCancel={propertyType.id ? onCancel : null}
      />
    )
  }

  isInternal() {
    return this.props.propertyType.internal.value
  }

  isSystemUser() {
    return this.props.session && this.props.session.userName === users.SYSTEM
  }
}

export default connect(mapStateToProps)(PropertyTypeFormButtons)
