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

class AttributeGroupImp(override val name: String,
                        override val attributes: Collection<AttributeDeclaration>,
                        override val parentGroups: Collection<AttributeGroup>): AttributeGroup

class SimpleAttributeDeclaration(override val attrType: AttributeDeclaration.AttributeType,
                                 override val name: String,
                                 override val elementName: String? = null): AttributeDeclaration

class ElementDeclarationImpl(override val name: String,
                             override val elementType: ElementDeclaration.ElementType,
                             override val attributes: Collection<AttributeDeclaration>,
                             override val attributeGroups: Collection<AttributeGroup>,
                             override val newAllowElements: Collection<ElementDeclaration>,
                             override val parentElement: ElementDeclaration? = null): ElementDeclaration
