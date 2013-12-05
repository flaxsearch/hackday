#!/usr/bin/env python
#
# Copyright 2013 Lemur Consulting Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Script to read through a set of configured Twitter lists and update the list
of accounts whose tweets should be read.
"""
import sys
import os
import twitter
import yaml

if len(sys.argv) != 2:
    print "Usage: list_updater.py <configfile>"
    raise SystemExit(1)

with open(sys.argv[1]) as f:
    config = yaml.load(f)

# Initialise the API connection
api = twitter.Api(consumer_key=config['twitter']['consumer_key'],
    consumer_secret=config['twitter']['consumer_secret'],
    access_token_key=config['twitter']['access_token_key'],
    access_token_secret=config['twitter']['access_token_secret'])

# And look up the members of each party list
for party in config['party_lists']:
    party_config = config['party_lists'][party]
    data_dir = party_config['data_dir']
    if not os.path.exists(data_dir):
        os.mkdir(data_dir)

    for user in api.GetListMembers(list_id=None, 
        slug=party_config['twitter_list_slug'], 
        owner_screen_name=party_config['twitter_screen_name'], 
        skip_status=True):
        idfile = os.path.join(data_dir, "{0}.since".format(user.screen_name))
        if not os.path.exists(idfile):
            print "Creating %s" % idfile
            with open(idfile, "w") as out:
                out.write("0");

