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

package org.jetbrains.kara.generate.test

import org.jetbrain.kara.generate.*



class StrBuilder(val indent: String = "") {
    private val stringBuilder = StringBuilder()

    public fun append(any: Any?): StrBuilder {
        stringBuilder.append(any)
        return this
    }

    public fun appendLine(line: Any? = "") {
        append(indent).append(line).append("\n")
    }
    public fun appendLine(line: StrBuilder.() -> Unit = {}) {
        append(indent).line()
        append("\n")
    }

    public fun appendCollection<T>(values: Collection<T>, strMaker: T.() -> String = { this.toString() }): StrBuilder {
        values.map { it.strMaker() }.appendString(stringBuilder, ", ", "[", "]")
        return this
    }

    public fun toString(): String {
        return stringBuilder.toString()
    }

    public fun toString(actions: StrBuilder.() -> Unit): String {
        this.actions()
        return stringBuilder.toString()
    }
}


fun makeStr(attrTypeDecl: AttributeTypeDeclaration, indent: String = ""): String {
    return StrBuilder(indent).toString {
        appendLine("AttributeTypeDecl = (${attrTypeDecl.name}, ${attrTypeDecl.elementName}, ${attrTypeDecl.attrType})")
        appendLine { append("Values = ").appendCollection(attrTypeDecl.values) }
    }
}

fun makeStr(attrDecl: AttributeDeclaration, indent: String = ""): String {
    return StrBuilder(indent).toString {
        appendLine("AttributeDecl = ${attrDecl.name}")
        appendLine("AttributeTypeDecl = (${attrDecl.attrTypeDeclaration.name}, ${attrDecl.attrTypeDeclaration.elementName})")
    }
}

fun makeStr(attrGroupType: AttributeGroup, indent: String = ""): String {
    return StrBuilder(indent).toString {
        appendLine("AttributeGroup = ${attrGroupType.name}")
        appendLine { append("ParentGroups = ").appendCollection(attrGroupType.parentGroups, { name }) }
        appendLine { append("NewAttributes = ").appendCollection(attrGroupType.newAttributes, { name })}
    }
}

fun makeStr(element: AbstractElementDeclaration, indent: String = "", isGroup: Boolean = false): String {
    return StrBuilder(indent).toString {
        if (isGroup) {
            appendLine("ElementGroupDeclaration = (${element.name}, ${element.allowText})")
        } else {
            appendLine("ElementDeclaration = (${element.name}, ${element.allowText})")
        }
        appendLine { append("ElementGroups = ").appendCollection(element.elementGroups, {name}) }
        appendLine { append("NewAllowElements = ").appendCollection(element.newAllowElements, {name}) }
        appendLine { append("AttributeGroups = ").appendCollection(element.attributeGroups, {name}) }
        appendLine { append("NewAttributes = ").appendCollection(element.newAttributes, {
            "($name | ${attrTypeDeclaration.name} | ${attrTypeDeclaration.elementName})"
        }) }

    }
}

fun makeStr(htmlModel: HtmlModel): String {
    val indent = "   "
    return StrBuilder().toString() {
        appendLine("AttributeDecl:")
        for (el in htmlModel.attributeDeclarations) {
            appendLine(makeStr(el, indent))
        }

        appendLine("AttributeGroupDecl:")
        for (el in htmlModel.attributeGroups) {
            appendLine(makeStr(el, indent))
        }
        appendLine("ElementDecl:")
        for (el in htmlModel.simpleElementDeclarations) {
            appendLine(makeStr(el, indent))
        }
        appendLine("ElementGroup:")
        for (el in htmlModel.groupElementDeclaration) {
            appendLine(makeStr(el, indent))
        }
    }
}