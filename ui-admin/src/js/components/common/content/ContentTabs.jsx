import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import UnsavedChangesDialog from '@src/js/components/common/dialog/UnsavedChangesDialog.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  tabsRoot: {
    borderBottomStyle: 'solid',
    borderBottomWidth: '1px',
    borderBottomColor: theme.palette.border.primary,
    minHeight: '38px'
  },
  tabRoot: {
    textTransform: 'none',
    minHeight: '38px',
    maxWidth: 'unset'
  },
  iconRoot: {
    marginLeft: '6px'
  },
  tabLabel: {
    display: 'inline-flex',
    alignItems: 'center'
  }
})

class ContentTabs extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      tabToClose: null,
      unsavedChangesDialogOpen: false
    }
  }
  handleTabChange = (event, value) => {
    const tab = this.props.tabs[value]
    this.props.tabSelect(tab)
  }

  handleTabClose = (event, tab) => {
    if (tab.changed) {
      this.setState({
        tabToClose: tab,
        unsavedChangesDialogOpen: true
      })
    } else {
      this.props.tabClose(tab)
    }
    event.stopPropagation()
  }

  handleTabCloseConfirm = () => {
    const { tabToClose } = this.state
    if (tabToClose) {
      this.props.tabClose(tabToClose)
      this.setState({
        tabToClose: null,
        unsavedChangesDialogOpen: false
      })
    }
  }

  handleTabCloseCancel = () => {
    this.setState({
      tabToClose: null,
      unsavedChangesDialogOpen: false
    })
  }

  render() {
    logger.log(logger.DEBUG, 'ContentTabs.render')

    const { tabs, selectedTab, classes } = this.props

    let value = false

    if (selectedTab) {
      const selectedIndex = _.findIndex(tabs, selectedTab)
      if (selectedIndex !== -1) {
        value = selectedIndex
      }
    }

    const { unsavedChangesDialogOpen } = this.state

    return (
      <React.Fragment>
        <Tabs
          value={value}
          variant='scrollable'
          scrollButtons='on'
          onChange={this.handleTabChange}
          classes={{ root: classes.tabsRoot }}
        >
          {this.props.tabs.map(tab => (
            <Tab
              key={tab.id}
              label={this.renderLabel(tab)}
              classes={{
                root: classes.tabRoot
              }}
            />
          ))}
        </Tabs>
        <UnsavedChangesDialog
          open={unsavedChangesDialogOpen}
          onConfirm={this.handleTabCloseConfirm}
          onCancel={this.handleTabCloseCancel}
        />
      </React.Fragment>
    )
  }

  renderLabel(tab) {
    return (
      <span className={this.props.classes.tabLabel}>
        {this.props.renderTab(tab)}
        {this.renderIcon(tab)}
      </span>
    )
  }

  renderIcon(tab) {
    return (
      <CloseIcon
        onClick={event => this.handleTabClose(event, tab)}
        classes={{
          root: this.props.classes.iconRoot
        }}
        fontSize='small'
      />
    )
  }
}

export default withStyles(styles)(ContentTabs)
