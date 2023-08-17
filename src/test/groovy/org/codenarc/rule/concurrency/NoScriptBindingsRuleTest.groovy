/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule.concurrency

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

/**
 * Tests for NoScriptBindingsRule
 *
 * @author Josh Chorlton
 */
class NoScriptBindingsRuleTest extends AbstractRuleTestCase<NoScriptBindingsRule> {

    @Test
    void testRuleProperties() {
        assert rule.priority == 3
        assert rule.name == 'NoScriptBindings'
    }

    @Test
    void testSuccessScenario() {
        final SOURCE = '''
          // these usages are OK
          Integer d = 5

          def myfun() {
            def a = "foo"
            Integer b = 6
          }

          class MyCorrectClass {
            private Integer b = 6
            public static final VALUE = 1234
          }

          private myFunction(configFile) {
            def params
            if (fileExists(configFile)) {
              params = utils.readYamlFile(configFile)
            }
            params
          }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void testMainScript() {
        final SOURCE = '''
              a = "foo"
        '''
        assertSingleViolation(SOURCE, 2, 'a = "foo"', 'The script variable [a] does not have a type declaration. It will be bound to the script which could cause concurrency issues.')
    }

    @Test
    void testInFunction() {
        final SOURCE = '''
              def myfun() {
                a = "foo"
              }
        '''
        assertSingleViolation(SOURCE, 3, 'a = "foo"', 'The script variable [a] does not have a type declaration. It will be bound to the script which could cause concurrency issues.')
    }

    @Test
    void testInClass() {
        final SOURCE = '''
              class MyClass {
                private Integer a = 3

                def myfun() {
                  a = 4
                }
              }
        '''
        assertNoViolations(SOURCE)
    }

    @Override
    protected NoScriptBindingsRule createRule() {
        new NoScriptBindingsRule()
    }
}