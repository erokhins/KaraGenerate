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

package org.jetbrains.kara.generate

import com.sun.xml.xsom.parser.XSOMParser
import com.sun.xml.xsom.*
import java.util.*
import org.jetbrains.kara.generate.*
import com.sun.xml.xsom.XSModelGroup.Compositor
import org.jetbrains.kara.generate.test.makeStr


class HtmlModelBuilder(val schema: XSSchema) {
    val attributeGroupCache = Cache<XSAttGroupDecl, AttributeGroup>()
    val elementCache = Cache<XSElementDecl, ElementDeclaration>()
    val elementGroupXSComplexTypeCache = Cache<XSComplexType, ElementGroupDeclaration>()
    val elementGroupXSModelGroupDeclCache = Cache<XSModelGroupDecl, ElementGroupDeclaration>()
    val attrCache = AttributeTypeCache()

    private fun buildAttributeGroup(groupDeclaration: XSAttGroupDecl): AttributeGroup {
        return attributeGroupCache.get(groupDeclaration) {
            val attrGroups = getAttGroups().map { buildAttributeGroup(it!!) }
            val attributes = getDeclaredAttributeUses()!!.map { attrCache.getAttributeDeclaration(it!!.getDecl()!!, groupDeclaration.getName()!!) }
            AttributeGroupImp(getName()!!, attrGroups, attributes)
        }
    }

    private fun buildElementGroupDeclaration(groupDecl: XSModelGroupDecl): ElementGroupDeclaration {  // example: <xsd:group name="phrasingContent">
        return elementGroupXSModelGroupDeclCache.get(groupDecl) {
            val modelGroup = getModelGroup()!!
            if (modelGroup.getCompositor() != Compositor.CHOICE) {
                throw IllegalStateException("in model group declaration modelGroup must have compositor CHOISE")
            }
            val elementGroups: MutableCollection<ElementGroupDeclaration> = ArrayList()
            modelGroup.forEach {
                val term = it.getTerm()!!
                if (term.isModelGroupDecl()) {
                    elementGroups.add(buildElementGroupDeclaration(term.asModelGroupDecl()!!));
                }
            }

            UnCyclicalGroupDeclaration(getName()!!, elementGroups) {
                val newAllowElements: MutableCollection<ElementDeclaration> = ArrayList()
                modelGroup.forEach {
                    val term = it.getTerm()!!
                    if (term.isElementDecl()) {
                        newAllowElements.add(buildElementDeclaration(term.asElementDecl()!!));
                    }
                }
                newAllowElements
            }
        }
    }

    private fun buildElementGroupDeclaration(complexType: XSComplexType): ElementGroupDeclaration {  // example: <xsd:element name="h1" type="simplePhrasingContentElement"/>
        return elementGroupXSComplexTypeCache.get(complexType) {
            buildCommonElementDeclaration(this, getName()!!)
        }
    }

    private fun getContentXSTerm(complexType: XSComplexType): Collection<XSTerm> {
        val modelGroup = complexType.getContentType().asParticle()?.getTerm()?.asModelGroup();

        if (modelGroup == null && complexType.getContentType().asSimpleType() != null) {
            throw IllegalStateException("unsupported xsd format")
        }

        if (modelGroup == null) {
            return Collections.emptyList()
        }
        val resultCollection = ArrayList<XSTerm>()
        modelGroup.forEach { resultCollection.add(it.getTerm()!!) }
        return resultCollection
    }

    private fun buildCommonElementDeclaration(complexType: XSComplexType, elementName: String): CommonElementDeclaration {
        val attrGroups = complexType.getAttGroups().map { buildAttributeGroup(it!!) }
        val attributes = complexType.getDeclaredAttributeUses()!!.map {
            attrCache.getAttributeDeclaration(it!!.getDecl()!!, elementName)
        }

        val elementGroups: MutableCollection<ElementGroupDeclaration> = ArrayList()
        val newAllowElement: MutableCollection<ElementDeclaration> = ArrayList()
        for (term in getContentXSTerm(complexType)) {
            if (term.isModelGroupDecl()) {
                elementGroups.add(buildElementGroupDeclaration(term.asModelGroupDecl()!!));
            } else {
                if (term.isElementDecl()) {
                    newAllowElement.add(buildElementDeclaration(term.asElementDecl()!!))
                } else {
                    throw IllegalStateException("bad term type, element name: ${elementName}")
                }
            }
        }
        return CommonElementDeclaration(elementName, complexType.isMixed(), elementGroups, newAllowElement, attrGroups, attributes)
    }

    private fun buildElementDeclaration(elementDecl: XSElementDecl): ElementDeclaration {
        return elementCache.get(elementDecl) {
            if (!getType()!!.isComplexType()) {
                throw IllegalStateException("element must have complex type, element name: ${getName()}")
            }
            val complexType = getType()!!.asComplexType()!!

            if (complexType.getName() == null) {                             // anonymous type decl
                buildCommonElementDeclaration(complexType, getName()!!)
            } else {                                                         // ref to elements group
                val elementGroup = buildElementGroupDeclaration(complexType);
                CommonElementDeclaration(getName()!!, false, Collections.singleton(elementGroup)) // allowText from group
            }
        }
    }

    private fun iterateAllElements() {
        val elementDeclarationIterator = schema.iterateElementDecls()!!
        while (elementDeclarationIterator.hasNext()) {
            buildElementDeclaration(elementDeclarationIterator.next())
        }
    }

    public fun build(): HtmlModel {
        iterateAllElements()
        val elementGroups = ArrayList(elementGroupXSModelGroupDeclCache.getAllElements())
        elementGroups.addAll(elementGroupXSComplexTypeCache.getAllElements())
        return HtmlModelImpl(
                attrCache.getAllTypeDecl(),
                attrCache.getAllDecl(),
                attributeGroupCache.getAllElements(),
                elementCache.getAllElements(),
                elementGroups
        )
    }
}
