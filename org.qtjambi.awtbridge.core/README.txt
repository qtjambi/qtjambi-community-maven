
Last Sync:
	commit 3d75736bb30623c5858fec33ad8c0a3419d35c52
	Author: Gunnar Sletta <gunnar.sletta@nokia.com>
	Date:   Thu Jul 8 07:44:30 2010 +0200

	    Replicate the repo...


Steps:
	Copy $QTJAMBI_AWTBRIDGE_SRC/com/trolltech/research/qtjambiawtbridge/* src/main/java/com/trolltech/research/qtjambiawtbridge/
	Copy $QTJAMBI_AWTBRIDGE_SRC/*** [!com,!typesystem_qawt.xml,!pregenerated] src/main/qmake/
	#Copy $QTJAMBI_AWTBRIDGE_SRC/typesystem_qawt.xml src/main/generator/
	Copy $QTJAMBI_AWTBRIDGE_SRC/ generatorstep.bat generatorstep.sh global.h typesystem_qawt.xml => src/main/generator/


This project (on Windows platforms) looks like it uses MFC (check on this
point), so this will mean bundling the necessary redistributabe DLLs in JAR
bundles.

We are going to need qtjambi.dll from the org.qtjambi.core project to link
against.
