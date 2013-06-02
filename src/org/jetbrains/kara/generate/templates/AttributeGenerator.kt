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

package org.jetbrains.kara.generate.templates

import org.jetbrains.kara.generate.AttributeTypeDeclaration.AttributeType.*
import java.util.HashMap
import java.util.ArrayList
import org.jetbrains.kara.generate.templates.SaveName
import org.jetbrains.kara.generate.templates.renderEnumAttributeClass
import org.jetbrains.kara.generate.templates.renderEnumAttributeTypeDeclaration
import org.jetbrains.kara.generate.AttributeTypeDeclaration
import org.jetbrains.kara.generate.AttributeDeclaration
import org.jetbrains.kara.generate.HtmlModel

class AttributesGenerator(val htmlModel: HtmlModel) {
    val specialAttrNames: Map<String, String> = createSpecialAttrNames()

    fun createSpecialAttrNames(): Map<String, String> {
        val map = HashMap<String, String>()
        map.put("class", "c")
        map.put("is", "is_")
        map.put("true", "true_")
        map.put("false", "false_")
        map.put("as", "as_")
        map.put("for", "forId")
        return map
    }

    fun upFirstLetter(str: String): String {
        return str.substring(0, 1).toUpperCase() +str.substring(1)
    }


    fun getSaveName(inputName: String, elementName: String?): String {
        var name = getSaveValue(inputName)
        if (elementName != null) {
            name = elementName.toLowerCase() + upFirstLetter(name)
        }
        name = specialAttrNames.get(name) ?: name
        return name
    }

    fun getAttributeTypeClassName(attrTypeDecl: AttributeTypeDeclaration): String { // only for enumType & strEnumType
        assert(attrTypeDecl.attrType == enumType || attrTypeDecl.attrType == strEnumType,
                "Type must be enumType or strEnum, but it is: " + attrTypeDecl.attrType)
        return upFirstLetter(getSaveName(attrTypeDecl.name, attrTypeDecl.elementName))
    }

//TODO: fix this
    fun getSaveValue(keyWords: String): String {
        val save1 = keyWords.replace('-','_').replace('/','_').replace('.','_')
        return specialAttrNames.get(keyWords) ?: save1
    }

    fun generateAttributeDeclaration(attrDecl: AttributeDeclaration): String  {
        val saveName = getSaveName(attrDecl.name, attrDecl.elementName)
        if (attrDecl.attrTypeDeclaration.attrType == enumType) {
            return renderEnumAttributeTypeDeclaration(saveName, attrDecl.name,
                    getAttributeTypeClassName(attrDecl.attrTypeDeclaration))
        }
        if (attrDecl.attrTypeDeclaration.attrType == strEnumType) {
            return renderStrEnumAttributeTypeDeclaration(saveName, attrDecl.name,
                    getAttributeTypeClassName(attrDecl.attrTypeDeclaration))
        }
        return renderSimpleAttributeTypeDeclaration(saveName, attrDecl.name,
                attrDecl.attrTypeDeclaration.attrType)
    }

    fun generateEnumClassDeclaration(attrDecl: AttributeTypeDeclaration): String {
        assert(attrDecl.attrType == enumType,
                "Type must be enumType or strEnum, but it is: " + attrDecl.attrType)
        val saveValues = ArrayList<SaveName>()
        for (value in attrDecl.values) {
            saveValues.add(SaveName(getSaveValue(value), value))
        }

        return renderEnumAttributeClass(getAttributeTypeClassName(attrDecl), saveValues)
    }


    fun generateFileEnumClasses(): String {
        return renderFile("kara.test") {
            for (attrTypeDecl in htmlModel.attributeTypeDeclarations) {
                append(generateEnumClassDeclaration(attrTypeDecl)).append("\n")
            }
        }
    }

    fun generateAttributesFile(): String {
        return renderAttributesFile("kara.test") {
            for (attrDecl in htmlModel.attributeDeclarations) {
                append(INDENT).append(generateAttributeDeclaration(attrDecl)).append("\n")
            }
        }
    }

}