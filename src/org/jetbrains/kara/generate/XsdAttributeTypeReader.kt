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

import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*
import org.jetbrains.kara.generate.AttributeDeclaration
import org.jetbrains.kara.generate.AttributeTypeDeclaration
import org.jetbrains.kara.generate.Cache
import com.sun.xml.xsom.*
import java.util.HashSet
import java.util.HashMap
import java.util.ArrayList
import org.jetbrains.kara.generate.getProcessedCollection
import org.jetbrains.kara.generate.test.makeStr
import java.util.Collections


val XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema"

private fun nameToType(name: String): SimpleAttributeTypeDeclaration? {
    return when (name) {
        "boolean" -> SimpleAttributeTypeDeclaration.boolean
        "anyURI" -> SimpleAttributeTypeDeclaration.anyUri
        "anySimpleType" -> SimpleAttributeTypeDeclaration.string
        "string" -> SimpleAttributeTypeDeclaration.string
        "dateTime" -> SimpleAttributeTypeDeclaration.dateTime
        "float" -> SimpleAttributeTypeDeclaration.float
        "positiveInteger" -> SimpleAttributeTypeDeclaration.positiveInteger
        "integer" -> SimpleAttributeTypeDeclaration.integer

        else -> null
    }
}

private fun detectXsSimpleType(xsType: XSSimpleType): SimpleAttributeTypeDeclaration? {
    if (xsType.getTargetNamespace() == XSD_NAMESPACE) {
        return nameToType(xsType.getName()!!)
    } else {
        return null
    }
}


private class DuplicateNameController<T>(val isEquals: (o1: T, o2: T) -> Boolean) {
    val elements = HashMap<String, MutableSet<T>>()

    fun isDuplicateName(name: String): Boolean {
        val set = elements.get(name)
        if (set != null && set.size > 1) {
            return true
        }
        return false
    }

    fun getAllElements(runUniqueName: T.() -> Unit): Collection<T> {
        val allElements = ArrayList<T>()
        for (set in elements.values()) {
            if (set.size == 1) {
                val element = set.first()!!
                element.runUniqueName()
                allElements.add(element)
            } else {
                allElements.addAll(set)
            }
        }
        return allElements
    }

    fun getEqualsToThis(name: String, element: T): T {
        val set = elements.get(name)
        if (set != null) {
            for (el in set) {
                if (isEquals(element, el)) {
                    return el
                }
            }
            set.add(element)
        } else {
            val newSet = HashSet<T>()
            newSet.add(element)
            elements.put(name, newSet)
        }
        return element
    }
}


class AttributeTypeCache {
    val attrTypeDuplicateContr = DuplicateNameController<MutableAttributeTypeDeclaration>({(o1, o2) -> o1.equalsType(o2) })
    val attrDuplicateContr = DuplicateNameController<MutableAttributeDeclaration>(
            {(o1, o2) -> o1.attrTypeDeclaration.equalsType(o2.attrTypeDeclaration) })

    private fun parseNotXsSimpleTypeDeclaration(xsType: XSSimpleType, attributeDeclName: String, elementName: String? = null):
            MutableAttributeTypeDeclaration {

        val values: MutableList<String> = ArrayList<String>()
        var isStringEnum = false;
        if (xsType.isUnion()) {
            for (union in xsType.asUnion()!!) {
                // union must be Restriction
                val restriction = union.asRestriction()!!
                if (union.getBaseType().getName() == "string" && restriction.getDeclaredFacets().isEmpty()) {
                    isStringEnum = true
                }
                restriction.getDeclaredFacets().forEach { values.add(it!!.getValue()!!.value!!) }
            }
        } else {
            // now xsType must be restriction
            xsType.asRestriction()!!.getDeclaredFacets().forEach { values.add(it!!.getValue()!!.value!!) }
        }

        val attrName = xsType.getName() ?: attributeDeclName
        val typeDecl =
                if (isStringEnum) {
                    MutableAttributeTypeDeclaration(attrName, strEnumType, elementName, values)
                } else {
                    if (values.size == 1 && values.first == attrName) {
                        // ticker
                        MutableAttributeTypeDeclaration(attrName, ticker, elementName)
                    } else {
                        MutableAttributeTypeDeclaration(attrName, enumType, elementName, values)
                    }
                }

        if (xsType.getName() != null) {
            typeDecl.setElementName(null)
        }

        return typeDecl
    }

    fun getAttributeDeclaration(xsDecl: XSAttributeDecl, elementName: String): AttributeDeclaration {
        val attrTypeDecl = getAttributeTypeDeclaration(xsDecl.getType()!!, xsDecl.getName()!!, elementName)
        val attrDecl = MutableAttributeDeclaration(xsDecl.getName()!!, attrTypeDecl, elementName,
                xsDecl.getDefaultValue()?.value)
        return attrDuplicateContr.getEqualsToThis(xsDecl.getName()!!, attrDecl)
    }

    fun getAttributeTypeDeclaration(xsType: XSSimpleType, attributeDeclName: String, elementName: String? = null): AttributeTypeDeclaration {
        val typeDeclaration = detectXsSimpleType(xsType)
        if (typeDeclaration != null) {
            return typeDeclaration
        }

        var typeDecl = parseNotXsSimpleTypeDeclaration(xsType, attributeDeclName, elementName)
        if (typeDecl.attrType == ticker) {
            return SimpleAttributeTypeDeclaration.ticker
        }

        return attrTypeDuplicateContr.getEqualsToThis(typeDecl.name, typeDecl)
    }

    public fun getAllTypeDecl(): Collection<AttributeTypeDeclaration> {
        // must be run !!
        return attrTypeDuplicateContr.getAllElements { setElementName(null) }
    }

    public fun getAllDecl(): Collection<AttributeDeclaration> {
        // must be run !!
        return attrDuplicateContr.getAllElements { setElementName(null) }
    }


}