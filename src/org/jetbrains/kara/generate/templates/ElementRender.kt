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
import org.jetbrains.kara.generate.templates.*
import java.util.Collections
import java.util.ArrayList
import org.jetbrains.kara.generate.ElementDeclaration
import java.util.HashSet
import java.util.HashMap
import org.jetbrains.kara.generate.templates.SpecialElementFunction


val ElementDeclaration.className: String
    get() {
        return SafeStr.generateSafeName(name).toUpperCase()
    }
val ElementDeclaration.functionName: String
    get() {
        return SafeStr.lowerFirstLetter(SafeStr.generateSafeName(name))
    }
fun ElementDeclaration.getCollectionArguments(): Collection<List<Argument>> {
    val collectionSpecFunc = SpecialElementFunction.specialFunctionMap.get(this.functionName)!!
    if (collectionSpecFunc.size == 0) {
        return Collections.singleton(SpecialElementFunction.args{
            arg("id", "String?", "null")
            arg("c", "StyleClass?", "null")
            arg("contents", "${className}.() -> Unit", "empty_contents")
        })
    } else {
        return collectionSpecFunc.map { it.arguments }
    }
}
fun ElementDeclaration.isUsualElement(): Boolean {
    return !SpecialElementFunction.specialFunctionMap.containsKey(this.functionName)
}
fun ElementDeclaration.getAllFunction(): Collection<SpecialElementFunction.Function>? {
    val functions = SpecialElementFunction.specialFunctionMap.get(this.functionName)!!
    if (functions.size == 0) {
        return null
    } else {
        return functions
    }
}

val ElementGroupDeclaration.className: String
    get() {
        return SafeStr.upperFirstLetter(name)
    }

object ElementRender {

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

    // fun HtmlBodyTag.ul(c : StyleClass? = null, id : String? = null, contents : UL.() -> Unit = empty_contents) = contentTag(UL(this), c, id, contents)
    // prefix need for Extension function
    fun renderFunctions(element: ElementDeclaration, prefix: String = "fun ", indent: String = INDENT): String {
        val s = StrBuilder(indent)
        if (element.isUsualElement()) {
            val arguments = element.getCollectionArguments()
            assert(arguments.size == 1, "usual element ${element.name} must have one function, but have: " + arguments.size)

            val functionHeader = renderFunctionHeader(element.functionName, arguments.first())
            s.appendLine("${prefix}${functionHeader}: Unit = contentTag(${element.className}(this), c, id, contents)")
        } else {
            for (function in element.getAllFunction()!!) {
                val functionHeader = renderFunctionHeader(element.functionName, function.arguments)
                s.brackets("$prefix$functionHeader: Unit") {
                    append(function.body.normalize(indent+INDENT))
                }
            }
        }
        return s.toString()
    }

    fun renderElement(element: ElementDeclaration, indent: String = ""): String {
        val s = StrBuilder(indent)
        val ext =
            if(element.allowText) {
                "AllowText, "
            } else {
                ""
            }
        s.brackets("""class ${element.className}(containingTag: BaseElement): ${ext}BaseBodyTag(containingTag, "${element.name}")""") {
            appendLine("public final class Attributes: AbstractCommonAttribute<Attributes>()")
            appendLine("public val attr: Attributes = Attributes()")
        }
        val attrClassName = "${element.className}.Attributes"
        for (attr in element.newAttributes) {
            s.appendLine(AttributeRender.renderExtensionAttribute(attrClassName, attr))
        }
        // render extension function
        for (el in element.newAllowElements) {
            s.append(renderFunctions(el, "public fun ${element.className}.", indent + INDENT))
        }
        return s.toString()
    }

    fun renderBaseBodyTagExtension(elements: List<ElementDeclaration>, indent: String = ""): String {
        val s = StrBuilder(indent)
        for(element in elements) {
            s.append(renderFunctions(element, "public fun BaseBodyTag.", indent))
        }
        return s.toString()
    }

}