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

"""Script to read through either the current or archived tweets, parse and index
them, including the addition of extracted entities and party names to the
tweet data.

Run with --archive flag to only read the files that have already been archived.
By default, will move the current tweet files to the archive, and only index
them.

Requires:
  - pyYaml (python-yaml on Ubuntu)
"""
import sys
import json
import yaml
import shutil
import time
import optparse
import os
import requests
from threading import Thread
import Queue
import logging, logging.config


mons = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']


class Indexer(Thread):
    """Indexer thread class. Reads items from a queue, indexes
    until None token is found.
    """
    # Shared update count
    update_count = 0

    def __init__(self, queue, config):
        Thread.__init__(self)
        self.queue = queue
        self.config = config
        self.stanford_url = config["stanford"]["handler_url"]
        self.solr_url = config["solr"]["update_url"]

    def run(self):
        while True:
            tweet = self.queue.get()
            if tweet == None:
                return

            tweet_text = get_full_text(tweet)
            stweet = {
                "id": tweet["id"],
                "text": tweet_text,
                'retweet_count': tweet.get('retweet_count', 0),
                'favorite_count': tweet.get('favorite_count', 0),
                'user_screen_name': tweet['user']['screen_name'],
                'user_full_name': tweet['user']['name'],
                'created_at': extract_date(tweet['created_at']),
                'party': tweet['party']
            }

            # Handle the optional properties, as available
            if 'place' in tweet:
                stweet['place_country'] = tweet['place']['country']
                stweet['place_full_name'] = tweet['place']['full_name']
            if 'geo' in tweet:
                stweet['place_location'] = tweet['geo']['coordinates']
            if 'user_mentions' in tweet:
                stweet['ent_mentions_screen_name'] = []
                stweet['ent_mentions_full_name'] = []
                for mention in tweet['user_mentions']:
                    stweet['ent_mentions_screen_name'].append(mention['screen_name'])
                    # Not all user profiles have a full name
                    if 'name' in mention:
                        stweet['ent_mentions_full_name'].append(mention['name'])
            if 'hashtags' in tweet:
                stweet['ent_hashtags'] = tweet['hashtags']
            if 'urls' in tweet:
                stweet['ent_urls'] = tweet['urls'].values()
        
            # Extract recognised entities from the text - note NOT tweet entities
            try:
                stanford_data = extract_stanford_data(self.stanford_url, tweet_text)
                stweet.update(stanford_data['entities'])
                if stanford_data['sentiment']:
                    stweet['sentiment'] = stanford_data['sentiment']['value']
            except Exception as e:
                # Abort if the webapp won't respond
                logger.error("Caught exception getting Stanford data: %s" % e)
                break

            response = requests.post(self.solr_url,
                headers=dict(config["solr"]["headers"]),
                data=json.dumps([stweet]))

            Indexer.update_count += 1
            self.queue.task_done()


def build_filelist(config, archive, tweetdir):
    """Get the list of files to be indexed. If archive = True, will read all
    files from the archive directory (and ignore the current data), otherwise
    will move the current tweets to the archive directory and just index them.
    Returns an array of filepaths to index."""
    filelist = []

    if archive:
        # Get list of all files in the archive directory
        for parent, subdirs, filenames in os.walk(config["archive_dir"]):
            for filename in filenames:
                if filename.endswith(".tweets"):
                    filelist.append(os.path.join(parent, filename))
    elif tweetdir:
        # Change base directory
        basedir = os.path.join(config["archive_dir"], tweetdir)
        for filename in os.listdir(basedir):
            if (filename.endswith(".tweets")):
                filelist.append(os.path.join(basedir, filename))
    else:
        # Move current tweet files to archive directory
        today_dir = os.path.join(config["archive_dir"], time.strftime("%Y%m%d"))
        if not os.path.exists(today_dir):
            os.makedirs(today_dir)
        tstamp = time.strftime("%H%M%S")
        for party in config["party_lists"]:
            tweetfile = config["party_lists"][party]["twitter_list_slug"] + ".tweets"
            tweetpath = os.path.join(config["data_dir"], tweetfile)
            if os.path.exists(tweetpath) and os.stat(tweetpath).st_size > 0:
                archive = "{0}_{1}.tweets".format(tweetfile[:-7], tstamp)
                destfile = os.path.join(today_dir, archive)
                shutil.move(tweetpath, destfile)
                filelist.append(destfile)

    logger.info("Found %d files to index" % len(filelist))
    return filelist


