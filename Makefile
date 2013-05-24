REPOS = github nargila origin

push: $(REPOS:%=push-%)


push-%:
	git push $*
	git push --tags $*

dist:
	mvn -Psign clean install
