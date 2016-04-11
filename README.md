# Biser
Binary serialization framework


## IO data format


### Primitives


#### Byte
8-bit signed integer (-128...127)

Format:
* `0x00...0xFF` > -128...127

Represent:
* java: `byte`
* flash: `int` (-128...127)


#### Boolean

Format:
* `0x00` > false
* `0x01` > true

Represent:
* java: `boolean`
* flash: `Boolean`


#### Integer
32-bit signed integer (-2 147 483 648 ... 2 147 483 647)

Format:
* `0x00...0xFD` > 0 ... 253
* `0xFF` > -1
* `0xFE [XXXX]` > XXXX - 4 bytes of integer raw code

Represent:
* java: `int`
* flash: `int`


#### Unsigned integer
32-bit unsigned integer (0 ... 4 294 967 295)

Format:
* `0x00...0xFE` > 0 ... 254
* `0xFF [XXXX]` > XXXX - 4 bytes of unsigned integer raw code

Represent:
* java: `int`
* flash: `uint`


#### Long integer
64-bit signed long integer (-9 223 372 036 854 775 808 ... 9 223 372 036 854 775 807)

Format:
* `0x00...0xFD` > 0 ... 253
* `0xFF` > -1
* `0xFE [XXXXXXXX]` > XXXXXXXX - 8 bytes of long integer raw code

Represent:
* java: `long`
* flash: `org.shypl.common.math.Long`


#### Unsigned long integer
64-bit unsigned long integer (0 ... 18 446 744 073 709 551 615)

Format:
* `0x00...0xFE` > 0 ... 254
* `0xFF [XXXXXXXX]` > XXXXXXXX - 8 bytes of long integer raw code

Represent:
* java: `long`
* flash: `org.shypl.common.math.Long`


#### Number
64-bit IEEE 754 double-precision floating point number

Format:
* `XXXXXXXX` > 8 bytes of double raw code

Represent:
* java: `double`
* flash: `Number`


#### String
UTF-8 string

Format:
* `[int] X*` > _int_ - string bytes size, _X*_ - bytes
* `0xFF` > _null_

Represent:
* java: `String`
* flash: `String`


#### Date
Date of unit timestamp

Format:
* `XXXXXXXX` > 8 bytes of raw long int (-1 = null)

Represent:
* java: `Date`
* flash: `Date`

#### Bytes
Sequence of bytes

Format:
* `[int] X*` > _int_ - sequence size, _X*_ - bytes
* `0xFF` > _null_

Represent:
* java: `byte[]`
* flash: `ByteArray`


### Structures


#### Enum

Format: 
* `[int]` > _int_ - ordinal of enum value
* `0xFF` > null

Represent:
* java: `Enum`
* flash: `org.shypl.common.lang.Enum`


#### Entity

Format: 
* `[int] [data bytes]` > _int_ - entity id, _data bytes_ - entity fields
* `0xFF` > null

Represent:
* java: `org.shypl.biser.io.Entity`
* flash: `org.shypl.biser.io.Entity`


### Collections


#### Array
Array of elements

Format: 
* `[int] [element]*` > _int_ - array size , _element*_ - elements
* `0xFF` > null

Represent:
* java: `Array`
* flash: `Vector`


#### Map
Map of key-value pairs

Format: 
* `[int] [[key] [value]]*` > _int_ - map size , _[[key] [value]]*_ - key-value pairs
* `0xFF` > null

Represent:
* java: `java.util.Map`
* flash: `org.shypl.common.collection.Map`


## Data model definition format 


### Configuration file format

	source: <path to model directory>
	modules:
		<module_1>:
			lang: <language>
			pack: <package name>
			target: <path to output directory>
			api:
				<api_name_1>: client | server
				<api_name_2>: client | server
				...
		<module_2>:
			...
	

### Data types


Primitives:

* Byte: `byte`
* Boolean: `bool`
* Integer: `int`
* Unsigned integer: `uint`
* Long integer: `long`
* Unsigned long integer: `ulong`
* Number: `double`
* String: `string`
* Bytes: `bytes`

Structures:

* Enum: `<enum_name> [ <value_1>, <value_2>, ..., <value_n>]`
* Entity: `<entity_name> { <property_1>: <type>, <property_2>: <type>, ..., <property_n>: <type> }`


Collections:

* Array: `*<type>`
* Map: `<<key_type>-<value_type>>`


### Examples

	EnumType_1 [
		VALUE_1
		VALUE_2
		VALUE_3
	]
	
	EntityType_1 {
		property_1: string
		property_2: EnumType_1
		property_3: *int
		property_4: <int-string>
		property_5: {
			sub_property_1: EntityType_1
		}
		property_6: [A, B]
		property_7: EntityType_1 {foo: int}
	}
	
	EntityType_2 EntityType_1 {
		bar: EnumType_1
	}
	
	^ api {
		user {
			> getFriends(limit: int): *User
		}
	}
	
	^ api.user {
		> getFriends(limit: int): *User 
	}