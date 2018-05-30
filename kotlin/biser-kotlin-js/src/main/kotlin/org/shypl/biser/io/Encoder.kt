package org.shypl.biser.io

typealias Encoder<T> = (writer: DataWriter, value: T) -> Unit