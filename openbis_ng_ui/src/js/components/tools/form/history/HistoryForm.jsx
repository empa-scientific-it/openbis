import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import HistoryGrid from '@src/js/components/tools/common/HistoryGrid.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class HistoryForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {
      loaded: false,
      selection: null
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      const rows = await this.loadEvents(this.props.object.id)
      this.setState(() => ({
        rows,
        loaded: true
      }))
    } catch (error) {
      store.dispatch(actions.errorChange(error))
    }
  }

  async loadEvents(eventType) {
    const criteria = new openbis.EventSearchCriteria()
    criteria.withEventType().thatEquals(eventType)

    const fo = new openbis.EventFetchOptions()
    fo.withRegistrator()

    const result = await openbis.searchEvents(criteria, fo)

    return result.objects.map(event => ({
      id: _.get(event, 'id'),
      eventType: FormUtil.createField({ value: _.get(event, 'eventType') }),
      entityType: FormUtil.createField({ value: _.get(event, 'entityType') }),
      entitySpace: FormUtil.createField({
        value: _.get(event, 'entitySpace')
      }),
      entityProject: FormUtil.createField({
        value: _.get(event, 'entityProject')
      }),
      entityRegistrator: FormUtil.createField({
        value: _.get(event, 'entityRegistrator')
      }),
      entityRegistrationDate: FormUtil.createField({
        value: _.get(event, 'entityRegistrationDate')
      }),
      identifier: FormUtil.createField({
        value: _.get(event, 'identifier')
      }),
      description: FormUtil.createField({
        value: _.get(event, 'description')
      }),
      reason: FormUtil.createField({
        value: _.get(event, 'reason')
      }),
      content: FormUtil.createField({
        value: _.get(event, 'content'),
        visible: false
      }),
      registrator: FormUtil.createField({
        value: _.get(event, 'registrator.userId')
      }),
      registrationDate: FormUtil.createField({
        value: _.get(event, 'registrationDate')
      })
    }))
  }

  handleRowChange(rowId, change) {
    const rows = this.state.rows
    this.setState(state => {
      const index = rows.findIndex(row => row.id === rowId)
      if (index !== -1) {
        const row = rows[index]
        const newRows = Array.from(rows)
        newRows[index] = {
          ...row,
          ...change
        }
        return {
          ...state,
          rows: newRows
        }
      }
    })
  }

  render() {
    logger.log(logger.DEBUG, 'HistoryForm.render')

    const { id } = this.props.object
    const { rows } = this.state

    return (
      <GridContainer>
        <HistoryGrid
          eventType={id}
          rows={rows}
          onRowChange={this.handleRowChange}
        />
      </GridContainer>
    )
  }
}

export default _.flow(connect(), withStyles(styles))(HistoryForm)
