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

package org.jetbrain.kara.generate

import com.sun.xml.internal.xsom.parser.XSOMParser
import com.sun.xml.internal.xsom.*
import java.util.*
import org.jetbrains.kara.generate.*
import org.jetbrain.kara.generate.AttributeDeclaration.AttributeType.*


val attributeGroupCache = Cache<XSAttGroupDecl, AttributeGroup>()
val attributeCache = Cache<XSAttributeDecl, AttributeDeclaration>()
val elementCache = Cache<XSElementDecl, ElementDeclaration>()


val indent = "   "



// TODO:
fun getAttributeDeclaration(xsDecl: XSAttributeDecl): AttributeDeclaration {
    return attributeCache.get(xsDecl) {
        println(indent + getName())

        SimpleAttributeDeclaration(string, getName()!!)
    }
}

fun getAttributeGroup(groupDeclaration: XSAttGroupDecl): AttributeGroup {
    return attributeGroupCache.get(groupDeclaration) {
        println(getName())

        val attrGroups = getAttGroups()!!.getProcessedCollection { getAttributeGroup(it) }
        val attributes = getAttributeUses()!!.getProcessedCollection { getAttributeDeclaration(it.getDecl()!!) }

        println()

        AttributeGroupImp(getName()!!, attributes, attrGroups)
    }
}

public fun getElementGroupDeclaration(groupDecl: XSModelGroupDecl): ElementDeclaration {
    return ElementDeclarationImpl(groupDecl.getName()!!, ElementDeclaration.ElementType.elementGroup, Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList())
}

public fun getContentElements(complexType: XSComplexType): Collection<XSTerm> {
    val modelGroup = complexType.getContentType()?.asParticle()?.getTerm()?.asModelGroup();

    if (modelGroup == null && complexType.getContentType()?.asEmpty() == null) {
        throw IllegalStateException("unsupported xsd format")
    }

    if (modelGroup == null) {
        return Collections.emptyList()
    }
    val resultCollection = ArrayList<XSTerm>()
    modelGroup.getChildren()!!.forEach { resultCollection.add(it.getTerm()!!) }
    return resultCollection
}

public fun buildElementDeclaration(elementDecl: XSElementDecl): ElementDeclaration {
    val elementName = elementDecl.getName()!!
    println(elementName + ":")
    val elementType = elementDecl.getType()!!;

    if (elementType.getName() == null) { // anonymous
        if (!elementType.isComplexType()) {
            throw IllegalStateException("for anonymous type declaration element type must be ComplexType")
        }
        val complexType = elementType.asComplexType()!!

        val attrGroups = complexType.getAttGroups()!!.getProcessedCollection { getAttributeGroup(it) }
        val attributes = complexType.getAttributeUses()!!.getProcessedCollection { getAttributeDeclaration(it.getDecl()!!) }

        var parent: ElementDeclaration? = null
        val newAllowElement: MutableCollection<ElementDeclaration> = ArrayList()
        for (term in getContentElements(complexType)) {
            if (term.isModelGroupDecl()) {
                if (parent != null) {
                    throw IllegalStateException("several element group, element: " + elementName)
                }
                parent = getElementGroupDeclaration(term.asModelGroupDecl()!!);
            } else {
                if (term.isElementDecl()) {
                    newAllowElement.add(getElementDeclaration(term.asElementDecl()!!))
                } else {
                    throw IllegalStateException("bad term type, element: " + elementName)
                }
            }
        }

        println()
        return ElementDeclarationImpl(elementName, ElementDeclaration.ElementType.element, attributes, attrGroups, newAllowElement, parent)
    } else {
        var parent: ElementDeclaration? = null // TODO: parent init

        return ElementDeclarationImpl(elementName, ElementDeclaration.ElementType.element, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), parent)
    }
}

public fun getElementDeclaration(elementDecl: XSElementDecl): ElementDeclaration {
    return elementCache.get(elementDecl) {
        buildElementDeclaration(this)
    }
}

public fun getAllElementTypes(schema: XSSchema) {
    val elementDeclarationIterator = schema.iterateElementDecls()!!
    while (elementDeclarationIterator.hasNext()) {
        val elementDeclaration = elementDeclarationIterator.next()
        getElementDeclaration(elementDeclaration)
    }
}



val SCHEME_URL = "src/org/jetbrains/kara/generate/grammar/html_5.xsd"
val HTML_NAMESPACE = "html-5"

public fun main(args: Array<String>) {
    val parser = XSOMParser()
    parser.parse(SCHEME_URL)
    val schema = parser.getResult()!!.getSchema(HTML_NAMESPACE)!!

    getAllElementTypes(schema)
    //println(schema.getElementDecls())

}


public class Cache<I, R> {
    val cache: MutableMap<I, R> = HashMap<I, R>()

    fun get(input: I, foo: I.() -> R): R {
        if (cache.containsKey(input)) {
            return cache.get(input)!!;
        } else {
            val result = input.foo()
            cache.put(input, result)
            return result
        }
    }
}

fun <I, R> Collection<I?>.getProcessedCollection(processor: (I) -> R): Collection<R> {
    val resultCollection: MutableCollection<R> = ArrayList<R>()
    for (input in this) {
        resultCollection.add(processor.invoke(input!!))
    }
    return resultCollection
}