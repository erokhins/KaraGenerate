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
import java.util.Collections
import java.util.ArrayList
import org.jetbrains.kara.generate.templates.ElementGenerator.Argument
import org.jetbrains.kara.generate.ElementDeclaration
import java.util.HashSet
import java.util.HashMap


class ElementGenerator(val htmlModel: HtmlModel) {

    class Argument(val name: String, val argType: String, val defaultValue: String? = null)

    fun renderFunctionHeader(functionName: String, arguments: List<Argument>, addDefaultValue: Boolean = true): String {
        return arguments.map{
            val s = "${it.name}: ${it.argType}"
            if (it.defaultValue != null && addDefaultValue) {
                s + " = ${it.defaultValue}"
            } else {
                s
            }
        }.makeString(", ", "$functionName(", ")")
    }

    //    fun HtmlBodyTag.ul(c : StyleClass? = null, id : String? = null, contents : UL.() -> Unit = empty_contents) = contentTag(UL(this), c, id, contents)

    class ElementInformationProvider(val element: ElementDeclaration) {
        val replaceMap: Map<String, String>
        {
            val replaceMap = HashMap<String, String>()
            replaceMap.put("var", "var_")
            replaceMap.put("object", "object_")

            this.replaceMap = replaceMap
        }

        fun isBodyTagElement(): Boolean {
            return true // TODO:
        }

        fun getSaveName(): String {
            return replaceMap.get(element.name) ?: element.name
        }

        fun getReaTagName(): String {
            return element.name
        }

        fun getNameTag(): String {
            return getSaveName().toLowerCase()
        }

        fun getClassName(): String {
            return getSaveName().toUpperCase()
        }

        fun getArguments(): List<Argument> {
            val args = ArrayList<Argument>()
            if (isBodyTagElement()) {
                args.add(Argument("c", "StyleClass?", "null"))
                args.add(Argument("id", "String?", "null"))
                args.add(Argument("contents", "${getClassName()}.() -> Unit", "empty_contents"))
            } else {
                // TODO:
            }

            return args
        }
    }

    class ElementGroupInfProvider(val group: ElementGroupDeclaration) {

        fun getClassName(): String {
            return upFirstLetter(group.name)
        }
    }

    // prefix need for Extension function
    fun renderFunction(inf: ElementInformationProvider, prefix: String = "fun ", indent: String = INDENT): String {
        val s = StrBuilder(indent)
        if (inf.isBodyTagElement()) {
            val functionHeader = renderFunctionHeader(inf.getNameTag(), inf.getArguments())
            s.append(indent)
            s.append("${prefix}${functionHeader}: Unit = contentTag(${inf.getClassName()}(this), c, id, contents)")
        } else {
            // TODO:
        }

        return s.toString()
    }

    fun renderHtmlElementClass(elementInfList: List<ElementInformationProvider>, indent: String = ""): String {
        val s = StrBuilder(indent)
        s.appendLine("fun <T:HtmlElement> HtmlElement.contentTag(tag : T, c : StyleClass? = null, id : String? = null, contents : T.() -> Unit = empty_contents) {}")
        s.appendLine()
        s.appendLine("public open class HtmlElement(containingTag: HtmlElement?, val tagName: String): AttributesImpl() {")

        for (elementInf in elementInfList) {
            s.appendLine {
                append(renderFunction(elementInf, "protected fun ", indent + INDENT))
            }
        }

        s.appendLine("}")
        return s.toString()
    }

    fun renderDeprecatedTrait(elementInfList: List<ElementInformationProvider>, indent: String = ""): String {
        val s = StrBuilder(indent)
        s.appendLine("trait DeprecatedFun {")

        for (inf in elementInfList) {
            s.appendLine {
                val functionHeader = renderFunctionHeader(inf.getNameTag(), inf.getArguments(), false)
                s.appendLine("""${INDENT}deprecated("") public fun $functionHeader""")
            }
        }

        s.appendLine("}")
        return s.toString()
    }

    fun renderTraitExtension(decl: AbstractElementDeclaration): String {
        val allTraits = HashSet<String>()
        if (decl.allowText) {
            allTraits.add("AllowText")
        }
        allTraits.addAll(decl.attributeGroups.map { getSaveGroupName(it.name) })
        allTraits.addAll(decl.elementGroups.map { ElementGroupInfProvider(it).getClassName() })
        return allTraits.makeString(", ")
    }

    fun renderElementGroupClass(group: ElementGroupDeclaration, indent: String = ""): String {
        val s = StrBuilder(indent)
        val groupInf = ElementGroupInfProvider(group)
        var ext = renderTraitExtension(group)
        if (ext.length() > 0) {
            ext = ": " + ext
        }
        s.appendLine("trait ${groupInf.getClassName()}${ext} {")
        for (attr in group.newAttributes) {
            //TODO: attrGenerate
        }
        for (element in group.newAllowElements) {
            val inf = ElementInformationProvider(element)
            val functionHeader = renderFunctionHeader(inf.getNameTag(), inf.getArguments(), false)
            s.appendLine("${INDENT}public fun $functionHeader")
        }
        s.appendLine("}")
        return s.toString()
    }

    fun renderElement(element: ElementDeclaration, indent: String = ""): String {
        val s = StrBuilder(indent)
        val elementInf = ElementInformationProvider(element)
        var ext = renderTraitExtension(element)
        if (ext.length() > 0) {
            ext = ext + ", "
        }
        s.appendLine("""class ${elementInf.getClassName()}(containingTag: HtmlElement): ${ext}HtmlElement(containingTag, "${elementInf.getReaTagName()}") {""")
        for (attr in element.newAttributes) {
            //TODO: attrGenerate
        }
        s.appendLine("}")
        for (el in element.newAllowElements) {
            val inf = ElementInformationProvider(el)
            s.appendLine(renderFunction(inf, "public fun ${elementInf.getClassName()}.", indent))
        }
        return s.toString()
    }

    fun renderHtmlElementFile(): String {
        return renderFile("kara.test") {
            val allElementsInGroups = HashSet<ElementDeclaration>()
            for (group in htmlModel.elementGroupDeclaration) {
                allElementsInGroups.addAll(group.newAllowElements)
            }
            val elementInfList = allElementsInGroups.sort{(a, b) -> a.name.compareTo(b.name)}.map { ElementInformationProvider(it) }
            append(renderHtmlElementClass(elementInfList))
        }
    }

    fun renderElementGroupFile() : String {
        return renderFile("kara.test") {
            for (group in htmlModel.elementGroupDeclaration) {
                append(renderElementGroupClass(group)).append("\n")
            }
        }
    }

    fun renderAllElementsFile(): String {
        return renderFile("kara.test") {
            for (element in htmlModel.elementDeclarations) {
                append(renderElement(element)).append("\n")
            }
        }
    }

    fun renderDeprecatedTraitFile(): String {
        return renderFile("kara.test") {
            append(renderDeprecatedTrait(htmlModel.elementDeclarations.map { ElementInformationProvider(it) }))
        }
    }
}