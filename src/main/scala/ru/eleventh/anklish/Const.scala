package ru.eleventh.anklish

import org.http4s.implicits.http4sLiteralsSyntax

import scala.concurrent.duration.{DurationDouble, FiniteDuration}

object Const {
  val NET_DICT_API_URL          = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  val NET_RETRIES               = 3
  val NET_DELAY: FiniteDuration = 0.5.second

  val ARG_MAX_CARDS = 3
  val ARG_ANKI_PATH = "anki"

  val ANKI_CONNECT_URL = "localhost:8765"
}
