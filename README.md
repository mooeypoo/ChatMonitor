# Mincraft-ChatMonitor

A plugin for minecraft server (bukkit) that monitors the chat and responds when text matches a list of words.

## Stability
This plugin is considered "BETA" stage. It was not fully tested on large scale servers with other plugins.

## Usage
The plugin listens to text events in chat and commands and responds according to the given configuration. The outgoing chat is tested against the words that are provided in the configuration files, and if a match is found, the system will then respond based on the group's configuration.

**Beware:** The order of evaluating the configuration files is not entirely predictable. If you provide the same word in different groups that have different response definitions, the system will only respond with what it considers the first instance, which may be any of the groups. Please avoid having the same word or the same phrase in different groups. **This is even more important if you are using regular expressions that may match the same result in two different groups**.

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
  - thanks
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

##### includecommands (Array)
If given, these are commands that the plugin will also examine for matching words. By default, the system only looks at chat messages. Whatever list of command names given in this array will mean the system also looks at the text even if that command was issued -- and may prevent the processing of hte command if a word in the list is matched. A good example of this is the `tell` command; if that command is included in this array, the system will examine the "private" message text for matching words as well. Otherwise, it will not look at those at all. This field can also include commands form other plugins.

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
* `%word%` will be replaced by the word in the word list that matched the rule in the group.

## Example
This plugin is fairly flexible and allows admins to use this for censoring bad speech, reminding the users of behavior rules, or congratulating users for good behavior. Admins can create different groups that trigger different behaviors. 

The example below sets up three groups:
- "minor" group for minor curse words that will mute the message and remind the user they should read the rules.
- "major" group for curse words and bad behavior that proceeds to run commands to ban the user
- "thanks" group that congratulates users for being nice to each other.

Setting up the above would require one main configuration file (`config.yml`) and 3 sub-config files (`words_minor.yml`, `words_major.yml` and `words_thanks.yml`)

### Config files for the example

Here are the configuration files that would work with this example:

**Main config** `config.yml`

```
defaultmessage: Bad word intercepted (%word%). Please don't do that!
groups:
- minor
- major
- thanks
```

**Minor group config** `words_minor.yml`

```
message: This message was not sent because it goes against our rules about age talk
  on this server. Please refer to the rules of the server on Discord.
preventsend: true
incluecommands:
- tell
words:
- how old are you
- your age
- my age
```

**Major group config** `words_major.yml`

```
message: We don't allow this word (%word%) on this server. Your message was not sent.
preventsend: true
incluecommands:
- tell
words:
- badword
- worseword
runcommands:
- 'ban %player% Automatic ban: Use of forbidden word (%word%).'
```

**Thanks group config** `words_thanks.yml`

```
message: Aww, thank you for being so friendly, %player%!
preventsend: false
words:
- love to help
- you're welcome
- can help you
- can help u
runcommands:
  - give %player% minecraft:gold_ingot 4
```

Note that the `minor` and `major` groups will not allow the message to be sent (they both have `preventsend: true`) while the `thanks` group allows for the message to be sent, but also sends the user a message and gives the player an award.

### Behavior for the example

The config files above would produce the following behaviors:

| Player saying | Anyone else seeing this message | Result |
| --- | --- |
| Hey, I'd love to help with that! | Yes | Message appearing on the player's screen, and the player gets 4 gold ingots. |
| Ooh, how old are you? | No | Message appearing on the player's screen. |
| What a badword you are! | No | Message appearing on the player's screen. Player banned. |

## Author and Contribution

Written and developed by mooeypoo (c) 2020. Distributed under GPL-v3 License.

Please report bugs and offer more features in the [issues](https://github.com/mooeypoo/ChatMonitor/issues) tab!

**Pull requests are welcome!** :heart_eyes: