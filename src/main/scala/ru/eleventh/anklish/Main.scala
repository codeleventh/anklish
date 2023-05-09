package ru.eleventh.anklish

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.slf4j.LoggerFactory
import ru.eleventh.anklish.Const._
import scopt.OParser

import scala.io.Source

object Main extends IOApp {

  private val client: Client[IO] = JavaNetClientBuilder[IO].create
  private val logger             = LoggerFactory.getLogger(this.getClass)

  private def getDefinition(word: String, retries: Int)(
      config: Args.Config
  ): IO[Either[Throwable, DictResponse]] =
    client
      .expect[List[DictResponse]](s"$NET_DICT_API_URL/$word")
      .map(_.head)
      .flatTap(res =>
        IO(logger.info(s"$word: ${res.meanings.head.definitions.head.definition.get}"))
      )
      .flatTap(_ => IO.sleep(NET_DELAY))
      .map(Right(_))
      .handleErrorWith(err =>
        retries match {
          case 0 => IO(Left(err))
          case _ =>
            IO(logger.error(s"$word: ${err.getMessage}")) *> getDefinition(word, retries - 1)(
              config
            )
        }
      )

  def run(args: List[String]): IO[ExitCode] = {
    val config: Args.Config = OParser.parse(Args.parser, args, Args.Config()).get
    val source              = Source.fromFile(config.files.head) // TODO:

    source
      .getLines()
      .filter(_.nonEmpty)
      .take(config.maxCardsToAdd)
      .toSeq
      .map(getDefinition(_, NET_RETRIES)(config))
      .sequence_
      .as(ExitCode.Success)
  }
}
