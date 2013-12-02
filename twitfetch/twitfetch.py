import sys
import os
import twitter
import json
import time


datadir = sys.argv[1]

# get startup data (we need a screen-name.since file for each user, possibly empty)
follow = {}
for filename in os.listdir(datadir):
    if filename.endswith('.since'):
        filepath = os.path.join(datadir, filename)
        with open(filepath) as f:
            data = f.read()
        follow[filename[:-6]] = long(data) if data else None
    
# now get tweets!
api = twitter.Api(consumer_key='yFf0JI5GS8ffRhYUEtbVg',
                  consumer_secret='WXwLMcbUuHVfE9poqVFEuTwuGte0L4Bs7F3fvr7dPE',
                  access_token_key='1479928770-8IqC6WAfSCQsv5mv3yDgRbJkJMHxbsMUKCXT9oF',
                  access_token_secret='JtIIlH9faveHPML6O8nukqsu2PugfggFKbyLGLqaDY')

while (True):
    for user, since in follow.iteritems():
        stats = api.GetUserTimeline(user, since_id=since, count=200)        
        print 'got {2} tweets for {0} since {1}'.format(user, since, len(stats))
        max_id = 0
        with open(os.path.join(datadir, user + '.tweets'), 'a') as f:
            for s in stats:
                max_id = max(max_id, s.id)
                s2 = dict(id=s.id, created=s.created_at, text=s.text, 
                          favorited=s.favorited, reply=s.in_reply_to_screen_name, 
                          location=s.location)
                f.write(json.dumps(s2) + '\n')

        with open(os.path.join(datadir, user + '.since'), 'w') as f:
            f.write(str(max_id))
        
        follow[user] = max_id
        time.sleep(5)
