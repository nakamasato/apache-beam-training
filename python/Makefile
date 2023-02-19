.PHONY: fmt
fmt:
	poetry run isort --profile black *.py
	poetry run black *.py
	poetry run flake8 *.py

.PHONY: lint
lint:
	poetry run isort --profile black --check --diff .
	poetry run black --check --diff .
	poetry run flake8 *.py

.PHONY: test
test:
	poetry run pytest tests/ --cov=autonote --cov-report=xml

.PHONY: install
install:
	poetry install
