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

import java.util.HashMap


object SaveStr {
    private val specialReplaceMap: Map<String, String>
    {
        val specialReplaceMap = HashMap<String, String>()
        specialReplaceMap.put("class", "c")
        specialReplaceMap.put("for", "forId")
        specialReplaceMap.put("is", "is_")
        specialReplaceMap.put("true", "true_")
        specialReplaceMap.put("false", "false_")
        specialReplaceMap.put("as", "as_")
        specialReplaceMap.put("var", "var_")
        specialReplaceMap.put("object", "object_")

        this.specialReplaceMap = specialReplaceMap
    }

    private fun specialReplace(str: String): String? {
        return specialReplaceMap.get(str)
    }

    public fun replaceUnsavedChars(str: String): String {
        return str.replaceAll("[^a-zA-Z_0-9]", "_")
    }

    public fun generateSaveName(str: String): String {
        val saveChars = replaceUnsavedChars(str)
        if (saveChars.isEmpty()) {
            return "_"
        }
        if (saveChars[0] in '0'..'9') {
            return "_" + saveChars
        }
        return specialReplace(saveChars) ?: saveChars
    }
}
