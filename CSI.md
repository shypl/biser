# CSI
		
	B - Число закодированное в 1 байт
	I - Число закодированное в 4 байта
	S - Строка произвольной длины закодированная в utf8 байты (размер предшествует)
	X - Последовательность байт произвольной длины (размер предшествует)
		
## Package flags

	F_AUTHORIZATION                  = 0x01
	F_MESSAGE_RECEIVED               = 0x03
	F_MESSAGE                        = 0x02
	F_PING                           = 0x06
	F_RECOVERY                       = 0x07
	F_SERVER_SHUTDOWN_TIMEOUT        = 0x08
	F_CLOSE                          = 0x10
	F_CLOSE_SERVER_SHUTDOWN          = 0x11
	F_CLOSE_ACTIVITY_TIMEOUT_EXPIRED = 0x12
	F_CLOSE_AUTHORIZATION_REJECT     = 0x13
	F_CLOSE_RECOVERY_REJECT          = 0x14
	F_CLOSE_CONCURRENT               = 0x15
	F_CLOSE_PROTOCOL_BROKEN          = 0x16
	F_CLOSE_SERVER_ERROR             = 0x17

## Flow

### Authorization

После установки соединения клиент отправляет запрос на авторизацию, содержащий ключ авторизации

	F_AUTHORIZATION
	B - Размер кюча
	S - Ключ
	
#### Success

В случае успешной авторизации, сервер отправляет ответ, содержащий конфигурационный параметры и ключ сессии 
	
	F_AUTHORIZATION
	I - Время провеки актирности соедиения в секундах 
	I - Время ожидания востановления соедиения в секундах 
	B - Размер ключа 
	
#### Reject

В случае неуспешной авторизации, сервер отправляет флаг 

	F_CLOSE_AUTHORIZATION_REJECT
	
## Messaging
	
### Client > Server

