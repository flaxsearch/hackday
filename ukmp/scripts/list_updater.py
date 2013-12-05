
import sys
import os
import twitter
import json
import yaml
import time

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

