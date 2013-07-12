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
package org.jetbrains.kara.generate.templates

import org.jetbrains.kara.generate.*
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*
import org.jetbrains.kara.generate.AttributeTypeDeclaration
import java.util.ArrayList
import org.jetbrains.kara.generate.AttributeGroup
import org.jetbrains.kara.generate.StrBuilder
import org.jetbrains.kara.generate.toExtendString



val AttributeDeclaration.propertyName: String
    get() {
        return AttributeSafeName(name, elementName).safeName
    }
val AttributeDeclaration.typeName: String
    get() {
        return this.attrTypeDeclaration.typeName
    }

val AttributeTypeDeclaration.className: String
    get() {
        val safe = AttributeSafeName(this.name, this.elementName)
        return when (this.attrType) {
            enumType -> safe.enumClassName
            strEnumType -> safe.enumClassName //TODO: StrEnumClass
            dateTime -> "DateTimeAttribute"
            float -> "FloatAttribute"
            integer -> "IntegerAttribute"
            positiveInteger -> "PositiveIntegerAttribute"
            boolean -> "BooleanAttribute"
            string -> "StringAttribute"
            ticker -> "TickerAttribute"
            anyUri -> "LinkAttribute"
        }
    }

val AttributeTypeDeclaration.typeName: String
    get() {
        val safe = AttributeSafeName(this.name, this.elementName)
        return when (this.attrType) {
            enumType, strEnumType -> safe.enumClassName
            dateTime -> "String"
            float -> "Float"
            integer -> "Int"
            positiveInteger -> "Int"
            boolean -> "Boolean"
            string -> "String"
            ticker -> "Boolean"
            anyUri -> "Link"
        }
    }

val AttributeGroup.className: String
    get() {
        return SafeStr.upperFirstLetter(SafeStr.generateSafeName(name))
    }

object AttributeRender {
    val IMPL_PROTECTED_CLASS = "AttributesImpl"

    fun renderEnumClass(attrDecl: AttributeTypeDeclaration, indent: String = ""): String {
        assert(attrDecl.attrType == enumType || attrDecl.attrType == strEnumType,
                "Type must be enumType or strEnum, but it is: " + attrDecl.attrType)

        val s = StrBuilder(indent)
        val className = attrDecl.className
        s.brackets("""public enum class ${className}(override val value: String): EnumValues<${className}>""") {
            for (value in attrDecl.values) {
                val safeValue = SafeStr.safeEnumValue(value)
                appendLine("""${safeValue}: ${className}("${value}")""")
            }
        }
        return s.toString()
    }

    fun renderAttributeDeclaration(attrDecl: AttributeDeclaration): String  {
        val typeDecl = attrDecl.attrTypeDeclaration
        return when (typeDecl.attrType) {
            enumType -> """val ${attrDecl.propertyName} = EnumAttribute("${attrDecl.name}", javaClass<${typeDecl.className}>())"""
            strEnumType -> """val ${attrDecl.propertyName} = EnumAttribute("${attrDecl.name}", javaClass<${typeDecl.className}>())"""
            else -> """val ${attrDecl.propertyName} = ${typeDecl.className}("${attrDecl.name}")"""
        }
    }

    fun renderTraitAttributeClass(attrGroup: AttributeGroup, indent: String = ""): String {
        return renderTraitAttributeClass(attrGroup.className, attrGroup.newAttributes, indent)
    }

    fun renderTraitAttributeClass(className: String, attributes: List<AttributeDeclaration>, indent: String = ""): String {
        val s = StrBuilder(indent)
        s.appendLine("""public trait $className: AttributeGroup""")
        s.indent {
            for (attr in attributes) {
                appendLine("public var ${className}.${attr.propertyName}: ${attr.typeName} by Attributes.${attr.propertyName}")
            }
        }
        return s.toString()
    }
}
