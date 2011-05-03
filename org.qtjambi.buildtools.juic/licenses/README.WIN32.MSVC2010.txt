
This document seeks to explain the rationale of understanding for which the
project maintainers operate under in respect of redistributing some 3rd party
binary components with a binary redistributable download of this project.

This document should merely be interpreted as a reference for a legal expert
to find a place to start.

Please remember there is no legal warranty provided by this information, you
should commission your own legal expert to evaluate your specific circumstance
with using and/or redistribution this project (as a whole or in part).



As of MinGW (April 2011) using:
	* GCC 4.5.2
	* GNU ld (GNU Binutils) 2.21

The licensing that covers:
	* MSVCRT*.dll ????? FIXME ????? Find the exact caveat and licensing for
		distributing MSVCRT*DLL with an application.
	* Qt*.dll are distributed in binary form under the LGPL v2.1
		license, this license is covered in the distributed source code
		from http://qt.gitorious.org/qt/qt which is inside the tree in a
		file called
		[http://qt.gitorious.org/qt/qt/blobs/master/LICENSE.LGPL]
		The overall licensing contains an exception to the LGPL
		"Nokia Qt LGPL Exception version 1.1" available at 
		http://qt.gitorious.org/qt/qt/blobs/master/LGPL_EXCEPTION.txt
		More information above Qt licensing maybe found at
		http://qt.nokia.com/about/licensing/
		It is possible the QtJambi project has modified the original Qt
		source before building our own version of Qt these source code
		modifications are available from the git repository
		[http://qt.gitorious.org/~dlmiles/qt/dlmiless-qt]
	* *.exe files this is not considered a derived work of the GNU
		compiler/toolchain and therefore out of scope of the toolchain
		licenses used to build it.  It is however inside the scope
		of the licenses covering the enclosing project(s) where the
		source is contained.
		This is licensed under simultaneous compatible
		LGPL v2.1, GPL v2, GPL v3 licenses and all contributions
		are expected under the same license scheme.
	* All other parts should be directly covered by the license(s) of
		the enclosing project itself.
		This is licensed under simultaneous compatible
		LGPL v2.1, GPL v2, GPL v3 licenses and all contributions
		are expected under the same license scheme.


If you have any reason to believe the understanding above is incorrect or
out of date please get in contact with the project maintainers.

