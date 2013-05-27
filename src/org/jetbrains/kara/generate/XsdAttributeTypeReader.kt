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

import org.jetbrain.kara.generate.AttributeDeclaration
import org.jetbrain.kara.generate.AttributeTypeDeclaration
import org.jetbrain.kara.generate.Cache
import com.sun.xml.xsom.*
import java.util.HashSet


val XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema"


fun nameToType(name: String): SimpleAttributeTypeDeclaration? {
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

fun detectXsSimpleType(xsType: XSSimpleType): SimpleAttributeTypeDeclaration? {
    if (xsType.getTargetNamespace() == XSD_NAMESPACE) {
        return nameToType(xsType.getName()!!)
    } else {
        return null
    }
}

fun getAttributeTypeDeclaration(xsType: XSSimpleType, attributeDeclName: String, elementName: String? = null): AttributeTypeDeclaration {
    val typeDeclaration = detectXsSimpleType(xsType)
    if (typeDeclaration != null) {
        return typeDeclaration
    }
    if (xsType.isUnion()) {
        println(xsType.toString() + " " + attributeDeclName + elementName)
        for (union in xsType.asUnion()!!) {
            for (facet in union.asRestriction()!!.getDeclaredFacets()) {
                println("   " + facet!!.getValue())
            }
        }
    }
    // TODO: parse enum values
    return AttributeTypeDeclarationImpl(AttributeTypeDeclaration.AttributeType.enumType, attributeDeclName, elementName);
}

fun getAttributeDeclaration(xsDecl: XSAttributeDecl, elementName: String): AttributeDeclaration {
    val attrDecl = getAttributeTypeDeclaration(xsDecl.getType()!!, xsDecl.getName()!!, elementName)
    return AttributeDeclarationImpl(xsDecl.getName()!!, attrDecl)
}

class AttributeTypeCache {

}