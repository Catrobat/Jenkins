#!/bin/bash

apt-get update
apt-get -y install python-pip
pip install ansible
ansible-playbook setup_jenkins.yml
