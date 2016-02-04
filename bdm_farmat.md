# Shypl Biser data model definition format

## Configuration file format

	source: <path to model directory>
	modules:
		<module_1>:
			lang: <language>
			pack: <package name>
			target: <path to output directory>
			csi:
				<api_name_1>: client | server
				<api_name_2>: client | server
				...
		<module_2>:
			...
	

## Data types

### Primitives

* Byte: `byte`
* Boolean: `bool`
* Integer: `int`
* Unsigned integer: `uint`
* Long integer: `long`
* Unsigned long integer: `ulong`
* Number: `double`
* String: `string`
* Date: `date`
* Bytes: `bytes`

### Structures

* Enum: `<enum_name> [ <value_1>, <value_2>, ..., <value_n>]`
* Entity: `<entity_name> { <property_1>: <type>, <property_2>: <type>, ..., <property_n>: <type> }`

### Collections

* Array: `*<type>`
* Map: `<<key_type>-<value_type>>`

## Examples

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