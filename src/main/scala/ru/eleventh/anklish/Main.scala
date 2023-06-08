package ru.eleventh.anklish

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.slf4j.{Logger, LoggerFactory}
import ru.eleventh.anklish.Const._
import ru.eleventh.anklish.model.DictResponse
import scopt.OParser

import scala.io.Source

object Main extends IOApp {

  implicit val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def getDefinition(word: String, retries: Int): IO[Either[Throwable, DictResponse]] =
    httpClient
      .expect[List[DictResponse]](s"$NET_DICT_API_URL/$word")
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
          IO(logger.info(s"$word: ${suc.phonetics.head.text.getOrElse("/nəʊ fəʊˈnɛtɪks faʊnd/")}"))
      }

  def run(args: List[String]): IO[ExitCode] = {
    val config: Args.Config = OParser.parse(Args.parser, args, Args.Config()).get
    val source: Source = Source.fromFile(config.files.head) // TODO: use all specified files
    val ankiClient: AnkiConnectClient = AnkiConnectClient(config)

    val wordlist = source // TODO: make it IO
      .getLines()
      .filter(_.nonEmpty)
      .take(config.maxCardsToAdd)

    for {
      _ <- ankiClient.getVersion
      _ <- ankiClient.getDeckStats(config.deck.get)
      _ <- ankiClient.deckNamesAndIds
      _ <- wordlist.map(getDefinition(_, NET_RETRIES)).toSeq.sequence
      _ <- ankiClient.addNote(config.deck.get, "<emphasis>front</emphasis>", "<code>back</code>")
    } yield ExitCode.Success
  }
}
