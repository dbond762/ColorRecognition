# ColorRecognition
Andoroid приложение для голосового управления устройствами, подключёнными к Arduino

[Ссылка на APK файл для установки на Android-устройство](https://github.com/dbond762/ColorRecognition/releases)

## Опсисание работы приложения

1. После нажатия на кнопку «Подключиться» появится список сопряженных Bluetooth устройств.
1. После выбора необходимого Bluetooth модуля программа переключается на MainActivity  и инициирует соединение. После установления
соединения кнопка «Распознавание команды» станет активной.
1. При нажатии на кнопку «Распознавание команды» с помощью механизма Intent вызывается Активити, выполняющее прослушивание
произнесенной фразы с последующей передачей ее сервису распознавания речи. Если работа производится с серверами через Интернет
результаты распознавания фраз будут значительно лучше. Для решения этой части задачи используется класс RecognizerIntent.
1. Если первым словом было «включить», то выполняется запрос к базе данных для поиска необходимого цвета. Если цвет найден, то он
будет передан по Bluetooth на устройство. Если первым словом было «трек», а второе — его номер, то на устройство будет подана
команда на включение данного трека.
1. На срабатывание датчика движения приложение отвечает определенными фразами. Для синтеза речи используется класс TextToSpeech.
