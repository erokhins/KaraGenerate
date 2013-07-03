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

package kara.test

import java.util.HashMap

public trait StyleClass
val <T> empty_contents: T.() -> Unit = {}

trait AttributeGroup {
    val attributesMap: MutableMap<String, Any>
}

open class BaseElement(val containingElement: BaseElement?) {
    val attributesMap = HashMap<String, Any>()
    protected fun String.plus() {}
}
fun <T:BaseElement> BaseElement.contentTag(tag : T, c : StyleClass? = null, id : String? = null, contents : T.() -> Unit = empty_contents) {}

trait AllowText {
    public fun String.plus()
}

public abstract class Attribute<T>(val name : String) {
    fun get(attributeGroup : AttributeGroup, property : PropertyMetadata) : T {
        return attributeGroup.attributesMap.get(name) as T
    }
    fun set(attributeGroup : AttributeGroup, property : PropertyMetadata, value : T) {
        attributeGroup.attributesMap.put(name, value)
    }
}


public class DateTimeAttribute(name: String): Attribute<String>(name)
public class FloatAttribute(name: String): Attribute<Float>(name)
public class IntegerAttribute(name: String): Attribute<Int>(name)
public class PositiveIntegerAttribute(name: String): Attribute<Int>(name)
public class BooleanAttribute(name: String): Attribute<Boolean>(name)
public class StringAttribute(name: String): Attribute<String>(name)
public class TickerAttribute(name: String): Attribute<Boolean>(name)

//anyUri
public trait Link
public class LinkAttribute(name: String): Attribute<Link>(name)

//enumType
public trait EnumValues<T : Enum<T>> : Enum<T> { // Enum must implements this trait
    val value : String get() = name()
}
public class EnumAttribute<T : EnumValues<T>>(name: String, val klass : Class<T>): Attribute<T>(name)

public class StrEnumAttribute<T>(name: String, val klass : Class<T>): Attribute<T>(name)


public open class BaseAttributeGroupImpl: AttributeGroup {
    override val attributesMap: MutableMap<String, Any> = HashMap()
    protected fun String.plus() {}
}