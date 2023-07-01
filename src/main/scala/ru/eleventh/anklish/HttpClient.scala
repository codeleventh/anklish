package ru.eleventh.anklish

import cats.effect.IO
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.slf4j.{Logger, LoggerFactory}
import ru.eleventh.anklish.Const.{NET_DICT_API_URL, NET_WAIT_RETRY}
import ru.eleventh.anklish.model.DictResponse

import java.net.URLEncoder

object HttpClient {

  implicit val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
  implicit val logger: Logger         = LoggerFactory.getLogger(this.getClass)

  def getDefinition(word: String, retries: Int): IO[Either[Throwable, DictResponse]] = {
    httpClient
      .expect[List[DictResponse]](s"$NET_DICT_API_URL/${URLEncoder.encode(word, "UTF-8")}")
      .attempt
      .map {
        case Right(dictResponse) => Right(dictResponse.head)
        case Left(e)             => Left(e)
      }
      .flatMap {
        case Left(e) =>
          retries match {
            case 0 => IO(Left(e))
            case _ => IO.sleep(NET_WAIT_RETRY) *> getDefinition(word, retries - 1)
          }
        case success => IO(success)
      }
      .flatTap {
        case Left(e)  => IO(logger.error(s"Word \"$word\": ${e.getMessage}"))
        case Right(_) => IO(logger.info(s"Got definition for \"$word\""))
      }
  }
}
