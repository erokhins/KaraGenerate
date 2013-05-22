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

class AttributeGroupImp(override val name: String,
                        override val attributes: Collection<AttributeDeclaration>,
                        override val parentGroups: Collection<AttributeGroup>): AttributeGroup

class SimpleAttributeDeclaration(override val attrType: AttributeDeclaration.AttributeType,
                                 override val name: String,
                                 override val elementName: String? = null): AttributeDeclaration

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
