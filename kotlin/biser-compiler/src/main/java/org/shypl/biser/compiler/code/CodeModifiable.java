package org.shypl.biser.compiler.code;

import java.util.Comparator;

public interface CodeModifiable {
	Comparator<CodeModifiable> COMPARATOR = (o1, o2) -> o1.getModifier().compareTo(o2.getModifier());

	CodeModifier getModifier();
}
