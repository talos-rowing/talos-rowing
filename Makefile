REPOS = github nargila origin
MVN_ARGS = 

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
