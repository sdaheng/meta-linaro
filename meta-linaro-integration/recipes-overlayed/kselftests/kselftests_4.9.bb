SUMMARY = "Linux Kernel Selftests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

SRC_URI = "https://www.kernel.org/pub/linux/kernel/v4.x/linux-${PV}.tar.xz"

SRC_URI[md5sum] = "0a68ef3615c64bd5ee54a3320e46667d"
SRC_URI[sha256sum] = "029098dcffab74875e086ae970e3828456838da6e0ba22ce3f64ef764f3d7f1a"

S = "${WORKDIR}/linux-${PV}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "libcap popt rsync-native"

inherit kernel-arch

# Filter out arch specific tests
TARGETS = " \
	${@bb.utils.contains_any("TARGET_ARCH", [ "x86", "x86-64" ], "breakpoints", "", d)} \
	capabilities \
	cpu-hotplug \
	efivarfs \
	exec \
	firmware \
	ftrace \
	futex \
	${@bb.utils.contains_any("TARGET_ARCH", [ "x86", "x86-64" ], "ipc", "", d)} \
	kcmp \
	lib \
	membarrier \
	memfd \
	memory-hotplug \
	mount \
	mqueue \
	net \
	nsfs \
	${@bb.utils.contains_any("TARGET_ARCH", [ "powerpc", "powerpc64" ], "powerpc", "", d)} \
	pstore \
	ptrace \
	seccomp \
	sigaltstack \
	size \
	static_keys \
	sysctl \
	timers \
	user \
	vm \
	${@bb.utils.contains_any("TARGET_ARCH", [ "x86", "x86-64" ], "x86", "", d)} \
	zram \
"

EXTRA_OEMAKE += "-C tools/testing/selftests TARGETS="${TARGETS}" INSTALL_PATH=${D}${bindir}/kselftests CC="${CC}""

# Their Makefiles are so sloppy, let's clean up a bit
do_configure () {
	sed "s|^CC := .*||g" -i ${S}/tools/testing/selftests/lib.mk
	sed "s|^CC = .*||g" -i ${S}/tools/testing/selftests/timers/Makefile
	sed "s|^CC = .*||g" -i ${S}/tools/testing/selftests/memfd/Makefile
	sed "s|^CC := .*||g" -i ${S}/tools/testing/selftests/powerpc/switch_endian/Makefile
	sed "s|gcc|\$(CC)|g" -i ${S}/tools/testing/selftests/breakpoints/Makefile
	sed "s|^LDFLAGS += -lrt -lpthread|LDLIBS += -lrt -lpthread|g" -i ${S}/tools/testing/selftests/timers/Makefile
	sed "s|BINARIES|TEST_PROGS|g" -i ${S}/tools/testing/selftests/sigaltstack/Makefile
}

do_compile () {
	oe_runmake
}

do_install () {
	oe_runmake install
	chown -R root:root ${D}
}

PACKAGE_BEFORE_PN = " \
	${PN}-breakpoints \
	${PN}-capabilities \
	${PN}-cpu-hotplug \
	${PN}-efivarfs \
	${PN}-exec \
	${PN}-firmware \
	${PN}-ftrace \
	${PN}-futex \
	${PN}-ipc \
	${PN}-kcmp \
	${PN}-lib \
	${PN}-membarrier \
	${PN}-memfd \
	${PN}-memory-hotplug \
	${PN}-mount \
	${PN}-mqueue \
	${PN}-net \
	${PN}-nsfs \
	${PN}-powerpc \
	${PN}-pstore \
	${PN}-ptrace \
	${PN}-seccomp \
	${PN}-sigaltstack \
	${PN}-size \
	${PN}-static-keys \
	${PN}-sysctl \
	${PN}-timers \
	${PN}-user \
	${PN}-vm \
	${PN}-x86 \
	${PN}-zram \
"

