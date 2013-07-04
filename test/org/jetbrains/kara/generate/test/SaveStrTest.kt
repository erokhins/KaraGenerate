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
import kotlin.test.assertEquals
import org.jetbrains.kara.generate.templates.SaveStr

class SaveStrTest {

    test fun unsavedCharsTest() {
        assertEquals("_6_sazZa____09", SaveStr.replaceUnsavedChars("%6 sazZa&-?_09"))
    }

    test fun keyWordReplace() {
        assertEquals("c", SaveStr.generateSaveName("class"))
    }

    test fun keyWordReplace2() {
        assertEquals("var_", SaveStr.generateSaveName("var"))
    }

    test fun numberStart() {
        assertEquals("_0", SaveStr.generateSaveName("0"))
    }

    test fun emptyStr() {
        assertEquals("_", SaveStr.generateSaveName(""))
    }

    test fun rightStr() {
        assertEquals("a09_zAZ", SaveStr.generateSaveName("a09_zAZ"))
    }


}