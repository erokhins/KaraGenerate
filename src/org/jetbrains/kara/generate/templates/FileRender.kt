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
import java.util.HashSet
import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*


class FileRender(val htmlModel: HtmlModel, val packageName: String = "kara.test") {

    fun renderFile(append: StrBuilder.() -> Unit): String {
        val s = StrBuilder()
        s.append(
"""
package ${packageName}

""")
        s.append()
        return s.toString()
    }

    fun renderAttributesFile(): String {
        return renderFile {
            append("object Attributes {\n")
            for (attrDecl in htmlModel.attributeDeclarations) {
                appendLine{ append(INDENT).append(AttributeRender.renderAttributeDeclaration(attrDecl))  }
            }
            append("}\n")
        }
    }

    fun renderEnumClassesFile(): String {
        return renderFile {
            for (attrTypeDecl in htmlModel.attributeTypeDeclarations) {
                when (attrTypeDecl.attrType) {
                    enumType -> appendLine(AttributeRender.renderEnumClass(attrTypeDecl))
                    strEnumType -> appendLine(AttributeRender.renderStrEnumClass(attrTypeDecl))
                    else -> throw IllegalStateException("All attrTypeDecl.attrType must be enum or strEnum, but this type is: ${attrTypeDecl.attrType}")
                }
            }
        }
    }

    fun renderAttributesGroupFile(): String {
        return renderFile {
            for (attrGroup in htmlModel.attributeGroups) {
                append(AttributeRender.renderTraitAttributeClass(attrGroup))
                append("\n")
            }
        }
    }

    // elements
    fun renderBaseBodyTagExtensionFile(): String {
        return renderFile {
            val allElementsInGroups = HashSet<ElementDeclaration>()
            for (group in htmlModel.elementGroupDeclaration) {
                allElementsInGroups.addAll(group.newAllowElements)
            }
            val elements = allElementsInGroups.sort{(a, b) -> a.name.compareTo(b.name)}
            append(ElementRender.renderBaseBodyTagExtension(elements, INDENT))
        }
    }

    fun renderAllElementsFile(): String {
        return renderFile {
            for (element in htmlModel.elementDeclarations) {
                append(ElementRender.renderElement(element)).append("\n")
            }
        }
    }

}