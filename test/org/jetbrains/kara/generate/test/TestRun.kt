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
import org.jetbrains.kara.generate.templates.AttributesGenerator
import java.io.File
import org.jetbrains.kara.generate.templates.ElementGenerator

val SCHEME_URL = "src/org/jetbrains/kara/generate/grammar/html_5.xsd"
val HTML_NAMESPACE = "html-5"

val WRITE_PATCH = "test/out/"
private fun writeFile(name: String, text: String) {
    File(name).writeText(text)
}

public fun main(args: Array<String>) {
    val parser = XSOMParser()
    parser.parse(SCHEME_URL)
    val schema = parser.getResult()!!.getSchema(HTML_NAMESPACE)!!

    val model = HtmlModelBuilder(schema).build()
    val attrGenerator = AttributesGenerator(model)

    val elementGenerator = ElementGenerator(model)

    writeFile(WRITE_PATCH + "model.out", makeStr(model))
    writeFile(WRITE_PATCH + "Enums.kt", attrGenerator.generateFileEnumClasses())
    writeFile(WRITE_PATCH + "Attributes.kt", attrGenerator.generateAttributesFile())
    writeFile(WRITE_PATCH + "AttributeGroups.kt", attrGenerator.generateAttributesGroupFile())
    writeFile(WRITE_PATCH + "AttributesImpl.kt", attrGenerator.generateMainAttributeFile())

    writeFile(WRITE_PATCH + "HtmlElement.kt", elementGenerator.renderHtmlElementFile())
    writeFile(WRITE_PATCH + "ElementGroups.kt", elementGenerator.renderElementGroupFile())
    writeFile(WRITE_PATCH + "Elements.kt", elementGenerator.renderAllElementsFile())

}