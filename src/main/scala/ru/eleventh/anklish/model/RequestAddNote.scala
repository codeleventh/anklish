package ru.eleventh.anklish.model

import io.circe.generic.JsonCodec
import ru.eleventh.anklish.model.AnkiActionEnum.AnkiActionEnum

@JsonCodec
case class Options(allowDuplicate: Boolean, duplicateScope: String)

@JsonCodec
case class Fields(Front: String, Back: String)

@JsonCodec
case class Note(
                 deckName: String,
                 modelName: String,
                 fields: Fields
               )

@JsonCodec
case class ParamsAddNote(note: Note)

@JsonCodec
case class RequestAddNote(
                           params: ParamsAddNote,
                           options: Options,
                           action: AnkiActionEnum = AnkiActionEnum.AddNote
                         ) extends AnkiRequest[ParamsAddNote](params, action)
