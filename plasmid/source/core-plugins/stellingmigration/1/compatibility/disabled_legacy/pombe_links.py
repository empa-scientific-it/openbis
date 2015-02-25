""" 
Managed Property Script for handling Readouts.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_NAME = "name"
ATR_COMMENT = "comment"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
NAME_LABEL = "name"
COMMENT_LABEL = "comment"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(pombes_list, comment_list):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    pombePath= "/YLAB-TEST/" + pombes_list
    permId =entityInformationProvider().getSamplePermId(SPACE, pombes_list)
    if not permId:
        permId = pombes_list
    name  = entityInformationProvider().getSamplePropertyValue(permId, 'NAME')
    print "the name is", name
    sampleLink = elementFactory().createSampleLink(permId)
    
    sampleLink.addAttribute(ATR_CODE, pombes_list)
    sampleLink.addAttribute(ATR_NAME, name) 
    sampleLink.addAttribute(ATR_COMMENT, comment_list)
    
    return sampleLink    


"""
Example input:

FRC1, FRC2, FRC3, FRC4
"""


def showRawValueInForms():
    return False
 
def batchColumnNames():
    return [CODE_LABEL, COMMENT_LABEL]
 
def updateFromRegistrationForm(bindings):
    elements = []
    for item in bindings:
        pombes_list = item.get('CODE')
        comment_list = item.get('COMMENT')
    if pombes_list:
          sampleLink = _createSampleLink(pombes_list, comment_list)
          elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)




def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(NAME_LABEL) 
    tableBuilder.addHeader(COMMENT_LABEL) 

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for pombe in elements:
        pombes_list = pombe.getAttribute(ATR_CODE, "")
        name = pombe.getAttribute(ATR_NAME,"") 
        comment_list=pombe.getAttribute(ATR_COMMENT, "")  
        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, pombe, pombes_list)
        row.setCell(CODE_LABEL, pombes_list)
        row.setCell(NAME_LABEL, name)
        row.setCell(COMMENT_LABEL, comment_list)
 
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new pombe,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new pombe relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRSP')\
                            .setDescription('Code of pombe, e.g. "FRSP1"'),
        inputWidgetFactory().createMultilineTextInputField(COMMENT_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Comment')       
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected pombe relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of pombe sample, e.g. "FRSP1"'),
       inputWidgetFactory().createMultilineTextInputField(COMMENT_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Comments'),       
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(COMMENT_LABEL, COMMENT_LABEL)

  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete the selected pombe relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new pombe element with values from input fields
           and add it to existing elements.
        """
        pombes_list = action.getInputValue(CODE_LABEL)
        comment_list = action.getInputValue(COMMENT_LABEL) 
        sampleLink = _createSampleLink(pombes_list, comment_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the pombe element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        pombes_list = action.getInputValue(CODE_LABEL)
        comment_list = action.getInputValue(COMMENT_LABEL)
        

        sampleLink = _createSampleLink(pombes_list, comment_list)
        
        

        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete pombes that correspond to selected rows.
           NOTE: As many rows can be deleted at once it is easier to delete them in reversed order.
        """
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()       
        for rowId in rowIds:
            elements.pop(rowId)      
    else:
        raise ValidationException('action not supported')
      
    """Update value of the managed property to XML string created from modified list of elements."""
    property.value = converter.convertToString(elements)  

def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        pombes = input.split(',')
        for pombe in pombes:
            (code, comment) = _extractCodeAndComment(pombe)
            sampleLink = _createSampleLink(code, comment)
            elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)
    
def _extractCodeAndComment(pombe):
    codeAndComment = pombe.split(':')
    if (len(codeAndComment) == 2):
        return (codeAndComment[0].strip(), codeAndComment[1].strip())
    else:
        return (codeAndComment[0].strip(), "n.a.") 