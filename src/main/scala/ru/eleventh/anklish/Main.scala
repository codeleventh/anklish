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

  private def formatCard(definition: DictResponse): (String, String) = ???

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
        case Right(suc) =>
          IO(
            logger.info(
              s"$word: ${suc.phonetics.headOption.map(_.text).getOrElse("/nəʊ fəʊˈnɛtɪks faʊnd/")}"
            )
          )
      }
  }

  def run(args: List[String]): IO[ExitCode] = {
    val config: Args.Config = OParser.parse(Args.parser, args, Args.Config()).get
    val source: Source = Source.fromFile(config.files.head) // TODO: use all of specified files
    val ankiClient: AnkiConnectClient = AnkiConnectClient(config)

    for {
      wordlist <- IO(
        source
          .getLines()
          .filter(_.nonEmpty)
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
      definitions = wordlist.map(getDefinition(_, NET_RETRIES)).toSeq.sequence
      _ <- definitions.map(
        _.collect({ case Right(res) => res }).map(definition =>
          ankiClient.addNote(config.deck.get, formatCard(definition))
        )
      )
    } yield ExitCode.Success
  }

}
