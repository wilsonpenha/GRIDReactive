#
# GRIDReactive
#
# Follow this steps to build and run this project
#

# using Eclipse IDE
1. copy this project into your workspace, then from your Java project perspective import it by selecting existing project from your workspace,
   Then build it.
2. open the setting.properties file at br.com.gridreactive package and set both required fields consumerKey and consumerSecret that you can build and get from your Tweeter
   account settings.
3. you can customize your setting by adding more fields to be retrieve from both github and tweets.
4. right click at RetrieveGitHubWithTweets class from br.com.gridreactive package and click Run As Java application, then you will get the output at your IDE console.

# using Ant build
1. got to /yourplace/GRIDReactive folder and make sure you have both java SE 8 and Ant 1.9 or higher, installed and configured at your machine
2. follow the Eclipse IDE steps 2 and 3,
3. to build this project run the command line "ant" to build your project, this will create a few options for you to use this app,
   this will deploy all .java, .class, .jar and your properties file at ./target/ like this:
   ./target/classes                  -> used to build your distrib-zip package
   ./distrib-zip/                    -> your package location and also your runtime location
   ./grid_reactive-0.1.0-distrib.zip -> build this in case you want to integrate with any application, so you only need to unpack this and use it
4. to run the GridReactive app after build with ant, go to the folder ./target/distrib-zip and change your .sh file execution permission with chmod +x call_github.sh, and then call it ./call_github.sh,
   this app will output your json with Reactive projects from github within tweets.

How to customize this application setting to bring more or less data from both github and tweets at br.com.gridreactive/setting.properties:

#
# Retrieve GitHub with Tweets configuration
#

# GitsHub settings
numberOfProjectsToList=7
githubProjectsList=github_projects

# specify all fields to output from github result, set * to bring all fields 
githubRetrieveFields=name,full_name,description,created_at,updated_at,recent_tweets,contributors_url,open_issues_count
#githubRetrieveFields=*

# Search Tweets credentials setting, this is required to use this tool
consumerKey=XXXXXXXXX
consumerSecret=YYYYYYYYYYYYYYYYYYYYYY

numberOfTweetsToList=5
searchTweetsByGithubField=full_name

# it's the json tag to bring the tweets
tweetsResults=statuses

# specify all fields to output from tweets result, set * to bring all fields
tweetsRetrieveFields=created_at,id,text,entities,user
#tweetsRetrieveFields=*

# set this one as true to bring tweets search metadata
getTweetsMetadata=false

