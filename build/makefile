
# no-nonsense android build system
# svd 2010-
#
#initial:
#
#fix ant.properties - source=src_ and package=..
#
#$ make
# >will make AndroidManifest.xml from myAndroidManifest.xml then die
#
#$ export ANDROID=/whereever/android-sdk-linux_86
#$ make target T=10
# or # make target TARGET=fullplatformname
# >will fix local.properties / project.properties
#
#$ fix by hand the local.properties / project-properties in all dependencies
#
#if needs android.support.v4/
#$ make support.libs
#
#run emulator
#$ make emu AVD=youravdimage DNS=yourdns
#
# ??? build.xml is autocreated
#
#$ make
#or
# $ make inst   -for only copying to emu
# $ make phone 	-for only copying to phone
#
#build copies only .java stuff from src/ into src_/, converting symlinks into hardlinks
#and embeds bzr-version into AndroidManifest.xml - original should be myAndroidManifest.xml
#needs symlinker.py from svd_util

#as in local.properties
ANDROID?=/home/android-sdk-linux_86

ifeq (,$(find $(ANDROID)/tools,$(PATH)))
export PATH:=$(ANDROID)/tools:$(PATH)
endif
ifeq (,$(find $(ANDROID)/platform-tools,$(PATH)))
export PATH:=$(ANDROID)/platform-tools:$(PATH)
endif
SDCARD?=sdcard.img
AVD?=a23
DNS?=192.168.100.1

APP = com.the.app.package
APPNAME = somename

#no paralelism here
#MAKEFLAGS=j1

all: clean install

VERi= $(shell bzr revno)
VER = beta-r$(VERi)
#VER = r$(shell svn info | perl -ne 'print if s/^Revision: *//')

REL = debug

#PACK=$(wildcard bin/*$(REL).apk)
PACK=bin/$(APPNAME)-$(REL).apk

#SYMLINKER = PATH=..:$$PATH PYTHONPATH=../py:../../py symlinker.py
SYMLINKER = PYTHONPATH=. symlinker.py
SRC_EXCLUDE ?=
SRC ?= src
SRC_?= src_

MYMANIFEST= myAndroidManifest.xml
MYBUILD= mybuild.xml
mymanifest: _manifest
_manifest:
	-test -f $(MYMANIFEST) && perl -ne 's/(android:versionName=)".*?"/\1"$(VER)"/; s/(android:versionCode=)".*?"/\1"$(VERi)"/; s/(package=)".*?"/\1"$(APP)"/; print' $(MYMANIFEST) > AndroidManifest.xml
	-test -f $(MYBUILD) && perl -ne 's/(<project name=)".*?"/\1"$(APPNAME)"/; print' $(MYBUILD) > build.xml

NODEPS=1
build: mymanifest
	rm -rf src_
	#cd src; for a in `find -L ./ -name \*.java -exec dirname {} \; | sort | uniq`; do  mkdir -p ../src_/$$a; ln $$a/*.java ../src_/$$a; done
	#cp -rl src src_; find -L src_ -not -name \*.java -a -not -type d -a -exec rm {} \;
	-$(SYMLINKER) --op=link --inc='*.java' --inc='*.properties' $(SRC_EXCLUDE:%=--exc='%') $(SRC) $(SRC_)
	#cd src_; rm -f $(SRC_EXCLUDE)
	ant $(ANTFLAGS) $(if $(NODEPS),nodeps) $(REL)

install: build
	$(MAKE) inst
#	adb shell setprop dalvik.vm.enableassertions all

inst:
	adb -e install -r "$(PACK)" 2>&1 | grep -v bytes.in

phone:
	adb -d install -r "$(PACK)"



EMUFLAGS += -no-boot-anim
EMUFLAGS += -noaudio	#CPU 100%->15%
#XXX OR: AVD set extra flag "Audio playback support" to "no": CPU 100%->15%
EMU = emulator -avd $(AVD) -sdcard $(SDCARD) -dns-server $(DNS) $(EMUFLAGS)

emu_new:
	$(EMU) -wipe-data &

emu:
	$(EMU) &

sdcard:
	mksdcard -l sdcard 256M $(SDCARD)

adbkill:
	adb kill-server
#	killall adb

adb:
	adb $(ARGS)

adbdev:
	adb devices

android:
	android $(ARGS)

clean:
	rm -rf bin/classes

sd:
	sudo mount -o loop sdcard /media/android_sdcard

DB= #databases
#e.g. DB=databases or wherever they are
getdb:
	adb pull /data/data/$(APP)/$(DB) .
putdb:
	adb shell mkdir   /data/data/$(APP)/$(DB)/
	adb push myepg.db /data/data/$(APP)/$(DB)/

catpref:
	adb -e shell cat /data/data/$(APP)/sh*/*
rmpref delpref:
	adb -e shell rm /data/data/$(APP)/sh*/*
putpref:
	adb -e push $(APP)_preferences.xml /data/data/$(APP)/shared_prefs/
getpref:
	adb -e pull /data/data/$(APP)/shared_prefs/$(APP)_preferences.xml .

log:
	adb -e logcat $(ARGS)

shell:
	adb shell $(ARGS)

SRCTAG ?= $(SRC)
EXCTAG ?= $(SRC_)
.PHONY: tags
tag tags:  plaintags
plaintags:
	ctags -R $(CTAGSFLAGS) $(EXCTAG:%=--exclude=%) $(SRCTAG)


TARGET=android-$T
target:
	@if test -z "$(TARGET)$T" ; then echo "TARGET=? or T=? e.g. 3 4 or 7" && false ; fi
	android update project -p . -t "$(TARGET)"

target-map: target
target-map: TARGET="Google Inc.:Google APIs:$T"

targets:
	android list targets

support.libs:
	mkdir -p libs
	ln -sf $(ANDROID)/extras/android/support/v4/android-support-v4.jar libs/

RELDIR_root = /somewhere/android
RELDIR = $(RELDIR_root)/$(APPNAME)
RELTARGET = $(RELDIR)/$(APPNAME)

VERPACK= `perl -ne 'print $$1 if s/android:versionName="([^"]+)"//' bin/AndroidManifest.xml`

rel release:
	#mkdir -p $(RELDIR)/old/
	#mv $(RELDIR)/*apk $(RELDIR)/old/
	cp $(PACK) $(RELTARGET)_`date -r $(PACK) +%Y%m%d`_$(VERPACK).apk
#	cp $(PACK) $(RELTARGET)_`date +%Y%m%d`_$(VER).apk

PID=$(APP)
memtrace:
	rm -f *.mm
	#PID=`$(MAKE) shell ARGS=ps | perl -ne 'print if s/^\S+ *(\d+) .*?$(APP).*/\1/'`; echo $$PID
	bash -c 'for ((a=1000;a<9990;a++)) ; do $(MAKE) shell ARGS="dumpsys meminfo $(PID)" >$$a.mm; grep -E "(size|allocated|Views|Activities):" $$a.mm || break ;  sleep 1; done'

hprof:
	adb shell chmod 777 /data/misc
	PID=`$(MAKE) shell ARGS=ps | perl -ne 'print if s/^\S+ *(\d+) .*?$(APP).*/\1/'`; echo $$PID; adb shell kill -10 $$PID
	sleep 3
	adb pull /data/misc ddd
	adb shell rm /data/misc/heap-dump*
	cd ddd; for a in heap-*.hprof; do hprof-conv $$a ok$$a && rm $$a; done

# vim:ts=4:sw=4:noexpandtab
