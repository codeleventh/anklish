package ru.eleventh.anklish

import cats.effect.IO
import io.circe.Decoder
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{Method, Request, Uri}
import org.slf4j.Logger
import ru.eleventh.anklish.Args.Config
import ru.eleventh.anklish.Const.ANKI_CONNECT_URL
import ru.eleventh.anklish.model._

case class AnkiConnectClient(config: Config)(implicit httpClient: Client[IO], logger: Logger) {

  private val ankiRequest = Request[IO]()
    .withMethod(Method.POST)
    .withUri(Uri.unsafeFromString(s"$ANKI_CONNECT_URL"))

  def getVersion: IO[Int] = for {
    res <- httpClient.expect[String](s"$ANKI_CONNECT_URL")
    majorVersion <- IO("AnkiConnect v\\.(.+)\\.?".r.findFirstMatchIn(res) match {
      case Some(m) => m.group(1)
      case None => throw new RuntimeException("Can't parse AnkiConnect version")
    }).map(_.toInt)
  } yield majorVersion

  def getDeckStats(deckName: String): IO[Option[DeckStat]] = {
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
    implicit val decoder: Decoder[Seq[DeckStat]] = _.as[Map[String, DeckStat]].map(_.values.toSeq)

    httpClient
      .expect[Seq[DeckStat]](
        ankiRequest.withEntity(RequestGetDeckStats(ParamsGetDeckStats(Seq(deckName))).asJson)
      )
      .onError(e => IO(logger.error(e.getMessage)))
      .map(_.headOption)
  }

  def deckNamesAndIds: IO[Map[String, Long]] = {
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

    httpClient
      .expect[Map[String, Long]](ankiRequest.withEntity(RequestDeckNamesAndIds()))
      .onError(e => IO(logger.error(e.getMessage)))
  }

  def addNote(deckName: String, card: (String, String)): IO[Long] = {
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

    httpClient
      .expect[AnkiResponse[Long]](
        ankiRequest.withEntity(
          RequestAddNote(
            ParamsAddNote(
              Note(
                deckName,
                if (config.reversible) "Basic (and reversed card)" else "Basic",
                Fields(card._1, card._2)
              )
            ),
            Options(allowDuplicate = false, duplicateScope = "deck")
          )
        )
      )
      .flatMap(_.fromOptions)
  }
}
