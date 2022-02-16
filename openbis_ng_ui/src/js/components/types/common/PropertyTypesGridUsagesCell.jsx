import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Collapse from '@material-ui/core/Collapse'
import Link from '@material-ui/core/Link'
import TypeLink from '@src/js/components/common/link/TypeLink.jsx'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  usages: {
    padding: 0,
    margin: 0,
    marginTop: theme.spacing(1)
  },
  usage: {
    listStyle: 'none',
    margin: 0,
    padding: 0
  }
})

class PropertyTypesGridUsagesCell extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      visible: false
    }
    this.handleVisibilityChange = this.handleVisibilityChange.bind(this)
  }

  handleVisibilityChange() {
    this.setState(state => ({
      visible: !state.visible
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'PropertyTypesGridUsagesCell.render')

    const { value } = this.props
    const { visible } = this.state

    if (value) {
      return (
        <div>
          <div>
            {value.count} (
            <Link
              onClick={() => {
                this.handleVisibilityChange()
              }}
            >
              {visible
                ? messages.get(messages.HIDE)
                : messages.get(messages.SHOW)}
            </Link>
            )
          </div>
          <Collapse in={visible} mountOnEnter={true} unmountOnExit={true}>
            <div>
              {this.renderUsages(
                openbis.EntityKind.EXPERIMENT,
                messages.get(messages.COLLECTION_TYPES),
                value.experimentTypes
              )}
              {this.renderUsages(
                openbis.EntityKind.SAMPLE,
                messages.get(messages.OBJECT_TYPES),
                value.sampleTypes
              )}
              {this.renderUsages(
                openbis.EntityKind.DATA_SET,
                messages.get(messages.DATA_SET_TYPES),
                value.dataSetTypes
              )}
              {this.renderUsages(
                openbis.EntityKind.MATERIAL,
                messages.get(messages.MATERIAL_TYPES),
                value.materialTypes
              )}
            </div>
          </Collapse>
        </div>
      )
    } else {
      return 0
    }
  }

  renderUsages(usageKind, usagesHeader, usagesList) {
    if (usagesList.length === 0) {
      return null
    }

    const { classes } = this.props

    return (
      <ul className={classes.usages}>
        {usagesHeader}:
        {usagesList.map(usage => (
          <li key={usage} className={classes.usage}>
            <TypeLink typeKind={usageKind} typeCode={usage} />
          </li>
        ))}
      </ul>
    )
  }
}

export default withStyles(styles)(PropertyTypesGridUsagesCell)
