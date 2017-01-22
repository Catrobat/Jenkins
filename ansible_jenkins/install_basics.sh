#!/bin/bash

apt-get update
apt-get -y install ansible
ansible-playbook install_new_ansible.yml
ansible-playbook master_jenkins_setup.yml
