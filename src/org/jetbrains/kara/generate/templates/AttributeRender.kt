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
        return SafeStr.safePropertyName(this.name)
    }
val AttributeDeclaration.nameInAttributes: String
    get() {
        val safeName = SafeStr.safePropertyName(this.name)
        if (elementName == null) {
            return safeName
        }
        val typeDecl = attrTypeDeclaration
        val secondName = SafeStr.upperFirstLetter(SafeStr.replaceUnsafeChars(name))
        return when (typeDecl.attrType) {
            enumType, strEnumType -> elementName!!.toLowerCase() + secondName
            else -> typeDecl.attrType.name() + secondName
        }
    }

val AttributeTypeDeclaration.enumSafeName: String
    get() {
        return if (elementName != null) {
            elementName!!.toLowerCase() + SafeStr.upperFirstLetter(SafeStr.replaceUnsafeChars(name))
        } else {
            SafeStr.generateSafeName(SafeStr.lowerFirstLetter(name))
        }
    }

val AttributeTypeDeclaration.enumClassName: String
    get() {
        return SafeStr.upperFirstLetter(enumSafeName)
    }

val AttributeTypeDeclaration.className: String
    get() {
        return when (this.attrType) {
            enumType, strEnumType -> enumClassName
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
        return when (this.attrType) {
            enumType, strEnumType -> enumClassName
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
        assert(attrDecl.attrType == enumType, "Type must be enumType, but it is: " + attrDecl.attrType)
        val s = StrBuilder(indent)
        val className = attrDecl.className
        s.brackets("""public enum class ${className}(override val value: String): EnumValues<${className}>""") {
            for (value in attrDecl.values) {
                val safeValue = SafeStr.safePropertyName(value)
                appendLine("""${safeValue}: ${className}("${value}")""")
            }
        }
        return s.toString()
    }

    fun renderStrEnumClass(attrDecl: AttributeTypeDeclaration, indent: String = ""): String {
        assert(attrDecl.attrType == strEnumType, "Type must be strEnum, but it is: " + attrDecl.attrType)
        val s = StrBuilder(indent)
        val className = attrDecl.className
        s.brackets("""public class ${className}(override val value: String): StrEnumValues<${className}>""") {
            for (value in attrDecl.values) {
                val safeValue = SafeStr.safePropertyName(value)
                appendLine("""val ${safeValue} = ${className}("${value}")""")
            }
        }
        return s.toString()
    }

    fun renderAttributeDeclaration(attrDecl: AttributeDeclaration): String  {
        val attrType = attrDecl.attrTypeDeclaration
        return when (attrType.attrType) {
            enumType -> """val ${attrDecl.nameInAttributes} = EnumAttribute("${attrDecl.name}", javaClass<${attrType.className}>())"""
            strEnumType -> """val ${attrDecl.nameInAttributes} = StrEnumAttribute("${attrDecl.name}", javaClass<${attrType.className}>())"""
            else -> """val ${attrDecl.nameInAttributes} = ${attrType.className}("${attrDecl.name}")"""
        }
    }

    fun renderTraitAttributeClass(attrGroup: AttributeGroup, indent: String = ""): String {
        return renderTraitAttributeClass(attrGroup.className, attrGroup.newAttributes, indent)
    }

    fun renderExtensionAttribute(className: String, attr: AttributeDeclaration): String {
        val attrType = attr.attrTypeDeclaration
        return "public var ${className}.${attr.propertyName}: ${attrType.typeName} by Attributes.${attr.nameInAttributes}"
    }

    fun renderTraitAttributeClass(className: String, attributes: List<AttributeDeclaration>, indent: String = ""): String {
        val s = StrBuilder(indent)
        s.appendLine("""public trait $className: AttributeGroup""")
        s.indent {
            for (attr in attributes) {
                appendLine(renderExtensionAttribute(className, attr))
            }
        }
        return s.toString()
    }
}
