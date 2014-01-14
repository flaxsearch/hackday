UKMP project
============

This project has been built on top, and extending, the work done with the
UKMP Parliamentary tweet data which was used on the original hackday. The
tweet data is fed through the Stanford NER library to extract organisations,
people and locations mentioned in the text.

The data is collected from four Twitter lists, created and maintained by the 
@tweetminster account. These are used to assign party affiliation at indexing
time (Conservative, Labour, LibDem and Other).

(I did spend time trying to download the tweets directly from the MPs 
Twitter feeds, but kept getting blocked, though I didn't think I was breaking
the restriction limit. If anyone has insight into why this was happening, or
can suggest a workaround, please let me know!)

The project consists of a number of components at this time:

1. scripts/fetch_lists.py, which accesses the Twitter feeds, and writes the
collected tweet data into files.

2. scripts/indexer.sh, which will index the tweet data. This relies on the 
Java web application to run through the Stanford NER entity extraction.

3. Solr, to index the data and make it accessible through the web front end.

4. A Java web application, which both provides an interface for the indexer
to access the Stanford NER extractor, and acts as a back-end for an 
AngularJS-based front end.


Requirements
------------

In order to run the application, you will need the following packages
installed. The application has been running under Linux, and has not been
tested on Windows.

- Java 1.7
- Maven
- Python 2.7
- Solr 4.x
- The python-twitter library, available at 
http://code.google.com/p/python-twitter/ (follow the instructions to install
it).


Running Solr
------------

The application was built using Solr 4.6.0, though any Solr 4.x release should
be suitable. The start_solr.sh script in the solr directory has some defaults 
for running Solr using the configuration files from github. Change the paths 
as necessary to point to the correct Solr download location and data 
directories.

You will probably also need to modify the logger configration, in 
solr/collection1/conf/log4j.properties so that the logging details go to
a sensible location.


Running the web application
---------------------------

The web application uses the DropWizard framework to manage the back-end, 
while AngularJS provides the front-end interface. The front-end communicates
through the DropWizard back-end, providing a layer between Solr and the wider
web.

To build the application, go to the java/ukmp directory, and use the following
maven command:

    mvn clean package

This will download all the required libraries and build a fat jar file which
can be run directly from the command line (the first time it builds will
take a while - the Stanford NER libraries are pretty big, and downloading
them can be slow).

To run the application, do the following (from the ukmp base directory):

    cd java/ukmp
    java -jar target/ukmp-0.0.1-SNAPSHOT.jar server config/ukmp.yml

The ukmp.yml file contains configuration details for the application. Paths
will need to be changed as necessary.

To check the web application is running as expected, open a browser and
navigate to http://localhost:8080/service/browse. If there is no data in the
search engine, you will get a relatively short JSON response. If data is
available, it will be considerably longer!

The web front-end can be accessed through http://localhost:8080/.


Fetching tweets
---------------

In order to fetch some tweets, you will first need to copy the 
scripts/conf/example_config.yml file and fill in details for a
Twitter application. You will need to create and register this yourself
using the instructions here: 
https://dev.twitter.com/docs/auth/application-only-auth.

Once you have created and filled in the twitter consumer_key, consumer_secret,
access_token_key and access_token_secret in your config.yml file, you may 
also want to change the data_dir and archive_dir properties. data_dir is the
directory to which the tweets will be written when they are fetched, while
archive_dir is where they will be stored by the indexer script. Change them
as appropriate for your setup.

Once that is done, fetch some tweets using

    python fetch_lists.py conf/config.yml

If everything is working as it should, you should have eight files created in
the data_dir -  for each party list there will be one file containing tweet 
data (xxx.tweets), and one containing the most recent tweet id (xxx.since).

The fetch_tweets.py script can be run from a cron job using the shell script
fetch_lists.sh. Modify the paths as appropriate.


Indexing tweets
---------------

As mentioned above, both Solr and the web application need to be running in
order for tweets to be successfully indexed.

You should also check the config.yml file created above to ensure the 
archive_dir, entity extractor URL (pointing to the web application) and the
Solr update URL are correct for your system.

Once that is done, index the current tweets using

    python indexer.py conf/config.yml

There are a number of additional options from the command line to index tweet
files from the archive (use "-a" to index all archived tweets, or 
"-d yyyyMMdd" to index tweets for a particular day).
