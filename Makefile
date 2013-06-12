REPOS = github nargila origin
MVN_ARGS = 

ifeq ($(TEST),0)
MVN_ARGS += -Dmaven.test.skip=true
endif

push: $(REPOS:%=push-%)


push-%:
	git push $*
	git push --tags $*

dist:
	mvn $(MVN_ARGS) -Psign clean install
