package ru.eleventh.anklish

import cats.effect.{IO, IOApp, Resource}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import org.slf4j.LoggerFactory
import cats.implicits._

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStream}

object Main extends IOApp.Simple {

  private val URL                = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  private val client: Client[IO] = JavaNetClientBuilder[IO].create
  private val logger             = LoggerFactory.getLogger("whatever")

  private def getDefinition(word: String): IO[DictResponse] = {
    client
      .expect[List[DictResponse]](s"$URL/$word")
      .map(_.head)
      .onError(e => IO(()).flatTap(_ => IO(logger.error(e.getMessage))))
  }

  def run: IO[Unit] = {
    Seq("hello", "music", "violin", "cutter", "kek")
      .map(word => {
        getDefinition(word).map(w =>
          logger.info(s"$word: ${w.meanings.head.definitions.head.definition.get}")
        )
      })
      .sequence_
  }
}
