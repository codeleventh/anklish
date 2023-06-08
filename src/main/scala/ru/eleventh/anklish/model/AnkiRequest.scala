package ru.eleventh.anklish.model

import ru.eleventh.anklish.Const.ANKI_CONNECT_VERSION
import ru.eleventh.anklish.model.AnkiActionEnum.AnkiActionEnum

class AnkiRequest[A](params: A, action: AnkiActionEnum, version: Int = ANKI_CONNECT_VERSION)
