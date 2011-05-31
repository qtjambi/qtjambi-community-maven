
Last Sync:
	commit 050a8f7fcf4bdd565867f60204877f9932aa7437
	Merge: 8897433 40ad0fc
	Author: Samu Voutilainen <smar@smar.fi>
	Date:   Sun May 29 19:30:41 2011 +0300

	    Merge commit 'refs/merge-requests/19' of git://gitorious.org/qt-jambi/qtjambi-community


Steps:
	Copy $QTJAMBI_SRC/generator/*** src/main/qmake/
	Edit src/main/qmake/generator.pri fixup $INCLUDEPATH location for src/main/cpp/
	Edit src/main/qmake/generator.pri add MSVC2010 warning disable.


Rebase history:
	commit faeedf368db89dbe7c6cc1732efd2f76fbd64408
	Author: Akos Kemives <akoskm@gmail.com>
	Date:   Wed Mar 30 13:23:24 2011 +0200

31-Apr-2011
	faeedf3..050a8f7  master     -> origin/master
	# rebase/20110531_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110531_1generator.patch 
