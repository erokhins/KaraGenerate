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

package org.jetbrain.kara.generate


trait Attribute {
    public enum class AttributeType {
        dateTime
        float
        positiveInteger
        boolean
        string
        anyUri
        special // use AttributeDeclaration
    }

    fun getName(): String
    fun getType(): AttributeType

    // if type != special is null
    fun getAttributeDeclaration(): AttributeDeclaration? {
        return null
    }

}

trait AttributeDeclaration {
    fun getName(): String

    // return null, if name of Attribute is unique
    fun getElementName(): String?
}

abstract class AbstractAttributeDeclaration(val name: String, val elementName: String? = null) : AttributeDeclaration {
    override fun getName(): String {
        return name
    }
    override fun getElementName(): String? {
        return elementName
    }
}


class SimpleAttributeDeclaration(val attributeType: Attribute.AttributeType, name: String, elementName: String? = null):
AbstractAttributeDeclaration(name, elementName)

class TickerAttributeDeclaration(name: String, elementName: String? = null):
AbstractAttributeDeclaration(name, elementName)

open class EnumAttributeDeclaration(val enumList: Set<String>, name: String, elementName: String? = null):
AbstractAttributeDeclaration(name, elementName)

class StringEnumAttributeDeclaration(name: String, enumList: Set<String>, elementName: String? = null):
EnumAttributeDeclaration(enumList, name, elementName)


trait AttributeBuilder {

    fun createAttributeDeclaration(attributeDeclaration: AttributeDeclaration);

    // create abstract tag with this attributes
    fun createAttributeGroup(name: String, attributes: Collection<Attribute>)
}




trait Builder {

}





