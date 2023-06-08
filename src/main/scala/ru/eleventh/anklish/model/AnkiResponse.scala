package ru.eleventh.anklish.model

import io.circe.generic.JsonCodec

@JsonCodec
case class AnkiResponse[A](result: Option[A], error: Option[String] = None) {
  def toEither: Either[String, A] = error match {
    case Some(e) => Left(e)
    case None => Right(result.get)
  }
}
