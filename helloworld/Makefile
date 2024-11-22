# ./quickstart/helloworld/Makefile
.PHONY: all clean check-direnv

all: check-direnv package deploy

SOURCES := $(shell find src -name "*.java")

# Check if direnv is available
DIRENV := $(shell command -v direnv 2> /dev/null)

# Ensure we're using direnv in this Makefile
SHELL := $(shell which bash)
.SHELLFLAGS := -eu -o pipefail -c

check-direnv:
ifndef DIRENV
	$(error "direnv is not installed. Please install direnv first")
endif
	@if [ ! -f .envrc ]; then \
		echo "Error: .envrc file not found in current directory"; \
		exit 1; \
	fi

package target/ROOT.war: check-direnv pom.xml $(SOURCES)
	direnv exec . mvn -Popenshift,container package

deploy:
	limactl shell podman podman push \
		--sign-by-sigstore-private-key /Users/asgeir.nilsen/sigstore.private \
		--sign-passphrase-file /Users/asgeir.nilsen/sigstore.passwd \
		lima-debian.internal/helloworld

clean: check-direnv
	direnv exec . mvn -Popenshift,container clean
	rm -rf target
