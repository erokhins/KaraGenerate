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


trait Builder {

}


trait Attribute {
    public enum class AttributeType {
        ticker
        dateTime
        float
        positiveInteger
        boolean
        string
        anyUri
        enumType
        stringEnumType
    }

}

trait AttributeDeclaration {
    fun getName(): String

    // return null, if name of Attribute is unique
    fun getElementName(): String? {
        return null;
    }
}

open class EnumAttributeDeclaration(val name: String, val enumList: Set<String>, val elementName: String? = null):
            AttributeDeclaration {

    override fun getName(): String {
        return name
    }

    override fun getElementName(): String? {
        return elementName
    }

}

class StringEnumAttributeDeclaration(name: String, enumList: Set<String>, elementName: String? = null):
        EnumAttributeDeclaration(name, enumList, elementName) {

}


trait AttributeBuilder {


    fun attributeGroup(name: String, attributes: Collection<AttributeDeclaration>)
}








