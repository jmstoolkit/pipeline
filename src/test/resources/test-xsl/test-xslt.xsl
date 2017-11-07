<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes"/>
<xsl:template match="/">
<xml2>
<id2><xsl:value-of select="xml1/id1"/></id2>
<name2><xsl:value-of select="xml1/name1"/></name2>
</xml2>
</xsl:template>
</xsl:stylesheet>
