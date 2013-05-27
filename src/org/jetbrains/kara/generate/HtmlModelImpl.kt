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

import org.jetbrain.kara.generate.*
import java.util.Collections
import org.jetbrain.kara.generate.AttributeTypeDeclaration.AttributeType

class AttributeGroupImp(override val name: String,
                        override val attributes: Collection<AttributeDeclaration>,
                        override val parentGroups: Collection<AttributeGroup>): AttributeGroup

class AttributeTypeDeclarationImpl(override val attrType: AttributeTypeDeclaration.AttributeType,
                                 override val name: String,
                                 override val elementName: String? = null,
                                 override val values: Collection<String>? = null
): AttributeTypeDeclaration

public enum class SimpleAttributeTypeDeclaration(attrType: AttributeType): AttributeTypeDeclaration {
    override val name: String = attrType.name()
    override val attrType: AttributeTypeDeclaration.AttributeType = attrType
    override val elementName: String? = null
    override val values: Collection<String>? = null

    dateTime : SimpleAttributeTypeDeclaration(AttributeType.dateTime)
    float : SimpleAttributeTypeDeclaration(AttributeType.float)
    integer : SimpleAttributeTypeDeclaration(AttributeType.integer)
    positiveInteger : SimpleAttributeTypeDeclaration(AttributeType.positiveInteger)
    boolean : SimpleAttributeTypeDeclaration(AttributeType.boolean)
    string : SimpleAttributeTypeDeclaration(AttributeType.string)
    ticker : SimpleAttributeTypeDeclaration(AttributeType.ticker)
    anyUri : SimpleAttributeTypeDeclaration(AttributeType.anyUri)
}

class AttributeDeclarationImpl(override val name: String,
                               override val attrTypeDeclaration: AttributeTypeDeclaration
): AttributeDeclaration

open class CommonElementDeclaration(override val name: String,
                             override val allowText: Boolean = false,
                             override val elementGroups: Collection<ElementGroupDeclaration> = Collections.emptyList(),
                             override val newAllowElements: Collection<ElementDeclaration> = Collections.emptyList(),
                             override val attributeGroups: Collection<AttributeGroup> = Collections.emptyList(),
                             override val attributes: Collection<AttributeDeclaration> = Collections.emptyList()
): ElementGroupDeclaration, ElementDeclaration

class SpecialGroupDeclaration(name: String, elementGroups: Collection<ElementGroupDeclaration>,
                              val newAllowElementsFun: () -> Collection<ElementDeclaration>
): CommonElementDeclaration(name, false, elementGroups) {
    private var realNewAllowElements: Collection<ElementDeclaration>? = null;

    override val newAllowElements: Collection<ElementDeclaration>
        get() {
            if (realNewAllowElements == null) {
                realNewAllowElements = newAllowElementsFun.invoke()
            }
            return realNewAllowElements!!
        }
}
