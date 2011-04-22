/*
 *
 *
 *
 */

#include "qt_test.h"

#include <QDesktopWidget>

MyApplication::MyApplication(int &argc, char **argv) :
	QApplication(argc, argv)
{
	init();
}

void
MyApplication::init()
{
	time(&starttime);
	endtime = starttime + 5;
	done = false;

	qMainWindow = new QMainWindow();

	qButton = new QPushButton();
	qButton->setMinimumWidth(180);
	qButton->setMinimumHeight(35);
	QObject::connect(qButton, SIGNAL(clicked()), this, SLOT(myQuit()));
	update_button(*qButton);

	qMainWindow->setCentralWidget(qButton);
	QRect r = qMainWindow->frameGeometry();
	r.moveCenter(desktop()->availableGeometry().center());
	qMainWindow->move(r.topLeft());
	qMainWindow->show();

	timer = new QTimer(qMainWindow);
	QObject::connect(timer, SIGNAL(timeout()), this, SLOT(tick()));
	timer->start(334);
}

MyApplication::~MyApplication()
{
	if(qMainWindow) {
		qMainWindow->hide();
		delete qMainWindow;
		qMainWindow = NULL;
	}
}

int
MyApplication::update_button(QPushButton &button)
{
	char buf[1024];
	int left;

	if(done)
		return 1;
	left = endtime - time(NULL);
	if(left <= 0) {
		snprintf(buf, sizeof(buf), "done");
		done = true;
	} else {
		snprintf(buf, sizeof(buf), "%d seconds left", left);
	}
	QString label = QString(buf);
	button.setText(label);
	return 0;
}

void
MyApplication::tick(void)
{
	tick_count++;
	if(update_button(*qButton)) {
		emit qButton->click();
	}
}

void
MyApplication::myQuit(void)
{
	// Write to file ?
	exit(0);
}
