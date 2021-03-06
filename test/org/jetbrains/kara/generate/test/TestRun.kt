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

import com.sun.xml.xsom.parser.XSOMParser
import org.jetbrains.kara.generate.HtmlModelBuilder
import java.io.File
import org.jetbrains.kara.generate.templates.ElementRender
import org.jetbrains.kara.generate.templates.FileRender

val SCHEME_URL = "src/org/jetbrains/kara/generate/grammar/html_5.xsd"
val HTML_NAMESPACE = "html-5"

val WRITE_PATCH = "test/out/"
private fun writeFile(name: String, text: String) {
    File(WRITE_PATCH + name).writeText(text)
}

public fun main(args: Array<String>) {
    val parser = XSOMParser()
    parser.parse(SCHEME_URL)
    val schema = parser.getResult()!!.getSchema(HTML_NAMESPACE)!!

    val model = HtmlModelBuilder(schema).build()
    
    
    val fileRender = FileRender(model)

    for (attribute in model.attributeDeclarations) {
        if (attribute.elementName != null) {
            println(attribute.name + " " + attribute.elementName)
        }
    }


    writeFile("model.out", makeStr(model))
    writeFile("Enums.kt", fileRender.renderEnumClassesFile())
    writeFile("Attributes.kt", fileRender.renderAttributesFile())
    writeFile("AttributeGroups.kt", fileRender.renderAttributesGroupFile())
    writeFile("AttributesImpl.kt", fileRender.renderProtectedImplAttributeClassFile())

    writeFile("HtmlElement.kt", fileRender.renderHtmlElementFile())
    writeFile("ElementGroups.kt", fileRender.renderElementGroupFile())
    writeFile("Elements.kt", fileRender.renderAllElementsFile())
//    writeFile("DeprecatedFun.kt", fileRender.renderDeprecatedTraitFile())

}