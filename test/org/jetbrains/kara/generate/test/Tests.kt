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


import org.junit.Test as test
import org.junit.Assert.*
import com.sun.xml.xsom.parser.XSOMParser
import org.jetbrains.kara.generate.HtmlModelBuilder
import java.io.BufferedReader
import java.io.FileReader


public class HtmlModelBuildTest {
    private val TEST_PATH = "test/org/jetbrains/kara/generate/test/xsd/"
    val HTML_NAMESPACE = "test"

    private fun readExpectedStr(nameTest: String): String {
        val s = StringBuilder()
        val file = BufferedReader(FileReader(TEST_PATH + "test_${nameTest}.out"))
        file.forEachLine { s.append(it).append("\n") }
        return s.toString()
    }

    private fun runTest(name: String) {
        val parser = XSOMParser()
        parser.parse(TEST_PATH + "test_${name}.xsd")
        val schema = parser.getResult()!!.getSchema(HTML_NAMESPACE)!!

        val model = HtmlModelBuilder(schema).build()
        assertEquals(readExpectedStr(name), makeStr(model))
    }

    test fun simpleTypes() {
        runTest("simple_types")
    }

    test fun attrGroups() {
        runTest("attr_groups")
    }

    test fun externalTypes() {
        runTest("external_attr_types")
    }

    test fun tickerTypes() {
        runTest("ticker_attr")
    }

    test fun complexTypes() {
        runTest("complex_attr_types")
    }

    test fun cyclical() {
        runTest("cyclical")
    }

//    test fun crash() {
//        runTest("crash")
//    }
}