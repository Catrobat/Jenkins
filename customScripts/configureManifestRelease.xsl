<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:android='http://schemas.android.com/apk/res/android' 
    > 
    <xsl:param name="CODE"/> 
    <xsl:output indent="no"/> 
    <!-- Supplies a newline after the XML header. It's cosmetic, but 
something to include in each script. --> 
    <xsl:template match="/"> 
        <xsl:text> 
</xsl:text> 
        <xsl:apply-templates select="@*|node()"/> 
    </xsl:template> 
    <!-- This is the part that makes the desired change. 
         One template like this for each change you want to make. -->
  <xsl:template match="application[@android:debuggable='true']">
    <xsl:copy>
      <xsl:copy-of select="@*[name(.)!='android:debuggable']" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

<xsl:template match="/manifest/@android:versionName">
        <xsl:attribute name="android:versionName"><xsl:value-of select="/manifest/@android:versionName"/>-<xsl:value-of select="$CODE"/>-release</xsl:attribute>
    </xsl:template>

    <xsl:template match="/manifest/@android:versionCode"> 
        <xsl:attribute name="android:versionCode"><xsl:value-of  
select="$CODE"/></xsl:attribute> 
    </xsl:template>

    <!-- Identity transform by default. Generally, you'll put this in 
each script you write, 
         if you're just making changes. It copies everything unchanged 
that doesn't have its own 
         template. --> 
    <xsl:template match="@*|node()"> 
        <xsl:copy> 
            <xsl:apply-templates select="@*|node()"/> 
        </xsl:copy> 
    </xsl:template> 
</xsl:stylesheet> 
