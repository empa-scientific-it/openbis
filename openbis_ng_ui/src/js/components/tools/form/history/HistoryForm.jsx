import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import HistoryGrid from '@src/js/components/tools/common/HistoryGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class HistoryForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}
  }

  render() {
    logger.log(logger.DEBUG, 'HistoryForm.render')

    const { id } = this.props.object

    return (
      <HistoryGrid
        id={ids.HISTORY}
        eventType={id}
        rows={[]}
        //onSelectedRowChange={}
        //selectedRowId={}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(HistoryForm)
