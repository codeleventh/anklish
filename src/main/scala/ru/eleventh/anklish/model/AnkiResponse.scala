package ru.eleventh.anklish.model

import cats.effect.IO
import io.circe.generic.JsonCodec

@JsonCodec
case class AnkiResponse[A](result: Option[A] = None, error: Option[String] = None) {
  def fromOptions: IO[A] =
    IO.fromEither(result match {
      case Some(_) => Right(result.get)
      case None => Left(new RuntimeException(error.get))
    })
}
