package ru.eleventh.anklish

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2.Compiler.Target.forConcurrent
import org.http4s.InvalidMessageBodyFailure
import org.slf4j.{Logger, LoggerFactory}
import ru.eleventh.anklish.Const._
import ru.eleventh.anklish.HttpClient.{getDefinition, httpClient}
import ru.eleventh.anklish.model.{DeckStat, DictResponse}
import scopt.OParser

import java.nio.file.{Files, Paths}
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.math.min
import scala.sys.process.Process

object Main extends IOApp {

  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def runAnki(
      ankiBinaryPath: String
  )(implicit ankiClient: AnkiConnectClient): IO[Unit] = {
    ankiClient.getVersion
      .recoverWith(_ =>
        IO(Process(ankiBinaryPath).run) *> IO.sleep(3.seconds) *> ankiClient.getVersion
      )
      .flatMap {
        case ANKI_CONNECT_VERSION => IO.unit
        case version =>
          IO(
            logger.warn(
              s"Anki Connect major version is differs from the target ($version != $ANKI_CONNECT_VERSION)"
            )
          ).as()
      }
      .orRaise(new RuntimeException(s"Cannot connect to Anki Connect ($ANKI_CONNECT_URL)"))
  }
  private def getActiveDeck(
      deckName: Option[String]
  )(implicit ankiClient: AnkiConnectClient): IO[DeckStat] =
    for {
      allDecks <- ankiClient.deckNamesAndIds
      deckId <- deckName match {
        case Some(deckName) =>
          IO.fromOption(allDecks.get(deckName))(
            new RuntimeException(s"There is no \"$deckName\" deck")
          )
        case None => IO(DEFAULT_DECK_ID)
      }
      deckName <- IO(
        allDecks
          .map(_.swap)
          .get(deckId: Long)
      )
      deck <- ankiClient.getDeckStats(deckName.get)
    } yield deck.get

  private def addDefinition(deckName: String)(word: String)(implicit
      ankiClient: AnkiConnectClient
  ): IO[Boolean] =
    IO.sleep(NET_WAIT_RETRY) *> getDefinition(word, NET_RETRIES)
      .flatMap {
        case Right(definition) =>
          val card = formatCard(definition)
          ankiClient
            .addNote(deckName, card)
            .flatTap(_ => IO(logger.info(s"Card \"${card._1}\": note was added")))
            .as(true)
            .handleErrorWith {
              case err: InvalidMessageBodyFailure
                  if err.getMessage.contains("Could not decode JSON") =>
                IO(logger.info(s"Card \"$word\": note was added")) *> IO(true)
              // TODO: fix decoder
              case err if err.getMessage.contains("cannot create note because it is a duplicate") =>
                IO(logger.warn(s"Card \"$word\": ${err.getMessage}")) *> IO(false)
              case err =>
                IO(logger.error(s"Card \"$word\": ${err.getMessage}")) *> IO(false)
            }
        case Left(_) => IO(false)
      }

  private def dropUntilSucceededN[A](
      list: List[A],
      n: Long,
      f: A => IO[Boolean],
      acc: List[A] = List.empty
  ): IO[List[A]] = n match {
    case 0 => IO(acc ++ list)
    case _ =>
      list match {
        case Nil => IO(acc ++ list)
        case head +: tail =>
          f(head) >>= {
            case true  => dropUntilSucceededN(tail, n - 1, f, acc)
            case false => dropUntilSucceededN(tail, n, f, acc appended head)
          }
      }
  }

  private def formatCard(dict: DictResponse): (String, String) = (
    dict.word + dict.phonetic.map("<br/>" + _).getOrElse(""),
    dict.meanings
      .map(meaning => {
        val definitions =
          meaning.definitions.zipWithIndex.map { case (d, i) => s"${i + 1}. ${d.definition}" }
        s"<i>${meaning.partOfSpeech}</i><br/>${definitions.mkString("<br/>")}"
      })
      .mkString("<br/><br/>")
  )

  def run(args: List[String]): IO[ExitCode] = {
    val config: Args.Config                    = OParser.parse(Args.parser, args, Args.Config()).get
    implicit val ankiClient: AnkiConnectClient = AnkiConnectClient(config)

    for {
      inputFile <- IO.fromOption(config.file)(new RuntimeException("Input file wasn't specified"))
      source = Source.fromFile(inputFile)

      _          <- runAnki(config.ankiBinaryPath)
      activeDeck <- getActiveDeck(config.deckName)
      activeDeckName = activeDeck.name
      _              = logger.info(s"Using deck \"$activeDeckName\"")

      cardsToAdd = (config.maxCardsToAdd, config.maxUnlearnedCards) match {
        case (max, None)               => max
        case (max, Some(maxUnlearned)) => min(max, maxUnlearned - activeDeck.learn_count)
      }
      _ <- IO.whenA(cardsToAdd <= 0)(
        IO.raiseError(
          new RuntimeException("No cards should be added at this time due to 'max' argument")
        )
      )
      _ = logger.info(s"Cards to add: $cardsToAdd")

      wordlist <- IO(source).bracket(src =>
        IO(src.getLines().map(_.trim).filter(_.nonEmpty).toList)
      )(source => IO(source.close()))
      leftovers <- dropUntilSucceededN(wordlist, cardsToAdd, addDefinition(activeDeckName))

      inputFilePath = Paths.get(inputFile.getAbsolutePath)
      tmpFilePath   = Paths.get(inputFilePath + ".tmp")
      _ <- IO(new java.io.PrintWriter(tmpFilePath.toFile)).bracket(writer => {
        IO(writer.write(leftovers.mkString(System.lineSeparator))) *> IO(writer.flush())
      })(writer => IO(writer.close()))

      _ = Files.delete(inputFilePath)
      _ = Files.move(tmpFilePath, inputFilePath)
    } yield ()
  }.as(ExitCode.Success)
}
