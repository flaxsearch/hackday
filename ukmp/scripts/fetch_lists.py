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
import logging, logging.config


def read_since(datadir, username):
    """Read the user's since values from their since file in the data directory.
    
    The since file stores a list of up to 100 max_id values in ascending order. We can
    then use this to periodically reindex tweets already seen.
    
    Returns [0] if the file does not exist.
    """
    filepath = os.path.join(datadir, username + '.since')
    if not os.path.exists(filepath):
        return [0]
    else:
        with open(filepath) as f:
            data = f.read()
        return [long(x) for x in data.split()] if data else [0]

def write_since(datadir, username, since):
    """Write the user's since values to the since file in the data directory."""
    with open(os.path.join(datadir, username + '.since'), 'w') as f:
        f.write('\n'.join(str(x) for x in since))

def get_api(auth):
    return twitter.Api(consumer_key=auth['consumer_key'],
        consumer_secret=auth['consumer_secret'],
        access_token_key=auth['access_token_key'],
        access_token_secret=auth['access_token_secret'])

def handle_stats(data_dir, user, stats, since):
    max_id = 0
    with open(os.path.join(data_dir, user + '.tweets'), 'a') as f:
        for s in stats:
            max_id = max(max_id, s.id)
            f.write(json.dumps(s.AsDict()) + '\n')

    if max_id > since[-1]:
        if since == [0]: 
            since = []
        # add the latest since value and truncate the list
        since = (since + [max_id])[config['reindexes'][0]:]
        write_since(data_dir, user, since)


if len(sys.argv) != 2:
    print "Usage: fetch_lists.py <configfile>"
    raise SystemExit(1)

# Read the config
with open(sys.argv[1]) as f:
    config = yaml.load(f)

with open(config['twitter']['auth']) as f:
    auth = yaml.load(f)

logging.config.dictConfig(config['logging'])
logger = logging.getLogger('fetch')

# Initialise the API connection
api = get_api(auth)

for party in config['party_lists']:
    party_config = config['party_lists'][party]
    slug = party_config['twitter_list_slug']
    since = read_since(config['data_dir'], slug)

    # reindexes in config should look something like [-48, -4, -1]
    # it should always include -1!
    reindexes = config['reindexes']
    try:
        all_stats = {}
        # start with earlier since values (re-indexing tweets)
        # FIXME this is a pretty dumb algorithm
        for reindex in reindexes:
            try:
                since_id = since[reindex]
                stats = api.GetListTimeline(list_id=None, slug=slug, 
                    owner_screen_name=party_config['twitter_screen_name'], 
                    since_id=since_id, 
                    count=config['twitter']['tweet_count'],
                )
                logger.info("Got {2} tweets for {0} since {1}".format(
                    slug, since_id, len(stats)))
                
                # merge into all_stats
                for s in stats:
                    all_stats[s.id] = s
                    
                time.sleep(config['twitter']['delay'])

            except IndexError as e:
                pass

            handle_stats(config['data_dir'], slug, all_stats.values(), since)

    except twitter.TwitterError as e:
        logger.error("%s: Caught TwitterError '%s'" % (slug, e))


