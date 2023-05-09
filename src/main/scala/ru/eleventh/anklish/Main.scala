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
  private val RETRIES = 3

  private def getDefinition(word: String, retries: Int): IO[Either[Throwable, DictResponse]] =
    client
      .expect[List[DictResponse]](s"$DICT_API_URL/$word")
      .map(_.head)
      .flatTap(res => IO(logger.info(res.meanings.head.definitions.head.definition.get)))
      .map(Right(_))
      .handleErrorWith(err => retries match {
        case 0 => IO(Left(err))
        case _ => IO(logger.error(s"$word: ${err.getMessage}")) *> getDefinition(word, retries - 1)
      })

  def run(args: List[String]): IO[ExitCode] = {
    val source = Source.fromFile(FILE_PATH)
    source
      .getLines()
      .filter(_.nonEmpty)
      .take(MAX_WORDS_TO_ADD)
      .toSeq
      .map(IO.sleep(0.5.second) *> getDefinition(_, RETRIES))
      .sequence_
      .as(ExitCode.Success)
  }
}
