package ru.eleventh.anklish

import cats.effect.{IO, IOApp}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import org.slf4j.LoggerFactory

object Main extends IOApp.Simple {

  private val URL                = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  private val client: Client[IO] = JavaNetClientBuilder[IO].create
  private val logger             = LoggerFactory.getLogger("whatever")

  private def getDefinition(word: String): IO[DictResponse] = {
    client
      .expect[List[DictResponse]](s"$URL/$word")
      .map(_.head)
      .onError(e => {
        logger.error(e.getMessage)
        IO(())
      })
  }

  def run: IO[Unit] = {
    Seq("hello", "music", "violin", "cutter", "kek")
      .map(word => {
        getDefinition(word).map(w =>
          logger.info(s"$word: ${w.meanings.head.definitions.head.definition.get}")
        )
      })
      .foreach(io => io.unsafeRunSync()(runtime))

    IO(())
  }
}
