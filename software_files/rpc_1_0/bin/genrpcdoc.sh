#!/bin/csh -f
# Part of the KDL Relational PC package (see LICENSE for copyright
# and license information).

# ********************************************************************
# This is the main document generation script for RPC.  Use this
# script to generate HTML and PDF versions of the RPC Documentation
#
# To use this script you must
#  - Set up the DocBook tools
#      - XSLT processor (used for processing files):  Saxon 6.5.3
#      - XML processor (ussed for validation):  Xalan
#      - XML parser:  Xerces
#      - catalog files for resolving paths to DTDs and stylesheets
#  - Install and configure related tools and libraries
#      - FOP (0.95)
#  - Define the directory locations below for your local installation
#
# --------------------------------------------------------------------
# Command line syntax:
# --------------------------------------------------------------------
#
#    genrpcdoc [options] <format>
#
# where parameters in <> are required and those in [] are optional.
# (Module names are required when generating a modular document; see below.)
#
# Examples:
#    Generate the HTML version of the RPC documentation:
#       genrpcdoc.sh html
#
#    Show the processing steps but do not generate the PDF version of
#    the RPC documentation:
#       genrpcdoc.sh --d pdf
#
# --------------------------------------------------------------------
# Options
# --------------------------------------------------------------------
# Multiple options are supported.
#
#    --help or --h       print usage instructions
#    --debug or --d      run in debug mode (no document generation)
#
# --------------------------------------------------------------------
# Formats
# --------------------------------------------------------------------
#    html                generate HTML
#    pdf                 generate PDF (includes FO generation)
#    v or validate       validate only; no other processing
#
# ********************************************************************

# --------------------------------------------------------------------
# Set the locations for source and code directories

set RPC_HOME="/Users/loiselle/work/causality" # Project home dir
set JAVA_LIBS="/usr/java"                     # Java libs
set SAXON_HOME="$JAVA_LIBS/saxon6_5_3"        # Saxon libs
#set XALAN_HOME="$JAVA_LIBS/xalan-j_2_5_2"    # Xalan libs
set XERCES_HOME="$JAVA_LIBS/xerces-2_9_0"     # Xerces libs (newer version)
set FOP_HOME="$JAVA_LIBS/fop-0.95"            # FOP 0.95 libs
set SGML_HOME="/usr/local/sgml"               # Catalog location
set DOCBOOK_XSL_HOME="$SGML_HOME/docbook-xsl-1.73.2" # DocBook XSL location

# --------------------------------------------------------------------
# Set global variables and default values

set nonomatch                     # used for DEBUG flag
set DEBUG=""                      # echos cmds instead of executing
set genfo="0"                     # records errors in FO generation
set gendoc="0"                    # records errors in PDF/HTML generation

set docdirname=""                 # directory that contains doc files
set docsrcfile=""                 # base file for the target
set docgendir="doc"               # directory for generated documentation
set htmlgendir="$docgendir/HTML"  # directory for generated HTML files
set docformat=""                  # HTML or PDF?
set validate="0"                  # default to not validate

set srcpath="$RPC_HOME/src/xml/doc"      # base path to doc source files
set htmlparams="base.dir=$htmlgendir/"    # provide default for command-line
                                         # params for HTML generation
set tocparams="book toc,title article nop"

set kdlcss="kdldoc.css"                  # CSS style sheet for HTML docs

# Following doesn't seem to work if set here; need to set both in rpc-fo.xsl
set foparams='draft.mode="no" fop1.extensions="1"'


# --------------------------------------------------------------------
# Identify options.  (Make sure any new options use a unique initial
# letter.)

while ($1 =~ -*)
   if (($1 =~ "--help") || ($1 =~ "--h")) then
      goto Help

   else if (($1 =~ "--debug") || ($1 =~ "--d")) then
      set DEBUG="echo"
      echo "Debugging run - no document generation"
      shift
      continue

   else
      echo "Unknown option: $1"
      goto Usage
   endif
end

if ($DEBUG != "") then
    echo "Parsed switches"
endif
   
# Set the name of the root XML file and the directory containing
# the source file(s).

set basedoc="RPC_Main"
set docdirname="rpc"
set docsrcpath="$RPC_HOME/src/xml/doc/$docdirname"
echo "Generating RPC documentation"

if ($DEBUG != "") then
    echo "Parsed document identifier:" $basedoc
endif

# --------------------------------------------------------------------
# Identify output format

