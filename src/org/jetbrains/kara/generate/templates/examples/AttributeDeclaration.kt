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

package org.jetbrains.kara.generate.templates.examples



object Attributes {
    val datetime = DateTimeAttribute("datetime")
    val value = DateTimeAttribute("value")
    val span = PositiveInteger("span")
    val spellcheck = BooleanAttribute("spellcheck")
    val alt = StringAttribute("alt")
    val disabled = TickerAttribute("disabled")
    val href = LinkAttribute("href")
    val src = LinkAttribute("src")
    val formMethod = EnumAttribute("method", javaClass<FormMethod>())
}

trait AG1: AttributeGroup

var AG1.datetime: String by Attributes.datetime
var AG1.value: String by Attributes.value
var AG1.span: Int by Attributes.span
var AG1.spellcheck: Boolean by Attributes.spellcheck
var AG1.alt: String by Attributes.alt
var AG1.disabled: Boolean by Attributes.disabled
var AG1.href: Link by Attributes.href
var AG1.method: FormMethod by Attributes.formMethod

public enum class FormMethod(override val value: String) : EnumValues<FormMethod> {
    get: FormMethod("get")
    post: FormMethod("post")
    put: FormMethod("put")
    delete: FormMethod("delete")
}









