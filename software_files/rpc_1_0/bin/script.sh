#!/bin/sh

#Part of the open-source KDL Relational PC package
# (see LICENSE for copyright and license information).
# Runs Python script, sets the appropriate classpath

# Get the bin directory
bindir=`echo "$0" | sed 's/script.sh$//'`
. "${bindir}/classpath.sh"

java -Xmx2049M -classpath $classpath -Djava.library.path="${bindir}/../lib" rpc.app.PythonScript $*
