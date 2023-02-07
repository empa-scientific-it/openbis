<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
version="1.0"> 
<xsl:output method="html" indent="yes" omit-xml-declaration="yes"/>
<xsl:template match="/">
		<p/>&#160;&#160;&#160;<b>Read #<xsl:value-of select="Summary/@Read"/></b>
		 <i><xsl:value-of select="Summary/@ReadType"/></i>
	<table class="DataTable" >
	 <tr>
      <th >Lane</th> <th>Tiles</th> <th>Clu.Dens. (#/mm<sup>2</sup>)</th>
      <th>% PF Clusters </th> <th>Clusters PF (#/mm<sup>2</sup>)</th> <th>% Phas./Preph.</th>
		 <th>Cycles<br/>Err Rated</th> <th>% Aligned</th> <th>% Error Rate</th> 
		<th>% Error Rate<br/> 35 cycle</th > <th>% Error Rate<br/> 75 cycle</th > <th>% Error Rate<br/> 100 cycle</th > <th>1<sup>st</sup> Cycle Int</th>
      <th>% Intensity<br/> Cycle 20</th>
    </tr>
    <xsl:variable name="densityRatio" select="Summary/@densityRatio"/>
      <xsl:for-each select="Summary/Lane">
					<tr>
         <td><xsl:value-of select="@key"/></td>         
         <td><xsl:value-of select="@TileCount"/></td>         
         <td><xsl:value-of select="format-number(@ClustersRaw * $densityRatio div 1000,'#')"/>K +/- 
				<xsl:value-of select="format-number(@ClustersRawSD * $densityRatio div 1000,'#.0')"/>K</td>         
         <td><xsl:value-of select="@PrcPFClusters"/> +/- 
				<xsl:value-of select="@PrcPFClustersSD"/></td>         
         <td><xsl:value-of select="format-number(@ClustersPF* $densityRatio div 1000,'#.0')"/>K +/- 
				<xsl:value-of select="format-number(@ClustersPFSD* $densityRatio div 1000,'#.00')"/>K</td>         
         <td><xsl:value-of select="@Phasing"/> /
				<xsl:value-of select="@Prephasing"/></td>
         <td><xsl:value-of select="@CalledCyclesMin"/>
            <xsl:if test="@CalledCyclesMax &gt; @CalledCyclesMin">-<xsl:value-of select="@CalledCyclesMax"/></xsl:if></td>
         <td><xsl:value-of select="@PrcAlign"/> +/-
				<xsl:value-of select="@PrcAlignSD"/></td>         
         <td><xsl:value-of select="@ErrRatePhiX"/> +/-
           <xsl:value-of select="@ErrRatePhiXSD"/></td>
         <td><xsl:value-of select="@ErrRate35"/> +/-
           <xsl:value-of select="@ErrRate35SD"/></td>
         <td><xsl:value-of select="@ErrRate75"/> +/-
           <xsl:value-of select="@ErrRate75SD"/></td>
         <td><xsl:value-of select="@ErrRate100"/> +/-
           <xsl:value-of select="@ErrRate100SD"/></td>
         <td><xsl:value-of select="format-number(@FirstCycleIntPF,'#')"/> +/-
             <xsl:value-of select="format-number(@FirstCycleIntPFSD,'#.0')"/></td>
         <td><xsl:value-of select="format-number(@PrcIntensityAfter20CyclesPF,'#.0')"/> +/-
             <xsl:value-of select="format-number(@PrcIntensityAfter20CyclesPFSD,'#.00')"/></td>
					</tr>
      </xsl:for-each>
  </table >
</xsl:template>
</xsl:stylesheet>
