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

import org.jetbrain.kara.generate.AttributeTypeDeclaration.AttributeType



trait AttributeTypeDeclaration {
    public enum class AttributeType {
        dateTime
        float
        integer
        positiveInteger
        boolean
        string
        ticker
        anyUri
        enumType
        strEnumType
    }
    val name: String
    val attrType: AttributeType
    val elementName: String?
    val values: Collection<String>?
}

trait AttributeDeclaration {
    val name: String
    val attrTypeDeclaration: AttributeTypeDeclaration
}

trait AttributeGroup {
    val name: String
    val attributes: Collection<AttributeDeclaration>
    val parentGroups: Collection<AttributeGroup>
}

trait ElementGroupDeclaration: AbstractElementDeclaration
trait ElementDeclaration: AbstractElementDeclaration

private trait AbstractElementDeclaration {
    val name: String
    val allowText: Boolean
    val elementGroups: Collection<ElementGroupDeclaration>
    val newAllowElements: Collection<ElementDeclaration>
    val attributeGroups: Collection<AttributeGroup>
    val attributes: Collection<AttributeDeclaration>
}


trait HtmlModel {
    val attributeDeclarations: Collection<AttributeTypeDeclaration>
    val attributeGroups: Collection<AttributeGroup>
    val simpleElementDeclarations: Collection<ElementDeclaration>
    val groupElementDeclaration: Collection<ElementGroupDeclaration>
}





