#initial:
#$ make
# >will make AndroidManifest.xml from myAndroidManifest.xml then die
#
#$ export ANDROID=/whereever/android-sdk-linux_86
#$ make target T=10
# or # make target TARGET=fullosname
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
#$ make
#or
# $ make inst      -for only copying to emu
# $ make phone 	-for only copying to phone
#
#build copies only .java stuff from src/ into src_/, converting symlinks into hardlinks
#and embeds bzr-version into AndroidManifest.xml - original should be myAndroidManifest.xml
#needs symlinker.py from svd_util

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

PACK=$(wildcard bin/*$(REL).apk)
#PACK=bin/$(APPNAME)-debug.apk

SYMLINKER = PATH=..:$$PATH PYTHONPATH=../py:../../py symlinker.py
SRC_EXCLUDE =

build:
	perl -ne 's/(android:versionName=)".*?"/\1"$(VER)"/;s/(android:versionCode=)".*?"/\1"$(VERi)"/;print' myAndroidManifest.xml > AndroidManifest.xml
	rm -rf src_
	#cd src; for a in `find -L ./ -name \*.java -exec dirname {} \; | sort | uniq`; do  mkdir -p ../src_/$$a; ln $$a/*.java ../src_/$$a; done
	#cp -rl src src_; find -L src_ -not -name \*.java -a -not -type d -a -exec rm {} \;
	$(SYMLINKER) --op=link --inc='*.java' src src_
	cd src_; rm -f $(SRC_EXCLUDE)
	ant $(REL)

install: build
	$(MAKE) inst
#	adb shell setprop dalvik.vm.enableassertions all

inst:
	adb -e install -r $(PACK) 2>&1 | grep -v bytes.in

phone:
	adb -d install -r $(PACK)

emu_new:
	#android create avd --name devtest --target 1
	#sudo umount /media/android_sdcard
	emulator -avd $(AVD) -sdcard $(SDCARD) -dns-server $(DNS) -wipe-data &

emu:
	emulator -avd $(AVD) -sdcard $(SDCARD) -dns-server $(DNS) &

sdcard:
	mksdcard -l sdcard 256M $(SDCARD)

adb:
	adb kill-server
#	killall adb

android:
	android

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
	adb -e shell cat /data/data/$(APP)/sh*/com*
rmpref:
	adb -e shell rm /data/data/$(APP)/sh*/com*

log:
	adb -e logcat

shell:
	adb shell $(ARGS)

SRC?= src
EXCLUDE= src_
.PHONY: tags
tag tags:
	ctags -R --langmap=matlab: $(EXCLUDE:%=--exclude=%) $(SRC)


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

rel release:
	#mkdir -p $(RELDIR)/old/
	#mv $(RELDIR)/*apk $(RELDIR)/old/
	cp $(PACK) $(RELTARGET)_`date +%Y%m%d`_$(VER).apk

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
