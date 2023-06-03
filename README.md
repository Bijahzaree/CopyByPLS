## ЭВМбз-22-1
## Plotnikov Aleksey

# CopyByPLS
### Version: Alpha

***CopyByPLS*** is designed to copy files from a playlist (.pls format) to another folder/storage.

How to use:

    javaw CopyByPLS "path-to-playlist.pls" "destination-folder" [/quiet]
or

    CopyByPLS.exe "path-to-playlist.pls" "destination-folder" [/quiet]
or

    java -jar CopyByPLS.jar "path-to-playlist.pls" "destination-folder" [/quiet]

This project uses:

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) [Launch4j](https://launch4j.sourceforge.net/)

### [Description in Russian]
## 	Общие сведения
**CopyByPLS** предназначено для копирования файлов, перечисленных в плейлисте (формат файла - ***.pls***) в отдельную папку или на другое хранилище.

***Alpha-версия*** требует установленной виртуальной машины ***Java*** (***JDK***, ***JRE*** или ***OpenJDK***) не ниже 14 версии.

## Функциональное назначение
Необходимость написания данной программы появилось из-за невозможности копирования библиотеки избранных треков в используемом медиа-плеере штатными средствами.
Для проигрования на другом устройстве, при достаточно большом количестве таковых треков, ручное копирование файлов не представляется возможном.

***CopyByPLS*** анализирует указанный pls-файл, формирует список на копирование и непосредственно копирует в новую папку или на другое устройство.

## Описание логической структуры
В связи с тем, что ***CopyByPLS*** находиться на стадии ***Alpha***-версии, логическая структура будет изменяться.

## Используемые технические средства
***CopyByPLS Alpha-версия*** использует Java не ниже 14 версии и разрабатывается как кроссплатформенное решение для работы как в Windows-среде, так и Unix-подобных системах.


## Вызов и загрузка
Для запуска ***CopyByPLS*** (***Alpha***-версия):

    javaw CopyByPLS "путь-до-файла-playlist.pls" "папка-назначения" [/quiet]
или

    CopyByPLS.exe "path-to-playlist.pls" "destination-folder" [/quiet]
или

    java -jar CopyByPLS.jar "path-to-playlist.pls" "destination-folder" [/quiet]
Параметр `/quiet` - необязательный, включает режим ***QuietMode*** (без вывода сообщений в консоль).

***CopyByPLS*** (***Alpha***-версия) по умолчанию создает лог-файл вывода ошибок " _err.log_ " и лог-файл " _out.log_ " в режиме ***QuietMode***

## Входные данные
Пример запуска ***CopyByPLS*** (***Alpha***-версия):

    javaw CopyByPLS .\playlist.pls ..\NewFolder [/quiet]
или

    CopyByPLS.exe "path-to-playlist.pls" "destination-folder" [/quiet]
или

    java -jar CopyByPLS.jar "path-to-playlist.pls" "destination-folder" [/quiet]
В качестве входных аргументов служат:

1. Путь и имя к .pls-файлу, в примере: `.\playlist.pls`. Подробнее о структуре входного .pls-файла - ниже.

1. Путь и название папки назначения, в примере: `..\NewFolder`.

1. (необязательный параметр) флаг `/quiet` включения ***QuietMode***.



## Характер, организация и предварительная подготовка входных данных
Примерная структура входного .pls-файла:
```
[playlist]
numberofentries=<число записей>
file1=<путь-к-файлу1>
title1=<Название трека1>
length1=<длина записи>
file2=<путь-к-файлу2>
title2=<Название трека2>
length2=<длина записи>
***
fileN=<путь-к-файлуN>
titleN=<Название трекаN>
lengthN=<длина записи>
```

***CopyByPLS*** (***Alpha***-версия) не интерпретирует и не копирует ссылки на треки, содержащиеся в архивах, контейнерах (.rar, .7z, .flac и пр.).
На запись вида (для примера):
```
fileX=<имяФайла.rar|путь-и-имяфайла-в-архиве>
fileY=<имяФайла.flac*7>
```
в лог-файле _err.log_ появится запись:
```
Illegal char <|> at index XX: имяФайла.rar|путь-и-имяфайла-в-архиве
Illegal char <*> at index YY: имяФайла.flac*7
```


## Выходные данные
***CopyByPLS*** предназначена для непосредственного копирования треков, указанных во входном .pls-файле в папку назначения.
***CopyByPLS*** (***Alpha***-версия) реализует единственный алгоритм копирования файлов - создание подпапок с именем " _Folder<_номерИндекса_>_ " и максимальным количеством в подпапке - 255 файлов.
Данный алгоритм реализован под конкретное устройство воспроизведения, накладывающие ограничения на структуру файлов.
В дальнейшем планируется организация прочих алгоритмов копирования.
