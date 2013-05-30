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

import org.jetbrain.kara.generate.AttributeTypeDeclaration.AttributeType.*
import org.jetbrain.kara.generate.AttributeDeclaration
import org.jetbrain.kara.generate.AttributeTypeDeclaration
import org.jetbrain.kara.generate.Cache
import com.sun.xml.xsom.*
import java.util.HashSet
import java.util.HashMap
import java.util.ArrayList
import org.jetbrain.kara.generate.getProcessedCollection
import org.jetbrains.kara.generate.test.makeStr


val XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema"

private fun nameToType(name: String): SimpleAttributeTypeDeclaration? {
    return when (name) {
        "boolean"           -> SimpleAttributeTypeDeclaration.boolean
        "anyURI"            -> SimpleAttributeTypeDeclaration.anyUri
        "anySimpleType"     -> SimpleAttributeTypeDeclaration.string
        "string"            -> SimpleAttributeTypeDeclaration.string
        "dateTime"          -> SimpleAttributeTypeDeclaration.dateTime
        "float"             -> SimpleAttributeTypeDeclaration.float
        "positiveInteger"   -> SimpleAttributeTypeDeclaration.positiveInteger
        "integer"           -> SimpleAttributeTypeDeclaration.integer

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




class AttributeTypeCache {
    private val cacheNotAnonymousType: MutableMap<String, MutableAttributeTypeDeclaration> = HashMap()

    private val duplicateAttrName: MutableSet<String> = HashSet()
    private val attrNameMap: MutableMap<String, MutableAttributeTypeDeclaration> = HashMap()

    private val allDecl = ArrayList<MutableAttributeTypeDeclaration>();

    private fun parseNotXsSimpleTypeDeclaration(xsType: XSSimpleType, attributeDeclName: String, elementName: String? = null):
            MutableAttributeTypeDeclaration {
        val wasType = cacheNotAnonymousType.get(xsType.getName());
        if (wasType != null) {
            return wasType
        }

        val values: MutableList<String> = ArrayList<String>()
        var isStringEnum = false;
        if (xsType.isUnion()) {
            for (union in xsType.asUnion()!!) { // union must be Restriction
                val restriction = union.asRestriction()!!
                if (union.getBaseType().getName() == "string" && restriction.getDeclaredFacets().isEmpty()) {
                    isStringEnum = true
                }
                restriction.getDeclaredFacets().forEach { values.add(it!!.getValue()!!.value!!) }
            }
        } else { // now xsType must be restriction
            xsType.asRestriction()!!.getDeclaredFacets().forEach { values.add(it!!.getValue()!!.value!!) }
        }

        val attrName = xsType.getName() ?: attributeDeclName
        val typeDecl =
            if (isStringEnum) {
                MutableAttributeTypeDeclaration(strEnumType, attrName, elementName, values)
            } else {
                if (values.size == 1 && values.first == attrName) {
                    MutableAttributeTypeDeclaration(ticker, attrName, elementName)
                } else {
                    MutableAttributeTypeDeclaration(enumType, attrName, elementName, values)
                }
            }

        if (xsType.getName() != null) {
            typeDecl.setElementName(null)
            cacheNotAnonymousType.put(xsType.getName()!!, typeDecl)
        }

        return typeDecl
    }

    fun getAttributeDeclaration(xsDecl: XSAttributeDecl, elementName: String): AttributeDeclaration {
        val attrDecl = getAttributeTypeDeclaration(xsDecl.getType()!!, xsDecl.getName()!!, elementName)
        return AttributeDeclarationImpl(xsDecl.getName()!!, attrDecl, xsDecl.getDefaultValue()?.value)
    }

    fun getAttributeTypeDeclaration(xsType: XSSimpleType, attributeDeclName: String, elementName: String? = null): AttributeTypeDeclaration {
        val typeDeclaration = detectXsSimpleType(xsType)
        if (typeDeclaration != null) {
            return typeDeclaration
        }

        var typeDecl = parseNotXsSimpleTypeDeclaration(xsType, attributeDeclName, elementName)
        val wasType = attrNameMap.get(typeDecl.name)
        if (wasType == null) {
            attrNameMap.put(typeDecl.name, typeDecl);
        } else {
            if (wasType.equalsType(typeDecl)) {
                typeDecl = wasType;
            } else {
                duplicateAttrName.add(typeDecl.name)
            }
        }
        if (wasType !== typeDecl) { // if new type
            allDecl.add(typeDecl)
        }

        return typeDecl
    }

    public fun getAllDecl(): Collection<AttributeTypeDeclaration> { // must be run !!
        return allDecl.getProcessedCollection {
            if (!duplicateAttrName.contains(it.name)) {
                it.setElementName(null)
            }
            it
        }
    }


}