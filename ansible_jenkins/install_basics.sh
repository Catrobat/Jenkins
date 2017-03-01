#!/bin/bash

apt-get update
apt-get -y install python-pip
pip install ansible
ansible-playbook master_jenkins_setup.yml
