/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kara.generate

import org.jetbrains.kara.generate.*
import java.util.Collections
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType
import java.util.Comparator
import java.util.ArrayList

fun <T>Collection<T>.sort(compare: (o1: T, o2: T) -> Int): List<T> {
    return this.sort(object :Comparator<T> {
        public override fun compare(o1: T, o2: T): Int {
            return compare(o1, o2)
        }
    })
}

public inline fun <T: Comparable<T>> Iterable<T>.sort() : List<T> {
    val list = toCollection(ArrayList<T>())
    java.util.Collections.sort(list)
    return list
}

class AttributeGroupImp(name: String,
                        newAttributes: Collection<AttributeDeclaration>,
                        parentGroups: Collection<AttributeGroup>): AttributeGroup {
    override val name: String = name
    override val newAttributes: List<AttributeDeclaration> = newAttributes.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val parentGroups: List<AttributeGroup> = parentGroups.sort({(a,b) -> a.name.compareTo(b.name)} )
}


class MutableAttributeTypeDeclaration(attrType: AttributeTypeDeclaration.AttributeType,
                                      name: String,
                                      elementName: String? = null,
                                      values: Collection<String> = Collections.emptyList()
): AttributeTypeDeclaration {
    override val name: String = name
    override val attrType: AttributeTypeDeclaration.AttributeType = attrType
    override val values: List<String> = values.sort()

    var mutableElementName: String? = elementName
    override val elementName: String?
        get() {
            return mutableElementName
        }

    fun setElementName(elementName: String?) {
        mutableElementName = elementName
    }

    fun equalsType(other: MutableAttributeTypeDeclaration): Boolean {
        var equalValues = false;
        if (values.size == other.values.size) {
            equalValues = other.values.all { values.contains(it) }
        }
        return other.attrType == attrType && other.name == name && equalValues
    }
}


public enum class SimpleAttributeTypeDeclaration(attrType: AttributeType): AttributeTypeDeclaration {
    override val name: String = attrType.name()
    override val attrType: AttributeTypeDeclaration.AttributeType = attrType
    override val elementName: String? = null
    override val values: List<String> = Collections.emptyList()

    dateTime : SimpleAttributeTypeDeclaration(AttributeType.dateTime)
    float : SimpleAttributeTypeDeclaration(AttributeType.float)
    integer : SimpleAttributeTypeDeclaration(AttributeType.integer)
    positiveInteger : SimpleAttributeTypeDeclaration(AttributeType.positiveInteger)
    boolean : SimpleAttributeTypeDeclaration(AttributeType.boolean)
    string : SimpleAttributeTypeDeclaration(AttributeType.string)
    ticker : SimpleAttributeTypeDeclaration(AttributeType.ticker)
    anyUri : SimpleAttributeTypeDeclaration(AttributeType.anyUri)
}


class AttributeDeclarationImpl(override val name: String,
                               override val attrTypeDeclaration: AttributeTypeDeclaration,
                               override val defaultValue: String? = null
): AttributeDeclaration


open class CommonElementDeclaration(name: String,
                                    allowText: Boolean = false,
                                    elementGroups: Collection<ElementGroupDeclaration> = Collections.emptyList(),
                                    newAllowElements: Collection<ElementDeclaration> = Collections.emptyList(),
                                    attributeGroups: Collection<AttributeGroup> = Collections.emptyList(),
                                    newAttributes: Collection<AttributeDeclaration> = Collections.emptyList()
): ElementGroupDeclaration, ElementDeclaration {
    override val name: String = name
    override val allowText: Boolean = allowText
    override val elementGroups: List<ElementGroupDeclaration> = elementGroups.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val newAllowElements: List<ElementDeclaration> = newAllowElements.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val attributeGroups: List<AttributeGroup> = attributeGroups.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val newAttributes: List<AttributeDeclaration> = newAttributes.sort({(a,b) -> a.name.compareTo(b.name)} )
}


class SpecialGroupDeclaration(name: String, elementGroups: Collection<ElementGroupDeclaration>,
                              val newAllowElementsFun: () -> Collection<ElementDeclaration>
): CommonElementDeclaration(name, false, elementGroups) {
    private var realNewAllowElements: List<ElementDeclaration>? = null;

    override val newAllowElements: List<ElementDeclaration>
        get() {
            if (realNewAllowElements == null) {
                realNewAllowElements = newAllowElementsFun().sort({(a,b) -> a.name.compareTo(b.name)} )
            }
            return realNewAllowElements!!
        }
}

class HtmlModelImpl(
        attributeDeclarations: Collection<AttributeTypeDeclaration>,
        attributeGroups: Collection<AttributeGroup>,
        simpleElementDeclarations: Collection<ElementDeclaration>,
        groupElementDeclaration: Collection<ElementGroupDeclaration>
): HtmlModel {
    override val attributeDeclarations: List<AttributeTypeDeclaration> = attributeDeclarations.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val attributeGroups: List<AttributeGroup> = attributeGroups.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val simpleElementDeclarations: List<ElementDeclaration> = simpleElementDeclarations.sort({(a,b) -> a.name.compareTo(b.name)} )
    override val groupElementDeclaration: List<ElementGroupDeclaration> = groupElementDeclaration.sort({(a,b) -> a.name.compareTo(b.name)} )
}
