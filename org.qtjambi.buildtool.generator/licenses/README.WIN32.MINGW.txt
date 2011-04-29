
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
	* libgcc*.dll and libstdc++*.dll this is covered under GNUs
		"GCC Runtime Library exception version 3.1".  A reference
		to this maybe found at
		[http://www.gnu.org/licenses/gcc-exception.html].  It is
		understood the source code form of this library is
		redistributed via the downloads section of www.mingw.org
		(which is the location this project obtained its copy).  We
		have not modified the originals in any way.
	* mingw*.dll this is Public Domain as per http://mingw.org/license
		"MinGW runtime: The MinGW base runtime package has been placed
		in the public domain, and is not governed by copyright. This
		basically means that you can do what you like with the code."
		although the information doesn't provide clear demarcation of
		exactly which code belongs in this bracket, but we interpret
		it to cover at least the DLLs listed in this item.  Copyrightable
		works in the public should probably have an original copyright
		holder that empowers a public domain license and waiver to those
		copyrights.  This way any future challenge of plagiarism on the
		works can be defended from this stance to call into question the
		original copyright holders initial ownership of the works.  We
		have not modified the originals in any way.
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

