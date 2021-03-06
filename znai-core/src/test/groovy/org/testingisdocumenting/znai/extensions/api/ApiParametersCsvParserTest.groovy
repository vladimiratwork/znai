/*
 * Copyright 2019 TWO SIGMA OPEN SOURCE, LLC
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

package org.testingisdocumenting.znai.extensions.api

import org.junit.Test

import static org.testingisdocumenting.znai.parser.TestComponentsRegistry.TEST_COMPONENTS_REGISTRY

class ApiParametersCsvParserTest {
    @Test
    void "should convert dot separated names to api parameter with nested children"() {
        def apiParameters = ApiParametersCsvParser.parse(TEST_COMPONENTS_REGISTRY.markdownParser(), """
firstName, String, descr1
nested, object, descr2
nested.zipCode, String, descr3
nested.address, String, descr4
nested.subNested, object, nested nested
nested.subNested.url, String, nested nested 1
nested.subNested.fileName, String, nested nested 2
nestedList, array of objects, descr5
nestedList.score, int, descr6
'escaped.name', String, desc7
""")

        apiParameters.toMap().should == [parameters: [
                [name: 'firstName', type: 'String', description: [[markdown: 'descr1', type: 'TestMarkdown']]],
                [name: 'nested', type: 'object', description: [[markdown: 'descr2', type: 'TestMarkdown']], children:
                        [[name: 'zipCode', type: 'String', description: [[markdown: 'descr3', type: 'TestMarkdown']]],
                         [name: 'address', type: 'String', description: [[markdown: 'descr4', type: 'TestMarkdown']]],
                         [name: 'subNested', type: 'object', description: [[markdown: 'nested nested', type: 'TestMarkdown']], children:
                                 [[name: 'url', type: 'String', description: [[markdown: 'nested nested 1', type: 'TestMarkdown']]],
                                  [name: 'fileName', type: 'String', description: [[markdown: 'nested nested 2', type: 'TestMarkdown']]]]]]],
                [name: 'nestedList', type: 'array of objects', description: [[markdown: 'descr5', type: 'TestMarkdown']], children: [
                        [name: 'score', type: 'int', description: [[markdown: 'descr6', type: 'TestMarkdown']]]]],
                [name: 'escaped.name', type: 'String', description: [[markdown: 'desc7', type: 'TestMarkdown']]]
        ]]
    }
}
