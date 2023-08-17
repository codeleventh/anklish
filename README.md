# Anklish

![](https://gist.githubusercontent.com/codeleventh/dbf7cfd9c2def11474500737a0443f58/raw/f81287f841e6a90497b881c65725d520a388fc8a/anklish.png)  
This is a Scala application for automatic creating Anki flashcards with English words definitions.  
It reads an input file with words, requests a dictionary definitions (via [third-party API](https://dictionaryapi.dev)),
and then creates the cards in a deck (by small chunks, depending on the settings).

My use case: I use [frequency dictionary](https://github.com/first20hours/google-10000-english/blob/master/20k.txt) (
which has been truncated at the suitable point) prepended with words that I bookmarked in my reader app (those words are
periodically imported by a third-party script). The program is executed daily through cron task.

## Prerequisites
* [Scala Build Tool](https://www.scala-sbt.org/)  
* [Anki](https://apps.ankiweb.net/) (versions greater than 2.1)  
* [AnkiConnect](https://ankiweb.net/shared/info/2055492159) add-on  

## Usage
Run `sbt "run [anklish_options] <input_file>"`  
Example: `sbt "run --deck \"english\" /home/codeleventh/english_words.txt"`

Command-line options:
```
  -d, --deck <value>       Anki deck for adding the cards. If no deck specified, the default deck will be used
  -anki, --anki-binary-path <value>
                           Path to Anki binary. It will be triggered to run if the Anki Connect port does not respond ("anki" as a default)
  --max-cards-to-add <value>
                           The maximum number of cards to be added to the deck (10 as a default)
  -max, --max-unlearned-cards <value>
                           The maximum number of unlearned cards allowed in the deck (taking into account the existing ones)
                           This parameter (if it isn't greater) takes precedence over previous one
  -rev, --reversible       Parameter that adds reversed copy of card along with the regular one
  <input_file>             Input file containing the word list
  --help                   Print help and exit
```

## License
This project is licensed under the [WTFPL License](http://www.wtfpl.net/):
> Copyright © 2016 Xiangrong Hao
>
> Everyone is permitted to copy and distribute verbatim or modified  copies of this license document, and changing it is allowed as long as the name is changed.
>
> DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE  
> TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION  
>
> 0. You just DO WHAT THE FUCK YOU WANT TO.


