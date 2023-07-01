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
      opt[String]("deckName")
        .abbr("d")
        .action((x, c) => c.copy(deckName = Some(x)))
        .optional()
        .maxOccurs(1)
        .text(
          "Anki deck for adding the cards. If no deck specified, the default deck will be used"
        ),
      opt[String]("anki-binary-path")
        .action((x, c) => c.copy(ankiBinaryPath = x))
        .optional()
        .maxOccurs(1)
        .text(
          "Path to Anki binary. It will be triggered to run if Anki Connect port will not respond"
        ),
      opt[Int]("max-cards-to-add")
        .action((x, c) => c.copy(maxCardsToAdd = x))
        .optional()
        .maxOccurs(1)
        .text(
          s"The maximum number of cards that should be added to the deck. $ARG_MAX_CARDS as default"
        ),
      opt[Int]("max-unlearned-cards")
        .abbr("max")
        .action((x, c) => c.copy(maxUnlearnedCards = Some(x)))
        .optional()
        .maxOccurs(1)
        .text(
          "The number of unlearned cards in the deck, which should not be exceeded (taking into account the existing ones).\nThis parameter (if it is less) will take precedence over the previous one"
        ),
      arg[File]("<input_file>")
        .required()
        .maxOccurs(1)
        .action((x, c) => c.copy(file = Some(x)))
        .text("Input file with word list"),
      help("help").text("Print help and exit")
    )
  }

  case class Config(
      maxUnlearnedCards: Option[Int] = None,
      maxCardsToAdd: Int = ARG_MAX_CARDS,
      ankiBinaryPath: String = ARG_ANKI_BINARY_PATH,
      deckName: Option[String] = None,
      file: Option[File] = None
  )
}
