<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
version="1.0"> 
<xsl:output method="html" indent="yes" omit-xml-declaration="yes"/>
<xsl:template match="/">
	<table class="DataTable" >
	 <tr>
     <th>Control</th>
     <th >Lane 1</th><th >Lane 2</th><th >Lane 3</th><th >Lane 4</th>
     <th >Lane 5</th><th >Lane 6</th><th >Lane 7</th><th >Lane 8</th>
    </tr>
    <xsl:for-each select="Controls/ctl"><tr>
     <td><xsl:value-of select="@Key"/></td>         
     <td><xsl:value-of select="@L1"/></td>         
     <td><xsl:value-of select="@L2"/></td>         
     <td><xsl:value-of select="@L3"/></td>         
     <td><xsl:value-of select="@L4"/></td>         
     <td><xsl:value-of select="@L5"/></td>         
     <td><xsl:value-of select="@L6"/></td>         
     <td><xsl:value-of select="@L7"/></td>         
     <td><xsl:value-of select="@L8"/></td>
    </tr></xsl:for-each>
  </table >
</xsl:template>
</xsl:stylesheet>
