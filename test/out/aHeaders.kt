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
    protected fun String.plus() {}
}
fun <T:BaseElement> BaseElement.contentTag(tag : T, c : StyleClass? = null, id : String? = null, contents : T.() -> Unit = empty_contents) {}

open class BaseTag(containingTag: BaseElement?, val tagName: String) : BaseElement(containingTag)


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

public trait StrEnumValues<T> {
    val value: String
}

public class StrEnumAttribute<T : StrEnumValues<T>>(name: String, val klass : Class<T>): Attribute<T>(name) {

}


public open class BaseAttributeGroupImpl: AttributeGroup {
    override val attributesMap: MutableMap<String, Any> = HashMap()
    protected fun String.plus() {}

    public fun build<T>(tag: T, contents: T.() -> Unit): T {
        tag.contents()
        return tag
    }
}

public abstract class AbstractAttribute: AttributeGroup {
    override val attributesMap: MutableMap<String, Any> = HashMap()
}

public abstract class AbstractCommonAttribute<T>: AbstractAttribute(), CommonAttributeGroup {
    public fun invoke(f: T.() -> Unit) {
        (this as T).f()
    }
}

public abstract class AbstractCommonEvents<T>: AbstractAttribute(), CommonEventsGroup {
    public fun invoke(f: T.() -> Unit) {
        (this as T).f()
    }
}

public final class CommonAttribute: AbstractCommonAttribute<CommonAttribute>()
public final class CommonEvents: AbstractCommonEvents<CommonEvents>()

public open class BaseBodyTag(containingElement: BaseElement?, name: String): BaseTag(containingElement, name)
val BaseBodyTag.attr = CommonAttribute()
val BaseBodyTag.events = CommonEvents()