
Last Sync:
	commit 65da77017c6f33c1dcdd50435118371f37e0e6da
	Merge: 3d93ed4 d69db28
	Author: Samu Voutilainen <smar@smar.fi>
	Date:   Tue Jul 19 13:57:54 2011 +0300

	    Merge commit 'refs/merge-requests/70' of git://gitorious.org/qt-jambi/qtjambi-community


Steps:
	Copy $QTJAMBI_SRC/generator/*** src/main/qmake/
	Edit src/main/qmake/generator.pri fixup $INCLUDEPATH location for src/main/cpp/
	Edit src/main/qmake/generator.pri add MSVC2010 warning disable.


Rebase history:
	commit faeedf368db89dbe7c6cc1732efd2f76fbd64408
	Author: Akos Kemives <akoskm@gmail.com>
	Date:   Wed Mar 30 13:23:24 2011 +0200

	commit 050a8f7fcf4bdd565867f60204877f9932aa7437
	Merge: 8897433 40ad0fc
	Author: Samu Voutilainen <smar@smar.fi>

	commit 4404e55b3f6c9b4a920f8fce8b55428b9dcff678
	Merge: 13e2020 143df9b
	Author: Samu Voutilainen <smar@smar.fi>

	commit a55cbde3273d7d853915e24808e277c5ec803758
	Author: Marius Brehler <marbre@linux.sungazer.de>
	Date:   Tue Jun 14 16:45:41 2011 +0200

	commit c2e8510c55eec3e5251795561911720e0c6f3dca
	Merge: 189b7f1 4410cfd
	Author: Darryl L. Miles <darryl.miles@darrylmiles.org>
	Date:   Thu Jun 16 13:54:00 2011 +0100

31-Apr-2011
	faeedf3..050a8f7  master     -> origin/master
	# rebase/20110531_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110531_1generator.patch 

14-Jun-2011
	050a8f7..4404e55  master     -> origin/master
	# rebase/20110614_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110614_1generator.patch

15-Jun-2011
	4404e55..a55cbde  master     -> origin/master
	# rebase/20110615_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110615_1generator.patch

16-Jun-2011
	a55cbde..c2e8510  master     -> origin/master
	# rebase/20110616_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110616_1generator.patch

19-Jul-2011
	c2e8510..65da770  master     -> origin/master
	# rebase/20110719_1generator.patch
	cd src/main/qmake/ && patch -p2 < ../../../../rebase/20110719_1generator.patch
