/*
 *
 *
 *
 */

#ifndef _QT_TEST_H
#define _QT_TEST_H

#include <time.h>

#include <QtCore>
#include <QApplication>
#include <QMainWindow>
#include <QPushButton>

class MyApplication : public QApplication
{
	Q_OBJECT
private:
	QMainWindow *qMainWindow;
	QPushButton *qButton;
	QTimer *timer;
	int tick_count;

	time_t starttime;
	time_t endtime;

	void init();
	int update_button(QPushButton &button);
public:
	MyApplication(int &argc, char **argv);
	virtual ~MyApplication();
	void tick();
	void myQuit();
};

extern int my_function(int a, int b);

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
}
#endif

#endif /* _QT_TEST_H */