if ($#argv != 1) then
     echo "Wrong number of arguments"
     goto Usage
else
  if (($1 == "validate") || ($1 == "v")) then
     set docformat="Validate"
     set validate="1"
  else if ($1 == "html") then
     set docformat="HTML"
     set stylesheet="rpc-html.xsl"
  else if ($1 == "pdf") then
     set docformat="PDF"
     set stylesheet="rpc-fo.xsl"
  else
     echo "Unknown format"
     goto Usage
  endif
endif

# --------------------------------------------------------------------
# Make sure we specified a legal docformat

if (($docformat == "") && ($validate == "0")) then
   echo "You must specify an output format or validate the document"
   goto Usage
endif

# Define filenames

set docsrcfile = $basedoc.xml
set docfo = $basedoc.fo
set docpdf = $basedoc.pdf

# Print current settings
echo "Output format: $docformat"

# Print more info if debugging
if ($DEBUG != "") then
    echo "docsrcpath: $docsrcpath"
    echo "docfo: $docfo"
    echo "docsrcfile: $docsrcfile"
    echo "docpdf: $docpdf"
endif

# Create the directory if it doesn't exist
if (! -d $htmlgendir) then
   mkdir $htmlgendir
   if ($DEBUG != "") then
      echo "Creating directory $htmlgendir"
   endif
endif

# --------------------------------------------------------------------
# Define paths to required libraries
# (Note that CLASSPATH is redefined here and does not use values from
# .bash_profile)

# Classpath for FOP 0.20.5 (currently not used)
#set FOP_CLASSPATH=$FOP_HOME/build/fop.jar:$FOP_HOME/lib/batik.jar:$FOP_HOME/lib/xalan-2.4.1.jar:$JAVA_LIBS/jimi/JimiProClasses.jar:$FOP_HOME/lib/avalon-framework-cvs-20020806.jar

# Classpath for FOP 0.95
set FOP_CLASSPATH=$FOP_HOME/build/fop.jar:$FOP_HOME/build/fop-sandbox.jar:$FOP_HOME/build/fop-hyph.jar:$FOP_HOME/lib/avalon-framework-4.2.0.jar:$FOP_HOME/lib/batik-all-1.7.jar:$FOP_HOME/lib/commons-io-1.3.1.jar:$FOP_HOME/lib/commons-logging-1.0.4.jar:$FOP_HOME/lib/serializer-2.7.0.jar:$FOP_HOME/lib/xalan-2.7.0.jar:$FOP_HOME/lib/xercesImpl-2.7.1.jar:$FOP_HOME/lib/xml-apis-1.3.04.jar:$FOP_HOME/lib/xml-apis-ext-1.3.04.jar:$FOP_HOME/lib/xmlgraphics-commons-1.3.1.jar

# Classpath for validation:
set VALCLASSPATH="$XERCES_HOME/xercesSamples.jar:$XERCES_HOME/xercesImpl.jar"

# Classpath for XSLT processing:
set CLASSPATH="{$FOP_CLASSPATH}:$SAXON_HOME/saxon.jar:$DOCBOOK_XSL_HOME/extensions/saxon65.jar:$XERCES_HOME/xercesImpl.jar:$JAVA_LIBS/resolver-1.0.jar:$SGML_HOME"

if ($DEBUG != "") then
    echo "CLASSPATH: $CLASSPATH"
endif

# --------------------------------------------------------------------
# Define some shortcuts for long commands used more than once
#
# Parameters
#   See also the discussion of these tools in Bob Stayton's excellent
# "DocBook XSL: The Complete Guide" (available at
# http://www.sagehill.net/docbookxsl/index.html); this document is
# essential for setting up a DocBook system.
#   
# Java parameters
#
#   Use the Xerces parser instead of the built-in Saxon parser:
#
#      -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=
#         org.apache.xerces.parsers.XIncludeParserConfiguration
#      -Djavax.xml.transform.TransformerFactory=
#         com.icl.saxon.TransformerFactoryImpl
#
#   Enable Xinclude processing
#
#      -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=
#         org.apache.xerces.parsers.XIncludeParserConfiguration
#
# Parameters used by the Saxon XSLT processor
#
# Command line options
#  -x <classname>  -  Use specified SAX parser for source file
#  -y <classname>  -  Use specified SAX parser for stylesheet file
#  -r <classname>  -  Use the specified URIResolver to process all URIs
#  -u  -  Indicates that the names of the source document and the style
#         document are URLs
#  -w0 -  Indicates the policy for handling recoverable errors in the
#         stylesheet: w0 means recover silently
#  -o <filename>   -  Send output to named file
#

# ------------
# generatehtml
# ------------

set generatehtml='java\
       -Xmx512M\
       -classpath $CLASSPATH\
       -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl\
       -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl\
       -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration\
       com.icl.saxon.StyleSheet\
       -x org.apache.xml.resolver.tools.ResolvingXMLReader\
       -y org.apache.xml.resolver.tools.ResolvingXMLReader\
       -r org.apache.xml.resolver.tools.CatalogResolver\
       -u -w0\
       $docsrcpath/$docsrcfile\
       $srcpath/stylesheets/$stylesheet\
       $htmlparams\
       generate.toc="$tocparams"\
       rpc.destination="$htmlgendir"'

# ----------
# generatefo
# ----------

set generatefo='java\
       -classpath $CLASSPATH\
       -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl\
       -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl\
       -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration\
       com.icl.saxon.StyleSheet\
       -x org.apache.xml.resolver.tools.ResolvingXMLReader\
       -y org.apache.xml.resolver.tools.ResolvingXMLReader\
       -r org.apache.xml.resolver.tools.CatalogResolver\
       -u -w0\
       -o $docgendir/$docfo\
       $docsrcpath/$docsrcfile\
       $srcpath/stylesheets/$stylesheet\
       $foparams\
       generate.toc="$tocparams"'

# -----------
# generatepdf
# -----------

# FOP 0.20.5
#set generatepdf='java\
#      -Xmx512M\
#      -classpath $CLASSPATH\
#      org.apache.fop.apps.Fop\
#      -fo $docfo\
#      -pdf $docpdf'

# Left in for testing new versions in the future
# FOP 0.95 (Java command)
set generatepdf='java\
     -Xmx512M\
     -classpath $CLASSPATH\
     org.apache.fop.cli.Main\
     -fo $docgendir/$docfo\
     -pdf $docgendir/$docpdf'

# -----------
# validatexml
# -----------

set validatexml='java\
       -classpath $VALCLASSPATH\
       sax.Counter\
       -v $docsrcfile'

# --------------------------------------------------------------------
# Generate document

switch ($docformat)
  case "HTML":
    echo "Generating HTML..."
    $DEBUG eval $generatehtml
    set gendoc = $status
    breaksw
  case "FO":
    echo "Generating $docfo..."
    $DEBUG eval $generatefo
    set genfo = $status
    breaksw
  case "PDF"
    echo "Generating $docfo..."
    $DEBUG eval $generatefo
    set genfo = $status
    echo "Generating $docpdf..."
    echo "Using FO file $docfo"
    $DEBUG eval $generatepdf
    set gendoc = $status
    breaksw
  case "Validate"
    echo "Validating XML..."
    $DEBUG eval $validatexml
    # If we're only validating, we're done
    goto Done
endsw

# --------------------------------------------------------------------
# Report a warning if errors occurred

if ($genfo != 0) then
  echo " "
  echo "*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
  echo " Error in XSL-FO generation - examine trace"
  echo "*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
  echo " "
endif
if ($gendoc != 0) then
  echo " "
  echo "*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
  echo " Errors in document generation - examine trace"
  echo "*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
  echo " "
endif

# --------------------------------------------------------------------
# Copy CSS stylesheet to directory containing generated HTML files

if ($docformat == "HTML") then
   $DEBUG cp $RPC_HOME/src/xml/doc/stylesheets/$kdlcss $htmlgendir
endif

# --------------------------------------------------------------------
# We're done!
Done:
exit 0

# --------------------------------------------------------------------
Help:
  echo 'Usage:'
  echo '   rpcdoc [options] <format>'
  echo ' '
  echo 'Parameters:'
  echo '   options:            special processing instructions'
  echo '   format:             output format'
  echo ' '
  echo 'Options:'
  echo '   --help or --h       print usage instructions'
  echo '   --debug or --d      run in debug mode (no document generation)'
  echo ' '
  echo 'Output format:'
  echo '   html                generate HTML'
  echo '   pdf                 generate PDF (includes FO generation)'
  echo '   fo                  generate FO only (no PDF)'
  echo '   validate or v       validate only; no other processing'
exit 1

NotImplemented:
  switch ($notimplcase)
      echo 'This functionality has not been implemented'
      echo "See 'genrpcdoc.sh --h' for more information"
  endsw
exit 1

Usage:
  echo 'Usage: genrpcdoc [options] <format>'
  echo "See 'genrpcdoc --h' for more information"
exit 1

