<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
                xmlns:lxslt="http://xml.apache.org/xslt">
<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
                  <table class="InfoTable">
                     <tr style="text-align: center;">Run Description<p/></tr>
                     <tr><th>Software:</th><td><xsl:value-of select="/Status/Software"/></td></tr>
                     <tr><th>Run name:</th><td><xsl:value-of select="/Status/RunName"/></td></tr>
                     <tr><th>Cycles:</th><td><xsl:value-of select="/Status/NumCycles"/></td></tr>
                     <tr><th>Instrument name:</th><td><xsl:value-of select="/Status/InstrumentName"/></td></tr>
                     <tr><th>Run Started (Estimated):</th><td><xsl:value-of select="/Status/RunStarted"/></td></tr>
                     <tr><th>Input Directory:</th><td><xsl:value-of select="/Status/InputDir"/></td></tr>
                     <tr><th>Output Directory:</th><td><xsl:value-of select="/Status/OutputDir"/></td></tr>
                     <tr><p/></tr>
                  </table >
      <p/>
                  <table class="InfoTable">
                     <tr style="text-align: center;">Configuration Settings<p/></tr>
                     <tr><th><li type="square">Copy all run files:</li></th><td><xsl:value-of select="/Status/Configuration/CopyAllFiles"/></td></tr>
                     <tr><th><li type="square">Copy images:</li></th><td><xsl:value-of select="/Status/Configuration/CopyImages"/></td></tr>
                     <tr><th><li type="square">Delete image files:</li></th><td><xsl:value-of select="/Status/Configuration/DeleteImages"/></td></tr>
                     <tr><th><li type="square">Delete intensity files:</li></th><td><xsl:value-of select="/Status/Configuration/DeleteIntensity"/></td></tr>
                     <tr><th><li type="square">Run info file exists:</li></th><td><xsl:value-of select="/Status/Configuration/RunInfoExists"/></td></tr>
                     <tr><th><li type="square">Is this a paired end run:</li></th><td><xsl:value-of select="/Status/Configuration/IsPairedEndRun"/></td></tr>
                     <tr><th><li type="square">Total number of reads:</li></th><td><xsl:value-of select="/Status/Configuration/NumberOfReads"/></td></tr>
                     <tr><th><li type="square">Number of lanes:</li></th><td><xsl:value-of select="/Status/Configuration/NumberOfLanes"/></td></tr>
                     <tr><th><li type="square">Number of tiles per lane:</li></th><td><xsl:value-of select="/Status/Configuration/TilesPerLane"/></td></tr>
                     <tr><th><li type="square">Control lane:</li></th>
                        <td>
                           <xsl:if test="/Status/Configuration/ControlLane = 0"> None</xsl:if>
                           <xsl:if test="/Status/Configuration/ControlLane &gt; 0 ">
                              <xsl:value-of select="/Status/Configuration/ControlLane"/>
                           </xsl:if>
                        </td>
                     </tr>                     
                  </table>
  </xsl:template>
</xsl:stylesheet>
