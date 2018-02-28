<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:exsl="http://exslt.org/common"
                version='1.0'>

<!-- Part of the KDL Relational PC package (see LICENSE for
     copyright and license information).
-->

<!-- ====================================================== -->
<!-- Style sheet for common formatting elements for RPC     -->
<!-- documentation                                          -->
<!-- ====================================================== -->

<!-- DocBook XSL Parameters -->

<xsl:param name="use.extensions" select="1"/>
<xsl:param name="bibliography.numbered" select="0"/>
<xsl:param name="default.table.width" select="'100%'"></xsl:param>
<xsl:param name="xref.with.number.and.title" select="0"></xsl:param>
<xsl:param name="formal.procedures" select="1"></xsl:param>

<!--
<xsl:param name="shade.verbatim" select="1"></xsl:param>
  Deprecated with version 1.67; now using CSS for this shading
  <xsl:attribute-set name="shade.verbatim.style">
    <xsl:attribute name="border">0</xsl:attribute>
    <xsl:attribute name="bgcolor">#E0E0E0</xsl:attribute>
  </xsl:attribute-set>
-->

<xsl:param name="admon.graphics" select="1"></xsl:param>
<xsl:param name="admon.graphics.extension" select="'.png'"></xsl:param>
<xsl:param name="admon.graphics.path">images/</xsl:param>
<xsl:param name="admon.textlabel" select="0"></xsl:param>
<xsl:param name="use.role.for.mediaobject" select="1"></xsl:param>

<!-- enable specialized indexes -->
<xsl:param name="index.on.type">1</xsl:param>

<!-- Default position is before the titled element unless
     specified otherwise here -->
<xsl:param name="formal.title.placement">
figure after
</xsl:param>

<!-- comment out next three params if not processing glossary -->

<xsl:param name="glossary.collection"
           select="'../../ProximityGlossary.xml'"/>
<xsl:param name="glossentry.show.acronym" select="'yes'"></xsl:param>
<xsl:param name="glossterm.auto.link" select="1"></xsl:param>
<!--
-->

<!--
  <xsl:param name="glossary.as.blocks" select="1"></xsl:param>
-->

<xsl:param name="bibliography.collection"
           select="'../RPCBibliography.xml'"/>

<!-- ====================================================== -->
<!-- Customization layer -->
<!-- Generated text -->

<xsl:param name="rpccommon.l10n.xml" select="document('')"/> 
<l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0"> 
  <l:l10n language="en"> 
     <l:gentext key="Example" text=""/>
     <l:gentext key="example" text=""/>
     <l:gentext key="Procedure" text="Procedure"/>
     <l:gentext key="procedure" text="procedure"/>
<!--
     <l:gentext key="ListofProcedures" text="List of Exercises"/>
     <l:gentext key="listofprocedures" text="List of Exercises"/>
-->
     <l:context name="title">
        <l:template name="example" text="%t"/>
        <l:template name="formalpara" text="%t"/>
        <l:template name="procedure" text="%t:"/>
        <l:template name="procedure.formal" text="%t:"/>
     </l:context>

     <!-- Some comments about xref styles:
          @ The order of alternative matters; the default must be
            listed last. 
          @ Make sure that you update BOTH the rpc-fo.xsl and
            rpc-html.xsl stylesheets when generated text should be
            different for printed and HTML versions.
          @ Only include gentext items in rpc-fo.xsl and rpc-html.xsl
            when they differ for HTML and PDF, otherwise  place them
            in rpc-common.xsl.
          @ Place default templates (no xrefstyle) in rpc-common.xsl.
     -->

     <l:context name="xref">
     <!-- These defns are used for crossrefs to non-numbered elements -->
        <l:template name="appendix" text="%t"/>
        <l:template name="chapter" text="%t"/>
        <l:template name="example" text="%t"/>
        <l:template name="figure" text="Figure&#160;%n"/>
        <l:template name="procedure" text="%t"/>
        <l:template name="bridgehead" text="%t"/>
        <l:template name="refsection" text="&#8220;%t&#8221;"/>
        <l:template name="refsect1" text="&#8220;%t&#8221;"/>
        <l:template name="refsect2" text="&#8220;%t&#8221;"/>
        <l:template name="refsect3" text="&#8220;%t&#8221;"/>
        <l:template name="sect1" text="&#8220;%t&#8221;"/>
        <l:template name="sect2" text="&#8220;%t&#8221;"/>
        <l:template name="sect3" text="&#8220;%t&#8221;"/>
        <l:template name="sect4" text="&#8220;%t&#8221;"/>
        <l:template name="sect5" text="&#8220;%t&#8221;"/>
        <l:template name="section" text="&#8220;%t&#8221;"/>
        <l:template name="simplesect" text="&#8220;%t&#8221;"/>
        <l:template name="page.citation" text=" (p.&#160;%p)"/>
     </l:context>
     <l:context name="xref-number">
     <!-- These defns are used when xref.with.number.and.title = 0 -->
        <!-- see rpc-fo.xsl and rpc-html.xsl -->
        <l:template name="appendix" text="Appendix&#160;%n"/>
        <l:template name="chapter" text="Chapter&#160;%n"/>
        <l:template name="bridgehead" text="%t"/>
        <l:template name="example" text="%t"/>
        <l:template name="figure" text="Figure&#160;%n"/>
        <l:template name="procedure" text="%t"/>
        <l:template name="section" text="&#8220;%t&#8221;"/>
     </l:context>
     <l:context name="xref-number-and-title">
     <!-- These defns are used when xref.with.number.and.title = 1 -->
        <!-- see rpc-fo.xsl and rpc-html.xsl -->
        <l:template name="appendix" text="Appendix&#160;%n, %t"/>
        <l:template name="chapter" text="Chapter&#160;%n, %t"/>
        <l:template name="bridgehead" text="%t"/>
        <l:template name="example" text="%t"/>
        <l:template name="figure" text="Figure&#160;%n"/>
        <l:template name="procedure" text="%n&#160;%t"/>
        <l:template name="sect1" text="&#8220;%t&#8221;"/>
        <l:template name="sect2" text="&#8220;%t&#8221;"/>
        <l:template name="sect3" text="&#8220;%t&#8221;"/>
        <l:template name="section" text="&#8220;%t&#8221;"/>
     </l:context>
  </l:l10n>
