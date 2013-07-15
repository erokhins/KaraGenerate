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
import org.jetbrains.kara.generate.templates.SafeStr

class SafeStrTest {
    test fun unsavedCharsTest() {
        assertEquals("_6_sazZa____09", SafeStr.replaceUnsafeChars("%6 sazZa&-?_09"))
    }
    test fun keyWordReplace() {
        assertEquals("c", SafeStr.generateSafeName("class"))
    }
    test fun keyWordReplace2() {
        assertEquals("var_", SafeStr.generateSafeName("var"))
    }
    test fun numberStart() {
        assertEquals("_0", SafeStr.generateSafeName("0"))
    }
    test fun emptyStr() {
        assertEquals("_", SafeStr.generateSafeName(""))
    }
    test fun rightStr() {
        assertEquals("a09_zAZ", SafeStr.generateSafeName("a09_zAZ"))
    }
    test fun upperEmptyStr() {
        assertEquals("", SafeStr.upperFirstLetter(""))
    }
    test fun upperStrOneLetter() {
        assertEquals("Z", SafeStr.upperFirstLetter("z"))
    }
    test fun upperStr() {
        assertEquals("AbcZ", SafeStr.upperFirstLetter("abcZ"))
    }
    test fun lowerEmptyStr() {
        assertEquals("", SafeStr.lowerFirstLetter(""))
    }
    test fun lowerStrOneLetter() {
        assertEquals("z", SafeStr.lowerFirstLetter("Z"))
    }
    test fun lowerStr() {
        assertEquals("abcZ", SafeStr.lowerFirstLetter("AbcZ"))
    }
}
