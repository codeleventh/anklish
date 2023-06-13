package ru.eleventh.anklish.model

import io.circe.generic.JsonCodec
import ru.eleventh.anklish.model.AnkiActionEnum.AnkiActionEnum

@JsonCodec
case class RequestDeckNamesAndIds(
    action: AnkiActionEnum = AnkiActionEnum.DeckNamesAndIds
) extends AnkiRequest[Unit](action = action, params = ())
