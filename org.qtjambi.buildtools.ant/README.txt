
Last Sync:
	commit 65da77017c6f33c1dcdd50435118371f37e0e6da
	Merge: 3d93ed4 d69db28
	Author: Samu Voutilainen <smar@smar.fi>
	Date:   Tue Jul 19 13:57:54 2011 +0300

	    Merge commit 'refs/merge-requests/70' of git://gitorious.org/qt-jambi/qtjambi-community


Steps:
	Copy $QTJAMBI_SRC/src/java/ant-qtjambi/***/ant-qtjambi.xml src/main/resources/
	# Then what is left
	Copy $QTJAMBI_SRC/src/java/ant-qtjambi/*** src/main/java/


Rebase history:
	commit faeedf368db89dbe7c6cc1732efd2f76fbd64408
	Author: Akos Kemives <akoskm@gmail.com>
	Date:   Wed Mar 30 13:23:24 2011 +0200

	commit 050a8f7fcf4bdd565867f60204877f9932aa7437
	Merge: 8897433 40ad0fc
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
	# rebase/20110531_2toolsant.patch
	cd src/main/java/ && patch -p4 < ../../../../rebase/20110531_2toolsant.patch 

14-Jun-2011
	050a8f7..4404e55  master     -> origin/master
	# rebase/20110614_2toolsant.patch
	cd src/main/java/ && patch -p4 < ../../../../rebase/20110614_2toolsant.patch

16-Jun-2011
	a55cbde..c2e8510  master     -> origin/master
	# rebase/20110616_2toolsant.patch
	cd src/main/java/ && patch -p4 < ../../../../rebase/20110616_2toolsant.patch

19-Jul-2011
	c2e8510..65da770  master     -> origin/master
	# rebase/20110719_2toolsant.patch
	cd src/main/java/ && patch -p4 < ../../../../rebase/20110719_2toolsant.patch
