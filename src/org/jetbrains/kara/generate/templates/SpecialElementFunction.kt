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

import java.util.ArrayList
import java.util.HashMap
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap


class Argument(val name: String, val argType: String, val defaultValue: String? = null)

object SpecialElementFunction {
    class ArgumentCreator {
        val arguments = ArrayList<Argument>()
        fun arg(name: String, argType: String, defaultValue: String? = null): ArgumentCreator {
            arguments.add(Argument(name, argType, defaultValue))
            return this
        }
    }
    fun args(create: ArgumentCreator.() -> Unit): List<Argument> {
        val argCreator = ArgumentCreator()
        argCreator.create()
        return argCreator.arguments
    }

    class Function(val arguments: List<Argument>, val body: String)

    fun Multimap<String, Function>.put(name: String, args: ArgumentCreator.() -> Unit, body: String) {
        val argCreator = ArgumentCreator()
        argCreator.args()
        this.put(name, Function(argCreator.arguments, body))
    }

    val specialFunctionMap: Multimap<String, Function>
    {
        val map = HashMultimap.create<String, Function>()
        map.put("meta", { arg("name", "String").arg("content", "String") },
                """
                    val tag = build(META(this), { })
                    tag.name = name
                    tag.content = content
                """
                )
        map.put("title", { arg("text", "String") },
                """
                    build(TITLE(this), { +text })
                """
        )
        map.put("title", { arg("init", "TITLE.() -> Unit", "{ }") },
                """
                    build(TITLE(this), init)
                """
        )


        specialFunctionMap = map
    }

}