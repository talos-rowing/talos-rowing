REPOS = github nargila origin
MVN_ARGS = 

_MVN_ARGS = -e $(MVN_ARGS)

REVNO = $(shell git rev-list --count HEAD)

ifdef SKIPTEST
_MVN_ARGS += -Dmaven.test.skip=true
endif

all:
	@echo available targets:
	@grep '^[^#[:space:]].*:' Makefile | sed 's,:.*,,' | grep -v $@

push: $(REPOS:%=push-%)


push-%:
	git push $*
	git push --tags $*

dist:
	mvn $(_MVN_ARGS) -Psign clean install

%:
	mvn $(_MVN_ARGS) $@