FILES:${PN}-breakpoints = "${bindir}/kselftests/breakpoints"
FILES:${PN}-capabilities = "${bindir}/kselftests/capabilities"
FILES:${PN}-cpu-hotplug = "${bindir}/kselftests/cpu-hotplug"
FILES:${PN}-efivarfs = "${bindir}/kselftests/efivarfs"
FILES:${PN}-exec = "${bindir}/kselftests/exec"
FILES:${PN}-firmware = "${bindir}/kselftests/firmware"
FILES:${PN}-ftrace = "${bindir}/kselftests/ftrace"
FILES:${PN}-futex = "${bindir}/kselftests/futex"
FILES:${PN}-ipc = "${bindir}/kselftests/ipc"
FILES:${PN}-kcmp = "${bindir}/kselftests/kcmp"
FILES:${PN}-lib = "${bindir}/kselftests/lib"
FILES:${PN}-membarrier = "${bindir}/kselftests/membarrier"
FILES:${PN}-memfd = "${bindir}/kselftests/memfd"
FILES:${PN}-memory-hotplug = "${bindir}/kselftests/memory-hotplug"
FILES:${PN}-mount = "${bindir}/kselftests/mount"
FILES:${PN}-mqueue = "${bindir}/kselftests/mqueue"
FILES:${PN}-net = "${bindir}/kselftests/net"
FILES:${PN}-nsfs = "${bindir}/kselftests/nsfs"
FILES:${PN}-powerpc = "${bindir}/kselftests/powerpc"
FILES:${PN}-pstore = "${bindir}/kselftests/pstore"
FILES:${PN}-ptrace = "${bindir}/kselftests/ptrace"
FILES:${PN}-seccomp = "${bindir}/kselftests/seccomp"
FILES:${PN}-sigaltstack = "${bindir}/kselftests/sigaltstack"
FILES:${PN}-size = "${bindir}/kselftests/size"
FILES:${PN}-static-keys = "${bindir}/kselftests/static_keys"
FILES:${PN}-sysctl = "${bindir}/kselftests/sysctl"
FILES:${PN}-timers = "${bindir}/kselftests/timers"
FILES:${PN}-user = "${bindir}/kselftests/user"
FILES:${PN}-vm = "${bindir}/kselftests/vm"
FILES:${PN}-x86 = "${bindir}/kselftests/x86"
FILES:${PN}-zram = "${bindir}/kselftests/zram"
FILES:${PN}-dbg += "${bindir}/kselftests/*/.debug"

ALLOW_EMPTY:${PN}-capabilities = "1"

RDEPENDS:${PN}-cpu-hotplug += "bash"
RDEPENDS:${PN}-efivarfs += "bash"
RDEPENDS:${PN}-memory-hotplug += "bash"
RDEPENDS:${PN}-net += "bash"
RDEPENDS:${PN}-vm += "bash"
RDEPENDS:${PN}-zram += "bash"
RDEPENDS:${PN} += "bash \
	${PN}-capabilities \
	${PN}-cpu-hotplug \
	${PN}-efivarfs \
	${PN}-exec \
	${PN}-firmware \
	${PN}-ftrace \
	${PN}-futex \
	${PN}-kcmp \
	${PN}-lib \
	${PN}-membarrier \
	${PN}-memfd \
	${PN}-memory-hotplug \
	${PN}-mount \
	${PN}-mqueue \
	${PN}-net \
	${PN}-nsfs \
	${PN}-pstore \
	${PN}-ptrace \
	${PN}-seccomp \
	${PN}-sigaltstack \
	${PN}-size \
	${PN}-static-keys \
	${PN}-sysctl \
	${PN}-timers \
	${PN}-user \
	${PN}-vm \
	${PN}-zram \
"

RDEPENDS:${PN}:append:x86 = " ${PN}-breakpoints ${PN}-ipc ${PN}-x86"
RDEPENDS:${PN}:append:x86-64 = " ${PN}-breakpoints ${PN}-ipc ${PN}-x86"
RDEPENDS:${PN}:append:powerpc = " ${PN}-powerpc"
RDEPENDS:${PN}:append:powerpc64 = " ${PN}-powerpc"

INSANE_SKIP:${PN} = "already-stripped"
