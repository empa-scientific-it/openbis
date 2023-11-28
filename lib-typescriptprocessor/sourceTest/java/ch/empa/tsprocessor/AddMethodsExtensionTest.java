/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.empa.tsprocessor;

import cz.habarta.typescript.generator.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;


public class AddMethodsExtensionTest
{

    interface GenericTestClass<A> {
        A get();
    }

    class TestClass {
        public boolean A;
        public String[] B;
        public boolean getA() {return false;}
        public String[] getB() {return null;}
        TestClass(String a){}
    }

    class InnerTestClass {
        public TestClass A;
        public boolean getA() {return false;}
    }

    interface InterfaceWithWildcard {
        interface OuterInterface{}

        String doSomething(List<? extends OuterInterface> input);
    }




    final Settings settings = new Settings();
    @BeforeTest
    public void setSettings(){
        settings.outputKind = (TypeScriptOutputKind.module);
        settings.jsonLibrary = JsonLibrary.jackson2;
        settings.extensions = List.of(new AddMethodsExtension());
    }



    @Test
    public void testGenericClass() {
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(GenericTestClass.class));
        System.out.println(output);

    }

    @Test
    public void testNormalClass(){
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(TestClass.class));
        System.out.println(output);
    }

    @Test
    public void testInnerClass(){
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(InnerTestClass.class));
        System.out.println(output);
    }
    @Test
    public void testWildCardType(){
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(InterfaceWithWildcard.class));
        System.out.println(output);
    }


}
