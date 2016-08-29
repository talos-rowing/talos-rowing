REPOS = github nargila origin
MVN_ARGS = 

REVNO = $(shell git rev-list --count HEAD)

ifeq ($(TEST),0)
MVN_ARGS += -Dmaven.test.skip=true
endif

all:
	@echo available targets:
	@grep '^[^#[:space:]].*:' Makefile | sed 's,:.*,,' | grep -v $@

push: $(REPOS:%=push-%)


push-%:
	git push $*
	git push --tags $*

dist:
	mvn $(MVN_ARGS) -Psign clean install
