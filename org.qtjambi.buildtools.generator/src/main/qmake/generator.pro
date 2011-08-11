contains(QT_CONFIG, release) {
    CONFIG -= debug
    CONFIG += release
}

# Input
HEADERS += \
        classlistgenerator.h \
        cppgenerator.h \
        cppheadergenerator.h \
        cppimplgenerator.h \
        docparser.h \
        generatorsetjava.h \
        javagenerator.h \
        jumptable.h \
        metainfogenerator.h \
        metajavabuilder.h \
        qdocgenerator.h \
        uiconverter.h \

SOURCES += \
        classlistgenerator.cpp \
        cppgenerator.cpp \
        cppheadergenerator.cpp \
        cppimplgenerator.cpp \
        docparser.cpp \
        generatorsetjava.cpp \
        javagenerator.cpp \
        jumptable.cpp \
        metainfogenerator.cpp \
        metajavabuilder.cpp \
        qdocgenerator.cpp \
        uiconverter.cpp \

include(generator.pri)

#win32-msvc* {
#	QMAKE_CFLAGS_RELEASE -= -O2
#	QMAKE_CFLAGS_LTCG -= -GL
#	QMAKE_CFLAGS_RELEASE += -ZI -Od -Fdgenerator
#
#	QMAKE_CFLAGS_DEBUG -= -Zi
#	QMAKE_CFLAGS_DEBUG += -ZI -Od -Fdgenerator
#
#	QMAKE_CXXFLAGS_RELEASE -= -O2
#	QMAKE_CXXFLAGS_LTCG -= -GL
#	QMAKE_CXXFLAGS_RELEASE += -ZI -Od -Fdgenerator
#
#	QMAKE_CXXFLAGS_DEBUG -= -Zi
#	QMAKE_CXXFLAGS_DEBUG += -ZI -Od -Fdgenerator
#
#	QMAKE_LFLAGS_LTCG -= /LTCG
#	QMAKE_LFLAGS_RELEASE += /DEBUG /OPT:REF
#	QMAKE_LFLAGS_DEBUG += /OPT:REF
#}
