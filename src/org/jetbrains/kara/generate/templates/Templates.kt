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

import org.jetbrains.kara.generate.test.StrBuilder
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType
import org.jetbrains.kara.generate.toStringList
import org.jetbrains.kara.generate.AttributeTypeDeclaration

class SaveName(val saveName: String, val realName: String)
val INDENT = "    "
val ENDLINE = "\n"

fun renderFile(packageName: String, append: StringBuilder.() -> Unit): String {
    val s = StringBuilder()
    s.append(
"""
package ${packageName}

""")
    s.append()
    return s.toString()
}

fun renderAttributesFile(packageName: String = "kara", append: StringBuilder.() -> Unit): String {
    return renderFile(packageName) {
        append("object Attributes {\n")
        append()
        append("}\n")
    }
}

fun <T>Collection<T>.toExtendString(toStrFun: T.() -> String = { toString() }): String {
    val str = this.toStringList(toStrFun, ", ")
    return if (str.isEmpty()) {
        ""
    } else {
        ": " + str
    }
}


fun renderEnumAttributeClass(className: String, values: List<SaveName>, startIndent: String = ""): String {
    val s = StrBuilder(startIndent)
    val header = """public enum class ${className}(override val value: String): EnumValues<${className}> {"""
    val tail = """}"""

    s.appendLine(header)
    for (value in values) {
        s.appendLine{ append(INDENT).append("""${value.saveName}: ${className}("${value.realName}")""") }
    }

    s.appendLine(tail)

    return s.toString()
}

// TODO:
fun renderStrEnumAttributeClass(className: String, values: List<SaveName>, startIndent: String = ""): String {
    return renderEnumAttributeClass(className, values, startIndent)
}

fun attrTypeToClassName(attrType: AttributeType): String?  {
    return when(attrType) {
        dateTime -> "DateTimeAttribute"
        float -> "FloatAttribute"
        integer -> "IntegerAttribute"
        positiveInteger -> "PositiveIntegerAttribute"
        boolean -> "BooleanAttribute"
        string -> "StringAttribute"
        ticker -> "TickerAttribute"
        anyUri -> "LinkAttribute"

        else -> null
    }
}

fun attrTypeToTypeName(attrType: AttributeType): String? {
    return when (attrType) {
        dateTime -> "String"
        float -> "Float"
        integer -> "Int"
        positiveInteger -> "Int"
        boolean -> "Boolean"
        string -> "StringAttribute"
        ticker -> "Boolean"
        anyUri -> "Link"

        else -> null
    }
}

fun renderSimpleAttributeTypeDeclaration(attrName: String, realName: String, attrType: AttributeType): String  {
    return """val ${attrName} = ${attrTypeToClassName(attrType)}("${realName}")"""
}

fun renderEnumAttributeTypeDeclaration(attrName: String, realName: String, nameClass: String): String {
    return """val ${attrName} = EnumAttribute("${realName}", javaClass<${nameClass}>())"""
}

// TODO:
fun renderStrEnumAttributeTypeDeclaration(attrName: String, realName: String, nameClass: String): String {
    return renderEnumAttributeTypeDeclaration(attrName, realName, nameClass)
}


fun renderAttributeGroup(saveGroupName: String, extendedGroups: List<String>, attributes: List<AttributeRender>, startIndent: String = ""): String {
    val s = StrBuilder(startIndent)
    s.appendLine("""public trait ${saveGroupName}${extendedGroups.toExtendString()} {""")
    for (attr in attributes) {
        s.appendLine { append(INDENT).append("public var ${attr.attrName}: ${attr.typeName}") }
    }
    s.appendLine("}")
    return s.toString()
}