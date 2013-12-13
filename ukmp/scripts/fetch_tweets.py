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

"""Script to read through the complete list of accounts and retrieve up to 200
tweets per account.
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

def create_follow_list(datadir):
    follow = {}
    for filename in os.listdir(datadir):
        if filename.endswith('.since'):
            username = filename[:-6]
            follow[username] = read_since(data_dir, username)
    return follow

def get_api(config):
    return twitter.Api(consumer_key=config['twitter']['consumer_key'],
        consumer_secret=config['twitter']['consumer_secret'],
        access_token_key=config['twitter']['access_token_key'],
        access_token_secret=config['twitter']['access_token_secret'])

def handle_stats(data_dir, user, party, stats, since):
    max_id = 0
    with open(os.path.join(data_dir, user + '.tweets'), 'a') as f:
        for s in stats:
            max_id = max(max_id, s.id)
            s2 = dict(id=s.id, created=s.created_at, text=s.text,
                favorited=s.favorited, reply=s.in_reply_to_screen_name,
                location=s.location, party=party)
            f.write(json.dumps(s2) + '\n')
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
    data_dir = config['party_lists'][party]['data_dir']
    for user, since in create_follow_list(data_dir).iteritems():
        for i in xrange(config['twitter']['retries']):
            try:
                stats = api.GetUserTimeline(screen_name=user, since_id=since,                       count=config['twitter']['tweet_count'])
                print "Got {2} tweets for {0} since {1}".format(
                    user, since, len(stats))
                handle_stats(data_dir, user, party, stats, since)
                time.sleep(config['twitter']['delay'])
                break
            except twitter.TwitterError as e:
                print "%s: Caught TwitterError '%s' on attempt %d" % (
                        user, e, i)
                if i == (config['twitter']['retries'] - 1):
                    print "Too many retries - aborting"
                    raise SystemExit(1)
                else:
                    time.sleep(config['twitter']['retry_delay'])

