package ru.eleventh.anklish

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.slf4j.{Logger, LoggerFactory}
import ru.eleventh.anklish.Const._
import ru.eleventh.anklish.model.DictResponse
import scopt.OParser

import java.net.URLEncoder
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.sys.process.Process

object Main extends IOApp {

  implicit val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def formatCard(definition: DictResponse): (String, String) = (
    definition.word,
    definition.phonetics.headOption
      .map(_.text.getOrElse("/nəʊ fəʊˈnɛtɪks faʊnd/"))
      .get
  )

  private def getDefinition(word: String, retries: Int): IO[Either[Throwable, DictResponse]] = {
    httpClient
      .expect[List[DictResponse]](s"$NET_DICT_API_URL/${URLEncoder.encode(word, "UTF-8")}")
      .attempt
      .map {
        case Right(dictResponse) => Right(dictResponse.head)
        case Left(err) => Left(err)
      }
      .flatMap {
        case suc => IO(suc)
        case Left(err) =>
          retries match {
            case 0 => IO(Left(err))
            case _ => IO.sleep(NET_DELAY) *> getDefinition(word, retries - 1)
          }
      }
      .flatTap {
        case Left(err) => IO(logger.error(s"$word: ${err.getMessage}"))
        case Right(_) => IO(logger.info(s"Got definition for \"$word\""))
      }
  }

  def run(args: List[String]): IO[ExitCode] = {
    val config: Args.Config = OParser.parse(Args.parser, args, Args.Config()).get
    val source: Source = Source.fromFile(config.files.head) // TODO: use all of specified files
    val ankiClient: AnkiConnectClient = AnkiConnectClient(config)

    {
      for {
        wordlist <- IO(
          source
            .getLines()
            .filter(_.trim.nonEmpty)
            .take(config.maxCardsToAdd)
        )
        _ <- ankiClient.getVersion
          .recoverWith(_ =>
            IO(Process(config.ankiBinaryPath).run) >> IO.sleep(2.seconds) *> ankiClient.getVersion
          )
          .flatTap({
            case ANKI_CONNECT_VERSION => IO()
            case version =>
              IO(
                logger.warn(
                  s"Anki Connect major version is differs from target ($version != $ANKI_CONNECT_VERSION)"
                )
              )
          })
          .onError(err =>
            IO(println(s"Cannot connect to Anki Connect ($ANKI_CONNECT_URL): ${err.getMessage}"))
          )

        _ <- ankiClient.getDeckStats(config.deck.get)
        _ <- ankiClient.deckNamesAndIds

        definitions <- wordlist
          .map(getDefinition(_, NET_RETRIES))
          .toSeq
          .sequence
          .map(_.collect({ case Right(res) => res }))
        _ <- definitions
          .traverse_(definition => {
            val card = formatCard(definition)
            ankiClient
              .addNote(config.deck.get, card)
              .flatTap(_ => IO(logger.info(s"Note \"${card._1}\" was added")))
              .handleErrorWith(err => IO(logger.error(err.getMessage)))
          })
      } yield ()
    } *> IO(ExitCode.Success)
  }
}
