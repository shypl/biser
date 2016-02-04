# Shypl Biser IO data format


## Primitives


### Byte
8-bit signed integer (-128...127)

#### Format
* `0x00...0xFF` > -128...127

#### Represent
* Java: `byte`
* ActionScript: `int` (-128...127)


### Boolean

#### Format
* `0x00` > false
* `0x01` > true

#### Represent
* Java: `boolean`
* ActionScript: `Boolean`


### Integer
32-bit signed integer (-2 147 483 648 ... 2 147 483 647)

#### Format
* `0x00...0xFD` > 0 ... 253
* `0xFF` > -1
* `0xFE [XXXX]` > XXXX - 4 bytes of integer raw code

#### Represent
* Java: `int`
* ActionScript: `int`


### Unsigned integer
32-bit unsigned integer (0 ... 4 294 967 295)

#### Format
* `0x00...0xFE` > 0 ... 254
* `0xFF [XXXX]` > XXXX - 4 bytes of unsigned integer raw code

#### Represent
* Java: `long`
* ActionScript: `uint`


### Long integer
64-bit signed long integer (-9 223 372 036 854 775 808 ... 9 223 372 036 854 775 807)

#### Format
* `0x00...0xFD` > 0 ... 253
* `0xFF` > -1
* `0xFE [XXXXXXXX]` > XXXXXXXX - 8 bytes of long integer raw code

#### Represent
* Java: `long`
* ActionScript: `org.shypl.common.math.Long`


### Unsigned long integer
64-bit unsigned long integer (0 ... 18 446 744 073 709 551 615)

#### Format
As long

#### Represent
* Java: `long`
* ActionScript: `org.shypl.common.math.Long`


### Number
64-bit IEEE 754 double-precision floating point number

#### Format
* `XXXXXXXX` > 8 bytes of double raw code

#### Represent
* Java: `double`
* ActionScript: `Number`

### String
UTF-8 string

#### Format
* `[int] X*` > _int_ - string bytes size, _X*_ - bytes
* `0xFF` > _null_

#### Represent
* Java: `String`
* ActionScript: `String`

### Date

#### Format
* `XXXXXXXX` > 8 bytes of timestamp microseconds
* `0x8000000000000000` > _null_

#### Represent
* Java: `java.util.Date`
* ActionScript: `Date`

### Bytes
Sequence of bytes

#### Format
* `[int] X*` > _int_ - sequence size, _X*_ - bytes
* `0xFF` > _null_

#### Represent
* Java: `byte[]`
* ActionScript: `ByteArray`


## Structures

### Enum

#### Format 
* `[int]` > _int_ - ordinal of enum value
* `0xFF` > null

#### Represent
* Java: `Enum`
* ActionScript: `org.shypl.common.lang.Enum`


### Entity

#### Format 
* `[int] [data bytes]` > _int_ - entity id, _data bytes_ - entity fields
* `0xFF` > null

#### Represent
* Java: `org.shypl.biser.io.Entity`
* ActionScript: `org.shypl.biser.io.Entity`


## Collections


### Array
Array of elements

#### Format 
* `[int] [element]*` > _int_ - array size , _element*_ - elements
* `0xFF` > null

#### Represent
* Java: `Array`
* ActionScript: `Vector`


### Map
Map of key-value pairs

#### Format 
* `[int] [[key] [value]]*` > _int_ - map size , _[[key] [value]]*_ - key-value pairs
* `0xFF` > null

#### Represent
* Java: `java.util.Map`
* ActionScript: `org.shypl.common.collection.Map`
