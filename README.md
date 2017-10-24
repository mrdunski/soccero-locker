# soccero-locker

Slack bot for scheduling games on common game console.

## How to start and test project

### Create slack workspace
* Go to <https://slack.com/>
* Create new workspace
* Go to <https://api.slack.com/>
* Build new app
* Add `bots` feature and add bot user
* Install app to the workspace
* Copy `Bot User OAuth Access Token`
* On any channel in your workspace invite bot user

### Start application

#### Gradle
run command `SLACK_TOKEN="your_access_token" ./gradlew(.bat) run`

#### Runner
* set env variable SLACK_TOKEN with your workspace token
* run `com.leanforge.game.GameBootApplication#main`

#### Local docker image
*  `./gradlew dockerBuildImage`
* `docker run -e SLACK_TOKEN=your_access_token mrdunski/soccero-locker:SNAPSHOT`

#### Docker image from [dockerhub](https://hub.docker.com/r/mrdunski/soccero-locker/)


`docker run -e SLACK_TOKEN=your_access_token mrdunski/soccero-locker`

## (some of) Coding conventions used in project
* Group functionality in package (for example classes that build game queues goes to `com.leanforge.game.queue`)
* Use repository to map data source to domain object
* Repository should be minimalistic - handle CRUD operations and data queries
* Put domain logic into domain service
* Create separate service for logic that does not fit into domain of any other service or uses more than one domain
* Use controllers to separate framework's entry points from business code
* Follow SOLID and DRY
* Test your code

### Slack controller example

```java
@Controller // Spring stereotype for controller bean
@SlackController // Marker for slack event listener
public class SampleSlackController {

  @Autowired
  SlackService slackService;
  
  // listener for message "simple message"
  @SlackMessageListener("simple message") 
  public void handleSimpleMessage(
          // injects channel id
          @SlackChannelId String channelId) {
    slackService.sendChannelMessage(channelId, "received simple message");
  }

  // listener for pattern "print (.+)"
  @SlackMessageListener("print (.+)")
  public void echo(
          @SlackChannelId String channelId,
          // Inject group 1 from pattern defined in listener
          @SlackMessageRegexGroup(1) String string) {
    slackService.sendChannelMessage(channelId, string);
  }
  
  @SlackReactionListener("gold") // listen for gold reaction  
  public void handleGold(
          SlackMessage message,
          @SlackUserId String userId,
          @SlackChannelId String channelId) {
    slackService.addReactions(message, "onion");
    slackService.sendChannelMessage(channelId, "<@" + userId + "> :gold:", "onion");
  }
}
```

## Health endpoint
Available at: [/health](http://localhost:8080/health)

## Websocket
Available at: [/games](ws://localhost:8080/games)

For event types check `com.leanforge.game.event.model`

## Available commands and features
Write `help` or check `com.leanforge.game.help.HelpController`