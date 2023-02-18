.PHONY: fmt
fmt:
	poetry run black *.py
	poetry run isort *.py

.PHONY: lint
lint:
	poetry run isort --check --diff .
	poetry run black --check --diff .
	poetry run flake8 .*py

.PHONY: test
test:
	poetry run pytest tests/ --cov=autonote --cov-report=xml

.PHONY: install
install:
	poetry install
