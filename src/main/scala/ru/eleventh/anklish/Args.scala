package ru.eleventh.anklish

import ru.eleventh.anklish.Const._
import scopt.{OParser, OParserBuilder}

import java.io.File

object Args {

  private val builder: OParserBuilder[Config] = OParser.builder[Config]

  case class Config(
      maxUnlearnedCards: Int = Int.MaxValue, // TODO:
      maxCardsToAdd: Int = ARG_MAX_CARDS,
      ankiPath: String = ARG_ANKI_PATH, // TODO:
      deck: Option[String] = None,      // TODO:
      files: Seq[File] = Seq.empty
  )

  val parser: OParser[Unit, Config] = {
    import builder._
    OParser.sequence(
      programName("anklish"),
      head("Scala script for adding English words to Anki decks"),
      head("Check the GitHub repository for more info: https://github.com/codeleventh/anklish"),
      opt[Int]("max-cards-to-add")
        .action((x, c) => c.copy(maxCardsToAdd = x))
        .optional()
        .maxOccurs(1)
        .text(
          s"The maximum number of cards that should be added to the deck. $ARG_MAX_CARDS as default"
        ),
      opt[Int]("max-unlearned-cards")
        .action((x, c) => c.copy(maxUnlearnedCards = x))
        .optional()
        .maxOccurs(1)
        .text(
          "The number of unlearned cards in the deck, which should not be exceeded (taking into account the added ones). Takes precedence over the previous argument"
        ),
      opt[String]("anki-path")
        .action((x, c) => c.copy(ankiPath = x))
        .optional()
        .maxOccurs(1)
        .text(
          "Path to Anki binary. It will be triggered to run if Anki Connect port will not respond"
        ),
      opt[String]("deck")
        .action((x, c) => c.copy(deck = Some(x)))
        .optional()
        .maxOccurs(1)
        .text(
          "Deck for adding the cards. If no deck specified, the default deck will be used"
        ),
      arg[File]("<input_file>...")
        .required()
        .unbounded()
        .action((x, c) => c.copy(files = c.files :+ x))
        .text("Input file(s) with word list"),
      help("help").text("Print help and exit")
    )
  }
}
