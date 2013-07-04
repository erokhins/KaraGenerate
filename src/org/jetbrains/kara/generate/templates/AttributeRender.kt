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

import org.jetbrains.kara.generate.AttributeDeclaration
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*
import org.jetbrains.kara.generate.AttributeTypeDeclaration
import java.util.ArrayList
import org.jetbrains.kara.generate.test.StrBuilder

object AttributeRender {

    val AttributeDeclaration.attrName: String
        get() {
            return AttributeSafeName(name, elementName).safeName
        }

    class AttributeTypeInf(val className: String, val typeName: String)
    fun AttributeTypeDeclaration.inf(): AttributeTypeInf {
        val safe = AttributeSafeName(this.name, this.elementName)
        val className =
                when (this.attrType) {
                    enumType -> "EnumAttribute"
                    strEnumType -> "EnumAttribute" //TODO: StrEnumClass
                    dateTime -> "DateTimeAttribute"
                    float -> "FloatAttribute"
                    integer -> "IntegerAttribute"
                    positiveInteger -> "PositiveIntegerAttribute"
                    boolean -> "BooleanAttribute"
                    string -> "StringAttribute"
                    ticker -> "TickerAttribute"
                    anyUri -> "LinkAttribute"
                }
        val typeName =
                when (this.attrType) {
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
        return AttributeTypeInf(className, typeName)
    }

    fun renderEnumClassDeclaration(attrDecl: AttributeTypeDeclaration, indent: String = ""): String {
        assert(attrDecl.attrType == enumType || attrDecl.attrType == strEnumType,
                "Type must be enumType or strEnum, but it is: " + attrDecl.attrType)

        val s = StrBuilder(indent)
        val className = attrDecl.inf().className
        s.appendLine("""public enum class ${className}(override val value: String): EnumValues<${className}> {""")
        for (value in attrDecl.values) {
            s.appendLine{ val safeValue = SafeStr.safeEnumValue(value)
                append(INDENT).append("""${safeValue}: ${className}("${value}")""") }
        }
        s.appendLine("}")
        return s.toString()
    }

}
