
Last Sync:
	commit 1bcb2c624ccb5c45425aa6af1ffe39dd17427950
	Author: Samu Voutilainen <smar@smar.fi>
	Date:   Wed Aug 10 23:49:06 2011 +0300

	    remove extra addition from exception message text so that message text
	    has correct values


Steps:
	Copy $QTJAMBI_SRC/src/java/qtjambi-util/*** src/main/java/


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

	commit 65da77017c6f33c1dcdd50435118371f37e0e6da
	Merge: 3d93ed4 d69db28
	Author: Samu Voutilainen <smar@smar.fi>
	Date:   Tue Jul 19 13:57:54 2011 +0300

31-Apr-2011
	faeedf3..050a8f7  master     -> origin/master
	# noaction

14-Jun-2011
	050a8f7..4404e55  master     -> origin/master
	# noaction

16-Jun-2011
	a55cbde..c2e8510  master     -> origin/master
	# rebase/20110616_4util.patch
	cd src/main/java/ && patch -p4 < ../../../../rebase/20110616_4util.patch

19-Jul-2011
	c2e8510..65da770  master     -> origin/master
	# noaction

11-Aug-2011
	65da770..1bcb2c6  master     -> origin/master
	# noaction
