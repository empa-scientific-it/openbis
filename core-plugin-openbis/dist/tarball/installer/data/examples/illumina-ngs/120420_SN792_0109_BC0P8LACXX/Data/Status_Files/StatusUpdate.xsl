<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
                xmlns:lxslt="http://xml.apache.org/xslt">
<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<table class="StatusTable"><tr>
			<th>Total:</th><td id="NumCycles"><xsl:value-of select="/Status/NumCycles"/></td>
			<th>Extracted:</th><td id="ImgCycle"><xsl:value-of select="/Status/ImgCycle"/></td>
			<th>Called:</th><td id="CallCycle"><xsl:value-of select="/Status/CallCycle"/></td>
			<th>Scored:</th><td id="ScoreCycle"><xsl:value-of select="/Status/ScoreCycle"/></td>
			<th>Copied:</th><td id="CopyCycle"><xsl:value-of select="/Status/CopyCycle"/></td>
		</tr></table>
  </xsl:template>
</xsl:stylesheet>
