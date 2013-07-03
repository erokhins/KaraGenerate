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
import org.jetbrains.kara.generate.test.StrBuilder
import java.util.Collections
import java.util.ArrayList
import org.jetbrains.kara.generate.templates.ElementGenerator.Argument
import org.jetbrains.kara.generate.ElementDeclaration
import java.util.HashSet


class ElementGenerator(val htmlModel: HtmlModel) {

    class Argument(val name: String, val argType: String, val defaultValue: String? = null)

    fun renderFunctionHeader(functionName: String, arguments: List<Argument>): String {
        return arguments.map{
            val s = "${it.name}: ${it.argType}"
            if (it.defaultValue != null) {
                s + "= ${it.defaultValue}"
            } else {
                s
            }
        }.makeString(", ", "$functionName(", ")")
    }

    //    fun HtmlBodyTag.ul(c : StyleClass? = null, id : String? = null, contents : UL.() -> Unit = empty_contents) = contentTag(UL(this), c, id, contents)

    class ElementInformationProvider(val element: ElementDeclaration) {
        fun isBodyTagElement(): Boolean {
            return true // TODO:
        }

        fun getNameTag(): String {
            return element.name.toLowerCase()
        }

        fun getClassNameForTag(): String {
            return element.name.toUpperCase()
        }

        fun getArguments(): List<Argument> {
            val args = ArrayList<Argument>()
            if (isBodyTagElement()) {
                args.add(Argument("c", "StyleClass?", "null"))
                args.add(Argument("id", "String?", "null"))
                args.add(Argument("contents", "${getClassNameForTag()}.() -> Unit", "empty_contents"))
            } else {
                // TODO:
            }

            return args
        }
    }

    // prefix need for Extension function
    fun renderFunction(inf: ElementInformationProvider, prefix: String = "", indent: String = INDENT): String {
        val s = StrBuilder(indent)
        if (inf.isBodyTagElement()) {
            val functionHeader = renderFunctionHeader(inf.getNameTag(), inf.getArguments())
            s.append(indent)
            s.append("fun ${prefix}${functionHeader} = contentTag(${inf.getClassNameForTag()}(this), c, id, contents)")
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
                append(renderFunction(elementInf, indent = indent + INDENT))
            }
        }

        s.appendLine("}")
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

}