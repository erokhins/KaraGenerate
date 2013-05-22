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
import com.sun.xml.internal.xsom.XSModelGroup.Compositor
import org.jetbrain.kara.generate.templates.examples.PositiveInteger


val attributeGroupCache = Cache<XSAttGroupDecl, AttributeGroup>()
val attributeCache = Cache<XSAttributeDecl, AttributeDeclaration>()
val elementCache = Cache<XSElementDecl, ElementDeclaration>()
val elementGroupXSComplexTypeCache = Cache<XSComplexType, ElementGroupDeclaration>()
val elementGroupXSModelGroupDeclCache = Cache<XSModelGroupDecl, ElementGroupDeclaration>()


fun nameToType(name: String): AttributeDeclaration.AttributeType? {
    return when (name) {
        "boolean"           -> boolean
        "anyURI"            -> anyUri
        "anySimpleType"     -> string
        "string"            -> string
        "dateTime"          -> dateTime
        "float"             -> float
        "positiveInteger"   -> positiveInteger
        "integer"           -> integer

        else -> null
    }
}

// TODO:
// if call xsDecl1 = xsDecl2? elementName1 != elementName2, then save only xsDecl1 with name elementName1
fun getAttributeDeclaration(xsDecl: XSAttributeDecl, elementName: String): AttributeDeclaration {
    return attributeCache.get(xsDecl) {
        val attributeType = getType()!!
        if (attributeType.getName() != null) {
            val attrType = nameToType(attributeType.getName()!!)
            if (attrType != null) {
                SimpleAttributeDeclaration(attrType, getName()!!, elementName)
            } else {
                SimpleAttributeDeclaration(string, getName()!!)
            }
        } else {
            SimpleAttributeDeclaration(string, getName()!!)
        }
    }
}

fun getAttributeGroup(groupDeclaration: XSAttGroupDecl): AttributeGroup {
    return attributeGroupCache.get(groupDeclaration) {
        val attrGroups = getAttGroups()!!.getProcessedCollection { getAttributeGroup(it) }
        val attributes = getAttributeUses()!!.getProcessedCollection { getAttributeDeclaration(it.getDecl()!!, groupDeclaration.getName()!!) }
        AttributeGroupImp(getName()!!, attributes, attrGroups)
    }
}

public fun getElementGroupDeclaration(groupDecl: XSModelGroupDecl): ElementGroupDeclaration {
    return elementGroupXSModelGroupDeclCache.get(groupDecl) {
        val modelGroup = getModelGroup()!!
        if (modelGroup.getCompositor() != Compositor.CHOICE) {
            throw IllegalStateException("in model group declaration modelGroup must have compositor CHOISE")
        }
        val elementGroups: MutableCollection<ElementGroupDeclaration> = ArrayList()
        modelGroup.forEach {
            val term = it.getTerm()!!
            if (term.isModelGroupDecl()) {
                elementGroups.add(getElementGroupDeclaration(term.asModelGroupDecl()!!));
            }
        }

        SpecialGroupDeclaration(getName()!!, elementGroups) {
            val resultCollection: MutableCollection<ElementDeclaration> = ArrayList()
            modelGroup.forEach {
                val term = it.getTerm()!!
                if (term.isElementDecl()) {
                    resultCollection.add(getElementDeclaration(term.asElementDecl()!!));
                }
            }
            resultCollection
        }
    }
}

public fun getElementGroupDeclaration(complexType: XSComplexType): ElementGroupDeclaration {
    return elementGroupXSComplexTypeCache.get(complexType) {
        buildAbstractElementDeclaration(complexType, complexType.getName()!!)
    }
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
    modelGroup.forEach { resultCollection.add(it.getTerm()!!) }
    return resultCollection
}

public fun buildAbstractElementDeclaration(complexType: XSComplexType, elementName: String): CommonElementDeclaration {
    val attrGroups = complexType.getAttGroups()!!.getProcessedCollection { getAttributeGroup(it) }
    val attributes = complexType.getAttributeUses()!!.getProcessedCollection {
        getAttributeDeclaration(it.getDecl()!!, elementName)
    }

    val elementGroups: MutableCollection<ElementGroupDeclaration> = ArrayList()
    val newAllowElement: MutableCollection<ElementDeclaration> = ArrayList()
    for (term in getContentElements(complexType)) {
        if (term.isModelGroupDecl()) {
            elementGroups.add(getElementGroupDeclaration(term.asModelGroupDecl()!!));
        } else {
            if (term.isElementDecl()) {
                newAllowElement.add(getElementDeclaration(term.asElementDecl()!!))
            } else {
                throw IllegalStateException("bad term type, element: " + elementName)
            }
        }
    }
    return CommonElementDeclaration(elementName, complexType.isMixed(), elementGroups, newAllowElement, attrGroups, attributes)
}

public fun getElementDeclaration(elementDecl: XSElementDecl): ElementDeclaration {
    return elementCache.get(elementDecl) {
        if (!elementDecl.getType()!!.isComplexType()) {
            throw IllegalStateException("element must have complex type")
        }
        val complexType = elementDecl.getType()!!.asComplexType()!!

        if (complexType.getName() == null) {
            buildAbstractElementDeclaration(complexType, elementDecl.getName()!!)
        } else {
            val elementGroup = getElementGroupDeclaration(complexType);
            CommonElementDeclaration(elementDecl.getName()!!, false, Collections.singleton(elementGroup))
        }
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

    fun getAllResults(): Collection<R> {
        return cache.values()
    }
}

fun <I, R> Collection<I?>.getProcessedCollection(processor: (I) -> R): Collection<R> {
    val resultCollection: MutableCollection<R> = ArrayList<R>()
    for (input in this) {
        resultCollection.add(processor.invoke(input!!))
    }
    return resultCollection
}