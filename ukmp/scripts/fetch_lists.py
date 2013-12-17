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

"""Script to read through the set of tweet lists and retrieve up to 200
tweets per list.
"""
import sys
import os
import twitter
import json
import yaml
import time

def read_since(datadir, username):
    """Read the user's since value from their since file in the data directory.
    Returns 0 if the file does not exist."""
    filepath = os.path.join(datadir, username + '.since')
    if not os.path.exists(filepath):
        return 0
    else:
        with open(filepath) as f:
            data = f.read()
        return long(data) if data else 0

def write_since(datadir, username, since):
    """Write the user's since value to the since file in the data directory."""
    with open(os.path.join(datadir, username + '.since'), 'w') as f:
        f.write(str(since))

def get_api(config):
    return twitter.Api(consumer_key=config['twitter']['consumer_key'],
        consumer_secret=config['twitter']['consumer_secret'],
        access_token_key=config['twitter']['access_token_key'],
        access_token_secret=config['twitter']['access_token_secret'])

def handle_stats(data_dir, user, stats, since):
    max_id = 0
    with open(os.path.join(data_dir, user + '.tweets'), 'a') as f:
        for s in stats:
            max_id = max(max_id, s.id)
            f.write(json.dumps(s.AsDict()) + '\n')
    if max_id > since:
        write_since(data_dir, user, max_id)


if len(sys.argv) != 2:
    print "Usage: fetch_tweets.py <configfile>"
    raise SystemExit(1)

# Read the config
with open(sys.argv[1]) as f:
    config = yaml.load(f)

# Initialise the API connection
api = get_api(config)

for party in config['party_lists']:
    party_config = config['party_lists'][party]
    slug = party_config['twitter_list_slug']
    since = read_since(config['data_dir'], slug)
    try:
        stats = api.GetListTimeline(list_id=None, slug=slug, 
            owner_screen_name=party_config['twitter_screen_name'], 
            since_id=since, count=config['twitter']['tweet_count'])
        print "Got {2} tweets for {0} since {1}".format(slug, since, len(stats))
        handle_stats(config['data_dir'], slug, stats, since)
    except twitter.TwitterError as e:
        print "%s: Caught TwitterError '%s'" % (slug, e)
    time.sleep(config['twitter']['delay'])

