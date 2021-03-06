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
                append(AttributeRender.renderEnumClass(attrTypeDecl))
                append("\n")
            }
        }
    }

    fun renderAttributesGroupFile(): String {
        return renderFile {
            for (attrGroup in htmlModel.attributeGroups) {
                append(AttributeRender.renderAttributesGroupTrait(attrGroup))
                append("\n")
            }
        }
    }

    fun renderProtectedImplAttributeClassFile(): String {
        return renderFile {
            val allAttrInGroups: MutableSet<AttributeDeclaration> = HashSet<AttributeDeclaration>()
            for (attrGroup in htmlModel.attributeGroups) {
                allAttrInGroups.addAll(attrGroup.newAttributes)
            }
            for (elementGroup in htmlModel.elementGroupDeclaration) {
                allAttrInGroups.addAll(elementGroup.newAttributes)
            }
            val attributes = allAttrInGroups.sort({(a, b) -> a.name.compareTo(b.name) })
            append(AttributeRender.renderProtectedImplAttributeClass(attributes))
        }
    }

    // elements
    fun renderHtmlElementFile(): String {
        return renderFile {
            val allElementsInGroups = HashSet<ElementDeclaration>()
            for (group in htmlModel.elementGroupDeclaration) {
                allElementsInGroups.addAll(group.newAllowElements)
            }
            val elementInfList = allElementsInGroups.sort{(a, b) -> a.name.compareTo(b.name)}
            append(ElementRender.renderHtmlElementClass(elementInfList))
        }
    }

    fun renderElementGroupFile() : String {
        return renderFile {
            for (group in htmlModel.elementGroupDeclaration) {
                append(ElementRender.renderElementGroupClass(group)).append("\n")
            }
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