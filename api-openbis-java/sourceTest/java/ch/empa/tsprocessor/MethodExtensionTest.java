package ch.empa.tsprocessor;

import ch.systemsx.cisd.base.annotation.JsonObject;
import cz.habarta.typescript.generator.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

public class MethodExtensionTest {

    interface GenericTestClass<A> {
        A get();
    }

    //@JsonObject("TestClass")
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
        settings.extensions = List.of(new MethodExtension());
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
