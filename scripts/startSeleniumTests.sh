#!/bin/bash
make stop-selenium
make refresh-all
xvfb-run --server-args="-screen 0, 840x600x24" make run-selenium-tests
