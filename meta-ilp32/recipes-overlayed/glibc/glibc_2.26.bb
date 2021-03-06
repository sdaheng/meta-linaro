require glibc.inc

LIC_FILES_CHKSUM = "file://LICENSES;md5=e9a558e243b36d3209f380deb394b213 \
      file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
      file://posix/rxspencer/COPYRIGHT;md5=dc5485bb394a13b2332ec1c785f5d83a \
      file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

DEPENDS += "gperf-native"

SRCREV ?= "fcbbcf128ec90d0d6ca2d641324fa097a1aaeac6"

SRCBRANCH ?= "arm/ilp32"

GLIBC_GIT_URI ?= "git://git.linaro.org/toolchain/glibc.git;protocol=https"
UPSTREAM_CHECK_GITTAGREGEX = "(?P<pver>\d+\.\d+(\.\d+)*)"

SRC_URI = "${GLIBC_GIT_URI};branch=${SRCBRANCH};name=glibc \
           file://etc/ld.so.conf \
           file://generate-supported.mk \
           \
           ${NATIVESDKFIXES} \
           file://0005-fsl-e500-e5500-e6500-603e-fsqrt-implementation.patch \
           file://0006-readlib-Add-OECORE_KNOWN_INTERPRETER_NAMES-to-known-.patch \
           file://0007-ppc-sqrt-Fix-undefined-reference-to-__sqrt_finite.patch \
           file://0008-__ieee754_sqrt-f-are-now-inline-functions-and-call-o.patch \
           file://0009-Quote-from-bug-1443-which-explains-what-the-patch-do.patch \
           file://0010-eglibc-run-libm-err-tab.pl-with-specific-dirs-in-S.patch \
           file://0011-__ieee754_sqrt-f-are-now-inline-functions-and-call-o.patch \
           file://0012-sysdeps-gnu-configure.ac-handle-correctly-libc_cv_ro.patch \
           file://0013-Add-unused-attribute.patch \
           file://0014-yes-within-the-path-sets-wrong-config-variables.patch \
           file://0015-timezone-re-written-tzselect-as-posix-sh.patch \
           file://0016-Remove-bash-dependency-for-nscd-init-script.patch \
           file://0017-eglibc-Cross-building-and-testing-instructions.patch \
           file://0018-eglibc-Help-bootstrap-cross-toolchain.patch \
           file://0019-eglibc-Clear-cache-lines-on-ppc8xx.patch \
           file://0020-eglibc-Resolve-__fpscr_values-on-SH4.patch \
           file://0021-eglibc-Install-PIC-archives.patch \
           file://0022-eglibc-Forward-port-cross-locale-generation-support.patch \
           file://0023-Define-DUMMY_LOCALE_T-if-not-defined.patch \
           file://0024-elf-dl-deps.c-Make-_dl_build_local_scope-breadth-fir.patch \
           file://0025-locale-fix-hard-coded-reference-to-gcc-E.patch \
           file://0027-glibc-reset-dl-load-write-lock-after-forking.patch \
           file://0028-Bug-4578-add-ld.so-lock-while-fork.patch \
"

NATIVESDKFIXES ?= ""
NATIVESDKFIXES:class-nativesdk = "\
           file://0001-nativesdk-glibc-Look-for-host-system-ld.so.cache-as-.patch \
           file://0002-nativesdk-glibc-Fix-buffer-overrun-with-a-relocated-.patch \
           file://0003-nativesdk-glibc-Raise-the-size-of-arrays-containing-.patch \
           file://0004-nativesdk-glibc-Allow-64-bit-atomics-for-x86.patch \
"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build-${TARGET_SYS}"

PACKAGES_DYNAMIC = ""

# the -isystem in bitbake.conf screws up glibc do_stage
BUILD_CPPFLAGS = "-I${STAGING_INCDIR_NATIVE}"
TARGET_CPPFLAGS = "-I${STAGING_DIR_TARGET}${includedir}"

GLIBC_BROKEN_LOCALES = ""
#
# We will skip parsing glibc when target system C library selection is not glibc
# this helps in easing out parsing for non-glibc system libraries
#
COMPATIBLE_HOST:libc-musl:class-target = "null"

EXTRA_OECONF = "--enable-kernel=${OLDEST_KERNEL} \
                --without-cvs --disable-profile \
                --disable-debug --without-gd \
                --enable-clocale=gnu \
                --enable-add-ons=libidn \
                --with-headers=${STAGING_INCDIR} \
                --without-selinux \
                --enable-obsolete-rpc \
                --enable-obsolete-nsl \
                --enable-tunables \
                --enable-bind-now \
                --enable-stack-protector=strong \
                --enable-stackguard-randomization \
                ${GLIBC_EXTRA_OECONF}"

EXTRA_OECONF += "${@get_libc_fpu_setting(bb, d)}"
EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'libc-inet-anl', '--enable-nscd', '--disable-nscd', d)}"


do_patch:append() {
    bb.build.exec_func('do_fix_readlib_c', d)
}

do_fix_readlib_c () {
	sed -i -e 's#OECORE_KNOWN_INTERPRETER_NAMES#${EGLIBC_KNOWN_INTERPRETER_NAMES}#' ${S}/elf/readlib.c
}

do_configure () {
# for ilp32 we need to run autconf
	cd ${S}/sysdeps/unix/sysv/linux/aarch64; autoconf -I../../../../..;cd -
	cd ${S}/sysdeps/aarch64; autoconf -I../..;cd -
	sed -i "s/10.0.0/4.11.0/g" ${S}/sysdeps/unix/sysv/linux/aarch64/configure.ac

# override this function to avoid the autoconf/automake/aclocal/autoheader
# calls for now
# don't pass CPPFLAGS into configure, since it upsets the kernel-headers
# version check and doesn't really help with anything
        (cd ${S} && gnu-configize) || die "failure in running gnu-configize"
        find ${S} -name "configure" | xargs touch
        CPPFLAGS="" oe_runconf
}

rpcsvc = "bootparam_prot.x nlm_prot.x rstat.x \
	  yppasswd.x klm_prot.x rex.x sm_inter.x mount.x \
	  rusers.x spray.x nfs_prot.x rquota.x key_prot.x"

do_compile () {
	# -Wl,-rpath-link <staging>/lib in LDFLAGS can cause breakage if another glibc is in staging
	unset LDFLAGS
	base_do_compile
	(
		cd ${S}/sunrpc/rpcsvc
		for r in ${rpcsvc}; do
			h=`echo $r|sed -e's,\.x$,.h,'`
			rm -f $h
			${B}/sunrpc/cross-rpcgen -h $r -o $h || bbwarn "${PN}: unable to generate header for $r"
		done
	)
	echo "Adjust ldd script"
	if [ -n "${RTLDLIST}" ]
	then
		prevrtld=`cat ${B}/elf/ldd | grep "^RTLDLIST=" | sed 's#^RTLDLIST="\?\([^"]*\)"\?$#\1#'`
		if [ "${prevrtld}" != "${RTLDLIST}" ]
		then
			sed -i ${B}/elf/ldd -e "s#^RTLDLIST=.*\$#RTLDLIST=\"${prevrtld} ${RTLDLIST}\"#"
		fi
	fi

}

# Use the host locale archive when built for nativesdk so that we don't need to
# ship a complete (100MB) locale set.
do_compile:prepend:class-nativesdk() {
    echo "complocaledir=/usr/lib/locale" >> ${S}/configparms
}

require glibc-package.inc

BBCLASSEXTEND = "nativesdk"