</l:i18n>

<!-- ====================================================== -->
<!-- Entity handling -->

<xsl:template match="symbol[@role = 'Symbol']">
  <fo:inline font-family="Symbol">
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>

<!-- ====================================================== -->
<!-- Other customizations -->

<!-- Remove (empty) title pages between cover and ToC -->

<xsl:template name="book.titlepage.separator">
</xsl:template>

<!-- Change style of inline elements -->

<!-- The inline.sansseq template that uses this attribute set (above)
     is defined in prc-fo.xsl and rpc-html.xsl -->

<xsl:template match="userinput">
  <xsl:call-template name="inline.boldmonoseq"/>
</xsl:template>

<xsl:template match="varname">
  <xsl:call-template name="inline.italicmonoseq"/>
</xsl:template>

<xsl:template match="keycap">
  <xsl:call-template name="inline.charseq"/>
</xsl:template>

<xsl:template match="guibutton">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guiicon">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guilabel">
<!-- <xsl:call-template name="inline.charseq"/> -->
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenuitem">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guisubmenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="application">
  <xsl:call-template name="inline.sansseq"/>
</xsl:template>

<xsl:template match="token">
  <xsl:choose>
    <xsl:when test="@role='annot'">
      <xsl:call-template name="inline.monoseq"/>
    </xsl:when>
    <xsl:when test="@role='attr'">
      <xsl:choose>
        <xsl:when test="ancestor::footnote">
          <xsl:call-template name="inline.sansseq.tiny"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="inline.sansseq"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="@role='class'">
      <xsl:call-template name="inline.italicseq"/>
    </xsl:when>
    <!-- columns in an NST (or other database) table -->
    <xsl:when test="@role='column'">
      <xsl:call-template name="inline.monoseq"/>
    </xsl:when>
    <xsl:when test="@role='condition'">
      <xsl:call-template name="inline.sansseq"/>
    </xsl:when>
    <xsl:when test="@role='constraint'">
      <xsl:call-template name="inline.sansseq"/>
    </xsl:when>
    <xsl:when test="@role='container'">
      <xsl:call-template name="inline.charseq"/>
    </xsl:when>
    <xsl:when test="@role='edge'">
      <xsl:call-template name="inline.sansseq"/>
    </xsl:when>
    <xsl:when test="@role='entity'">
      <xsl:call-template name="inline.sansseq"/>
    </xsl:when>
    <xsl:when test="@role='link'">
      <xsl:call-template name="inline.sansseq"/>
    </xsl:when>
    <xsl:when test="@role='object'">
      <xsl:call-template name="inline.charseq"/>
    </xsl:when>
    <xsl:when test="@role='subgraph'">
      <xsl:call-template name="inline.charseq"/>
    </xsl:when>
    <!-- refers to database tables, including NSTs -->
    <xsl:when test="@role='table'">
      <xsl:call-template name="inline.charseq"/>
    </xsl:when>
    <xsl:when test="@role='vertex'">
      <xsl:call-template name="inline.italicseq"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="inline.charseq"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ====================================================== -->
<!-- Special formatting -->

<!-- Editorial comments, optional hyphenation -->

<xsl:template match="hyphenate.charseq">
  <xsl:attribute name="hyphenate">true</xsl:attribute>
</xsl:template>

<xsl:template match="phrase">
  <xsl:choose>
    <xsl:when test="@role='hyphenate'">
      <xsl:attribute name="hyphenate">true</xsl:attribute>
        <xsl:call-template name="inline.charseq"/>
    </xsl:when>
    <xsl:when test="@role='edcomment'">
      <!-- comment out the following line to remove edcomments from
           the generated doc -->
      <xsl:call-template name="inline.edcomment"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="inline.charseq"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  
<!-- End stylesheet -->

</xsl:stylesheet>
