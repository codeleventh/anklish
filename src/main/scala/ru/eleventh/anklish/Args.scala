package ru.eleventh.anklish

import ru.eleventh.anklish.Const._
import scopt.{OParser, OParserBuilder}

import java.io.File

object Args {

  private val builder: OParserBuilder[Config] = OParser.builder[Config]

  val parser: OParser[Unit, Config] = {
    import builder._
    OParser.sequence(
      programName("anklish"),
      head("Scala script for adding English words to Anki decks"),
      head("Check the GitHub repository for more info: https://github.com/codeleventh/anklish"),
      opt[String]("deck")
        .abbr("d")
        .action((x, c) => c.copy(deck = Some(x)))
        .optional()
        .maxOccurs(1)
        .text(
          "Anki deck for adding the cards. If no deck specified, the default deck will be used"
        ),
      opt[String]("anki-binary-path")
        .abbr("anki")
        .action((x, c) => c.copy(ankiBinaryPath = x))
        .optional()
        .maxOccurs(1)
        .text(
          s"Path to Anki binary. It will be triggered to run if the Anki Connect port does not respond (\"$ARG_ANKI_BINARY_PATH\" as a default)"
        ),
      opt[Int]("max-cards-to-add")
        .action((x, c) => c.copy(maxCardsToAdd = x))
        .optional()
        .maxOccurs(1)
        .text(
          s"The maximum number of cards to be added to the deck ($ARG_MAX_CARDS as a default)"
        ),
      opt[Int]("max-unlearned-cards")
        .abbr("max")
        .action((x, c) => c.copy(maxUnlearnedCards = Some(x)))
        .optional()
        .maxOccurs(1)
        .text(
          "The maximum number of unlearned cards allowed in the deck (taking into account the existing ones)\nThis parameter (if it isn't greater) takes precedence over previous one"
        ),
      opt[Unit]("reversible")
        .abbr("rev")
        .action((x, c) => c.copy(reversible = true))
        .optional()
        .maxOccurs(1)
        .text("Parameter that adds reversed copy of card"),
      arg[File]("<input_file>")
        .required()
        .maxOccurs(1)
        .action((x, c) => c.copy(file = Some(x)))
        .text("Input file containing the word list"),
      help("help").text("Print help and exit")
    )
  }

  case class Config(
                     maxUnlearnedCards: Option[Int] = None,
                     maxCardsToAdd: Int = ARG_MAX_CARDS,
                     ankiBinaryPath: String = ARG_ANKI_BINARY_PATH,
                     deck: Option[String] = None,
                     reversible: Boolean = false,
                     file: Option[File] = None
                   )
}
