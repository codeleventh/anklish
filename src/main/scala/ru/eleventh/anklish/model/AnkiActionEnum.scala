package ru.eleventh.anklish.model

object AnkiActionEnum extends Enumeration {
  type AnkiActionEnum = String

  val DeckNamesAndIds = "deckNamesAndIds"
  val GetDeckStats = "getDeckStats"
  val AddNote = "addNote"
}
