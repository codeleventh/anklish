package ru.eleventh.anklish


import cats.effect.{IO, IOApp}
import cats.effect.unsafe.IORuntime
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._

object Main extends IOApp.Simple {

  private val URL = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  private val client: Client[IO] = JavaNetClientBuilder[IO].create

  def run: IO[Unit] = {
    val res = client.expect[String](s"$URL/hello").onError(e => {
      println(e.getMessage)
      IO(())
    }).unsafeRunSync()(runtime)

    IO(res).map { body =>
      println(body)
      ()
    }
  }
}