slug_parties = {}
def get_party_from_filename(filename, config):
    """Figure out which party this file should be indexed for. Assumes the
    first part of the filename is the list slug.
    Returns None if the party cannot be identified from the list slug."""
    party = None

    # Cut off the underscore, timestamp and .tweets extension
    slug = os.path.basename(filename)[:-14]
    if slug in slug_parties:
        party = slug_parties[slug]
    else:
        for p in config["party_lists"]:
            if slug == config["party_lists"][p]["twitter_list_slug"]:
                slug_parties[slug] = p
                party = p
                break
        if not party:
            slug_parties[slug] = None

    return party

def extract_date(datestring):
    # "Mon Jul 08 22:31:53 +0000 2013"
    yr = datestring[-4:]
    mn = mons.index(datestring[4:7])
    dy = datestring[8:10]
    tm = datestring[11:19]

    return "%s-%02d-%sT%sZ" % (yr, mn, dy, tm)

def extract_stanford_data(stanford_url, text):
    response = requests.post(stanford_url,
        headers = { "content-type": "application/json" },
        data = json.dumps({ 'text': text }))

    return json.loads(response.text)

def get_full_text(tweet):
    """Get the full tweet text. Re-tweeted text may be truncated with no
    indication except for ending with a ... character, but the original 
    text may be retrieved from the retweeted_status.
    Returns the full message text where available, else the tweet text."""
    text = tweet['text']
    if text.endswith(u'\u2026') and 'retweeted_status' in tweet:
        # Find the first word in the original text
        rt_text = tweet['retweeted_status']['text']
        spPos = rt_text.index(' ')
        word = rt_text[:spPos]
	# Locate the first word in the tweet text, and rebuild the text
	# starting there
        txtPos = text.index(word)
        text = text[:txtPos] + rt_text
    return text

def check_indexers(indexers):
    """Check if all the indexer threads are running, returning false if all
    are dead.
    
    Parameters:
    - indexers - the array of indexer threads.
    """
    live_count = 0
    for indexer in indexers:
        if indexer.is_alive():
            live_count += 1
    if live_count != len(indexers):
        logger.warn("%d/%d indexers running" % (live_count, len(indexers)))
    return live_count > 0

def main(opts, config):
    # Get the list of files to index
    if opts.tweet_file:
        tweetfiles = [ opts.tweet_file ]
    else:
        tweetfiles = build_filelist(config, opts.archive, opts.tweet_dir)

    if not tweetfiles:
        return

    solr_url = config["solr"]["update_url"]

    # create a queue
    queue = Queue.Queue(100)

    # create and start the indexer threads
    indexers = []
    for i in xrange(config['num_threads']):
        logger.debug('Starting indexer thread {0}'.format(i))
        indexer = Indexer(queue, config)
        indexer.start()
        indexers.append(indexer)

    for tweetfile in tweetfiles:
        party = get_party_from_filename(tweetfile, config)
        count = 0
        for line in open(tweetfile):
            # Skip blank lines
            if line.strip():
                tweet = json.loads(line)
                tweet['party'] = party
                if (queue.full() and not check_indexers(indexers)):
                    # No indexers running - abort!
                    raise RuntimeError("Indexers not running")
                queue.put(tweet)
                count += 1
        logger.debug('queued {0} tweets from {1}'.format(count, tweetfile))
            
    # wait for the indexer threads to complete
    for indexer in indexers:
        queue.put(None)
    for indexer in indexers:
        indexer.join()

    # Send a hard commit
    response = requests.post(solr_url,
        headers = dict(config["solr"]["headers"]),
        data='{"commit":{}}')
    if response.status_code != 200:
        logger.error('Response error code from Solr: {0} {1}'.format(
            response.status_code, response.text))
        raise Exception, 'FIXME'

    logger.info('indexed {0} tweets in total'.format(Indexer.update_count))
     

if __name__ == '__main__':
    p = optparse.OptionParser()
    p.add_option("--archive", action="store_true", dest="archive")
    p.add_option("-a", action="store_true", dest="archive")
    p.add_option("-d", dest="tweet_dir")
    p.add_option("-f", dest="tweet_file")
    opts, args = p.parse_args()

    if len(args) != 1:
        print "Usage: indexer.py [--archive|-a] [-d <tweetdir>] <configfile>"
        raise SystemExit(1)

    # Read the config
    with open(args[0]) as f:
        config = yaml.load(f)

    logging.config.dictConfig(config['logging'])
    logger = logging.getLogger('indexer')
    main(opts, config)
