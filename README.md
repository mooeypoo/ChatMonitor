![Java CI with Maven](https://github.com/mooeypoo/ChatMonitor/workflows/Java%20CI%20with%20Maven/badge.svg) ![GitHub last commit](https://img.shields.io/github/last-commit/mooeypoo/ChatMonitor) [![Maintainability](https://api.codeclimate.com/v1/badges/ba8dfbdc3905b70cbcb5/maintainability)](https://codeclimate.com/github/mooeypoo/ChatMonitor/maintainability) [![Test Coverage](https://api.codeclimate.com/v1/badges/ba8dfbdc3905b70cbcb5/test_coverage)](https://codeclimate.com/github/mooeypoo/ChatMonitor/test_coverage) [![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code_of_conduct.md) [![Donate to the project!](https://img.shields.io/badge/Buy%20me%20a%20coffee!-Donate-ff69b4?style=flat)](https://ko-fi.com/mooeypoo)


# Mincraft-ChatMonitor

![ChatMonitor banner](https://raw.githubusercontent.com/mooeypoo/ChatMonitor/main/assets/ChatMonitor-header.png)

A plugin for minecraft server (Spigot) that monitors the chat and responds when text matches a list of words.

Automatically respond to messages in chat or commands sent by users. ChatMonitor is a flexible configurable system allowing you to respond to any trigger and decide whether the matching message:
* Should be muted
* Should be responded to directly to the user or publically as a chat broadcast
* Cancelled, if it is part of an action
* Trigger further commands to the server, or to other plugins.

ChatMonitor allows you to define groups of words, each group with separate responses, replies, and options, all configurable in configuration files.


## Install

* Download the [latest release of the plugin](https://github.com/mooeypoo/ChatMonitor/releases).
* Place the `.jar` file in your `/plugins` folder.

After running the plugin for the first time, you can edit `config.yml` with your groups. The config files for the groups will be auto-generated for you the first time the plugin is reloaded with the group settings in the main config. You can then edit those to add the word list and specific properties below.

Read below about the definition of the configuration files.

### Initial configuration
If you want to start with an initial configuration, there are config files available in the Example folder that will let you kickstart your word-lists.

* [ChatMonitor_wordgroup_offensive.yml](https://github.com/mooeypoo/ChatMonitor/blob/main/src/main/resources/Examples/ChatMonitor_wordgroup_offensive.yml) - Contains a base list of regular expressions aimed to capture most common offensive language in English. This file will continuously be updated, so make sure to update the list periodically.

Download any of those files (or multiple) and add them to your `/plugins/ChatMonitor` data folder. Make sure to add the group names to your `config.yml` (example: `- offensive` in the group list to include the `ChatMonitor_wordgroup_offensive.yml` file.)


## Usage
The plugin listens to text events in chat and commands and responds according to the given configuration. The outgoing chat is tested against the words that are provided in the configuration files, and if a match is found, the system will then respond based on the group's configuration.

As a result, the plugin can be used for various purposes, like:

* **Dealing with offensive language** -- muting the message, sending a response to the user, and triggering a warning or ban command.
* **Responding to keywords** -- allowing the message through and responding with a broadcast response to the entire chat.
* **Dealing with code of conduct violations** -- responding to terms that indicate violation of the rules by muting the message, sending a warning to the user or general chat, or escalating by tiggering a followup command for warning or banning the user.

### Examples

Responding to an offensive word by muting the message, sending the user a note, and triggering a command for a ban-manager plugin.
![Example 1](https://raw.githubusercontent.com/mooeypoo/ChatMonitor/main/assets/offensive-words-example.png)

Responding to trigger "!rules" by broadcasting a message to the entire chat.
![Example 2](https://raw.githubusercontent.com/mooeypoo/ChatMonitor/main/assets/response-faq-example.png)

**Note about multiple groups:** The order of evaluating the configuration files is not entirely predictable. If you provide the same word in different groups that have different response definitions, the system will only respond with what it considers the first instance, which may be any of the groups. Please avoid having the same word or the same phrase in different groups. **This is even more important if you are using regular expressions that may match the same result in two different groups**.

### Configuration
The plugin reads the main configuration files and then the subsequent word lists in secondary configuration files. When setting up groups in the main config file, you must also create corresponding config files that are named as `words_[group name].yml` and place the group's definition and response instructions.

#### Main configuration file
The main configuration file has the following parameters:

##### defaultmessage (String)
The default message for all responses. If a group has no specific message, this message will be used instead. 

Example: 

```
defaultmessage: Please don't use this word on this server!
```

##### groups (Array) 
Defines the groups that will include words and actions. There's no limit to the number of groups, but there must be at least one group and corresponding configuration file. Each group requires a configuration file named `words_[groupname].yml` in the same place as the main `config.yml` file. 

**NOTE:** The system will look for files that match the naming scheme exactly, including case sensitivity. Watch out that there is a difference between the group `minor` and `Minor` whether you name your config file `words_minor.yml` or `words_Minor.yml`.

Example:

```
groups:
  - minor
  - major
  - faq_rules
```

#### Group configuration file
Each group must have a configuration file that defines its behavior and the list of words its testing against. This allows the admin to set up different responses (good or bad) depending on a set of words. The words are set up as regular expression, and would work either as stright string lookup or more elaborate regular expression, if needed.

The group configuration file has the following parameters:

##### message (String)
The message that will be sent to the user if a word in the list is matched with the user's message. If none is provided, the main `defaultmessage` from the main config is used.

Example:

```
message: This message was not sent because it goes against our rules about age talk on this server. Please refer to the rules of the server on Discord.
```

##### preventsend (Boolean)
Dictates whether the message itself -- if matched to one of the words -- will be prevented from being sent and processed at all. If false, the message will proceed as usual, even if other actions are triggered. If `true`, the message will be muted, and if it is a command, it will not be stopped from being triggered.

Example:

```
preventsend: true
```

##### broadcast (Boolean)
Dictates whether to send the reply message to the entire chat. If set to true, the message is broadcasted as a general messsage to all players. If set to false, only the player who triggered this will receive the message, without anyone else seeing it.

Example:

```
broadcast: true
```

##### includecommands (Array)
If given, these are commands that the plugin will also examine for matching words. By default, the system only looks at chat messages. Whatever list of command names given in this array will mean the system also looks at the text even if that command was issued -- and may prevent the processing of the command if a word in the list is matched. A good example of this is the `tell` command; if that command is included in this array, the system will examine the "private" message text for matching words as well. Otherwise, it will not look at those at all. This field can also include commands form other plugins.

Example:

```
includecommands:
  - tell
```

##### words (Array)
The list of words (or regular expressions) that would trigger the behavior of this group. These can include any strings and regular expressions.

**CAVEAT:** Please beware of repeated terms across groups or regular expressions that may overlap across groups, as these would only trigger the first group the system's read, which is non-predictable.

Example:

```
words:
	- love to help
	- you're welcome
	- can help you
	- can help u
```

##### runcommands (Array)
An array of commands to run if one of the words has matched the incoming text. These can be base mojang commands or commands that involve other plugins. Similar to other strings, these can also utilize the message variables (see below)

Example:

```
runcommands:
- 'ban %player% Automatic ban: Use of forbidden word (%word%).'
```

#### Message variables
**NOTE:** All messages and commands can have three message-parameters that are then internally replaced within the system:

* `%player%` will be replaced by the name of the player that triggered the chat message or command.
* `%word%` will be replaced by the word from the player's message that matched the rule
* `%matchrule%` will be replaced by the match rule from the group config that matched the word given by the user

## Example

See the [Configuration example](https://github.com/mooeypoo/ChatMonitor/wiki/Configuration-example) for some use cases.

## Author and Contribution

Written and developed by mooeypoo (c) 2020. Distributed under GPL-v3 License.

Please report bugs and offer more features in the [issues](https://github.com/mooeypoo/ChatMonitor/issues) tab!

**Pull requests are welcome!** :heart_eyes:
