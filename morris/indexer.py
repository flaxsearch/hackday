import sys
import json

with open(sys.argv[1]) as f:
    tweets = json.load(f)

mons = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

for tweet in tweets:
    ents = tweet['entities']
    
    #"Mon Jul 08 22:31:53 +0000 2013"
    yr = tweet['created_at'][-4:]
    mn = mons.index(tweet['created_at'][4:7])
    dy = tweet['created_at'][8:10]
    tm = tweet['created_at'][11:19]
    
    stweet = {
        'id': tweet['id'],
        'text': tweet['text'],
        'ent_mentions_screen_name': [x['screen_name'] for x in ents['user_mentions']],
        'ent_mentions_full_name': [x['name'] for x in ents['user_mentions']],
        'ent_hashtags': [x['text'] for x in ents['hashtags']],
        'ent_urls': [x['expanded_url'] for x in ents['urls']],
        'retweeted': tweet['retweeted'],
        'place_country': tweet['place']['country'] if tweet['place'] else None,
        'place_full_name': tweet['place']['full_name'] if tweet['place'] else None,
        'place_location': tweet['geo']['coordinates'] if tweet['geo'] else None,
        'user_screen_name': tweet['user']['screen_name'],
        'user_full_name': tweet['user']['name'],
        'created_at': '%s-%02d-%sT%sZ' % (yr, mn, dy, tm)
    }
    
    print json.dumps(stweet, indent=2)
