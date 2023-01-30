<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
                xmlns:lxslt="http://xml.apache.org/xslt">
<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<xsl:value-of select="/Status/Configuration/TilesPerLane"/>
  </xsl:template>
</xsl:stylesheet>
