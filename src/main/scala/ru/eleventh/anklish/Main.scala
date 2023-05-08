package ru.eleventh.anklish

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.io.Source

object Main extends IOApp {

  private val client: Client[IO] = JavaNetClientBuilder[IO].create
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val DICT_API_URL = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  private val FILE_PATH = "./src/main/resources/list.txt"
  private val MAX_WORDS_TO_ADD = 10

  private def getDefinition(word: String): IO[Either[Throwable, DictResponse]] =
    client
      .expect[List[DictResponse]](s"$DICT_API_URL/$word")
      .map(_.head)
      .map(Right(_))
      .handleError(Left(_))

  def run(args: List[String]): IO[ExitCode] = {
    val source = Source.fromFile(FILE_PATH)
    source
      .getLines()
      .filter(_.nonEmpty)
      .take(MAX_WORDS_TO_ADD)
      .toSeq
      .map(word => {
        IO.sleep(0.5.second) *>
          getDefinition(word).map({
            case Left(err) =>
              logger.error(s"$word: ${err.getMessage}")
            case Right(dict) =>
              logger.info(s"$word: ${dict.meanings.head.definitions.head.definition.get}")
          })
      })
      .sequence_
      .as(ExitCode.Success)
  }
}
