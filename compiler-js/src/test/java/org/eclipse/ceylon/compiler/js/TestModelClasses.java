/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.compiler.js;

import java.util.List;
import java.util.Map;

import org.eclipse.ceylon.compiler.typechecker.context.PhasedUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import util.ModelUtils;

import org.eclipse.ceylon.compiler.js.loader.MetamodelGenerator;
import org.eclipse.ceylon.compiler.js.loader.MetamodelVisitor;

public class TestModelClasses {

    private static Map<String, Object> topLevelModel;
    private static Map<String, Object> model;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void initTests() {
        TestModelMethodsAndAttributes.initTypechecker();
        MetamodelVisitor mmg = null;
        for (PhasedUnit pu : TestModelMethodsAndAttributes.tc.getPhasedUnits().getPhasedUnits()) {
            if (pu.getPackage().getModule().getNameAsString().equals("t2")) {
                if (mmg == null) {
                    mmg = new MetamodelVisitor(pu.getPackage().getModule());
                }
                pu.getCompilationUnit().visit(mmg);
            }
        }
        topLevelModel = mmg.getModel();
        model = (Map<String, Object>)topLevelModel.get("t2");
    }

    @Test
    public void testTopLevelElements() {
        Assert.assertNotNull("Missing package t2", model);
        String[] tops = { "Algebraic1", "Algebraic2", "algobj1", "algobj2", "AlgOne", "AlgTwo", "AlgThree",
                "ParmTypes1", "ParmTypes2", "ParmTypes3", "ParmTypes4",
                "SimpleClass1", "SimpleClass2", "SimpleClass3", "SimpleClass4",
                "Nested1", "Satisfy2", "Satisfy1"};
        for (String tle : tops) {
            Assert.assertNotNull("Missing top-level element " + tle, model.get(tle));
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void testSimpleClasses() {
        Map<String,Object> cls = (Map<String,Object>)model.get("SimpleClass1");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "SimpleClass1");
        cls = (Map<String, Object>)cls.get("super");
        ModelUtils.checkType(cls, "ceylon.language::Basic");

        cls = (Map<String, Object>)model.get("SimpleClass2");
        ModelUtils.checkAnnotations(cls, "shared", "abstract");
        ModelUtils.checkParam(cls, 0, "name", "ceylon.language::String", false, false);
        ModelUtils.checkAnnotations(((List<Map<String,Object>>)cls.get(MetamodelGenerator.KEY_PARAMS)).get(0), "shared");
        cls = (Map<String, Object>)cls.get("super");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "Basic",
                MetamodelGenerator.KEY_MODULE, "$");

        cls = (Map<String, Object>)model.get("SimpleClass4");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "SimpleClass4",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_CLASS);
        ModelUtils.checkAnnotations(cls, "shared");
        ModelUtils.checkType((Map<String,Object>)cls.get("super"), ".::SimpleClass2");
        ModelUtils.checkParam(cls, 0, "x", "ceylon.language::Integer", true, false);
    }

    @Test @SuppressWarnings("unchecked")
    public void testSimpleClassWithMethods() {

        Map<String, Object> cls = (Map<String, Object>)model.get("SimpleClass3");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "SimpleClass3");
        Map<String,Map<String, Object>> m2 = (Map<String,Map<String, Object>>)cls.get(MetamodelGenerator.KEY_METHODS);
        Assert.assertEquals("SimpleClass3 should have 1 method", 1, m2.size());
        ModelUtils.checkMap(m2.get("equals"), MetamodelGenerator.KEY_NAME, "equals",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_METHOD);
        ModelUtils.checkAnnotations(m2.get("equals"), "shared", "actual");
        ModelUtils.checkType(m2.get("equals"), "ceylon.language::Boolean");
        m2 = (Map<String,Map<String, Object>>)cls.get(MetamodelGenerator.KEY_ATTRIBUTES);
        Assert.assertEquals("SimpleClass3 should have 1 attribute", 1, m2.size());
        ModelUtils.checkMap(m2.get("hash"), MetamodelGenerator.KEY_NAME, "hash",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_GETTER);
        ModelUtils.checkAnnotations(m2.get("hash"), "shared", "actual");
        ModelUtils.checkType(m2.get("hash"), "ceylon.language::Integer");
        cls = (Map<String, Object>)cls.get("super");
        ModelUtils.checkType(cls, "ceylon.language::Object");
    }

    @Test @SuppressWarnings("unchecked")
    public void testSatisfies1() {
        Map<String,Object> cls = (Map<String,Object>)model.get("Satisfy1");
        ModelUtils.checkType((Map<String, Object>)cls.get("super"), "ceylon.language::Basic");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_SATISFIES);
        Assert.assertEquals("Satisfy1 should satisfy 1 interface", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "ceylon.language::Comparable<.::Satisfy1>");
        Map<String,Map<String,Object>> m2 = (Map<String,Map<String,Object>>)cls.get(MetamodelGenerator.KEY_METHODS);
        
        Assert.assertEquals("Satisfy1 should implement 1 method", 1, m2.size());
        ModelUtils.checkType(m2.get("compare"), "ceylon.language::Comparison");
        ModelUtils.checkParam(m2.get("compare"), 0, "other", ".::Satisfy1", false, false);
    }

    @Test @SuppressWarnings("unchecked")
    public void testSatisfies2() {
        Map<String,Object> cls = (Map<String, Object>)model.get("Satisfy2");
        ModelUtils.checkType((Map<String, Object>)cls.get("super"), "ceylon.language::Basic");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_SATISFIES);
        Assert.assertEquals("Satisfy1 should satisfy 2 interfaces", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "ceylon.language::Iterable<ceylon.language::Integer>");
        Map<String,Map<String,Object>> m2 = (Map<String,Map<String,Object>>)cls.get(MetamodelGenerator.KEY_METHODS);
        Assert.assertEquals("Satisfy2 should have 1 method", 1, m2.size());
        ModelUtils.checkType(m2.get("iterator"), "ceylon.language::Iterator<ceylon.language::Integer>");
        m2 = (Map<String,Map<String,Object>>)cls.get(MetamodelGenerator.KEY_ATTRIBUTES);
        Assert.assertEquals("Satisfy2 should have 1 attribute", 1, m2.size());
        ModelUtils.checkType(m2.get("clone"), ".::Satisfy2");
    }

    @Test @SuppressWarnings("unchecked")
    public void testParameterTypes1() {
        Map<String,Object> cls = (Map<String,Object>)model.get("ParmTypes1");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_TYPE_PARAMS);
        Assert.assertEquals("ParmTypes1 must have 1 parameter type", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "Element");
        ModelUtils.checkParam(cls, 0, "x", "Element", false, false);
    }

    @Test @SuppressWarnings("unchecked")
    public void testParameterTypes2() {
        Map<String,Object> cls = (Map<String, Object>)model.get("ParmTypes2");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_TYPE_PARAMS);
        Assert.assertEquals("ParmTypes2 must have 1 parameter type", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "Element");
        ModelUtils.checkMap(ps.get(0), MetamodelGenerator.KEY_DS_VARIANCE, "out");
        ModelUtils.checkParam(cls, 0, "x", "Element", false, true);
        ps = (List<Map<String, Object>>)ps.get(0).get(MetamodelGenerator.KEY_SATISFIES);
        ModelUtils.checkType(((List<Map<String, Object>>)ps).get(0), "ceylon.language::Object");
    }

    @Test @SuppressWarnings("unchecked")
    public void testParameterTypes3() {
        Map<String,Object> cls = (Map<String, Object>)model.get("ParmTypes3");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_TYPE_PARAMS);
        Assert.assertEquals("ParmTypes3 must have 2 parameter types", 2, ps.size());
        ModelUtils.checkType(ps.get(0), "Type1");
        ModelUtils.checkType(ps.get(1), "Type2");
        ModelUtils.checkParam(cls, 0, "a1", "Type1", false, false);
        ModelUtils.checkParam(cls, 1, "a2", "Type2", false, false);
        List<Map<String, Object>> ptc = (List<Map<String, Object>>)ps.get(0).get(MetamodelGenerator.KEY_SATISFIES);
        ModelUtils.checkType(ptc.get(0), "ceylon.language::Number");
        ptc = (List<Map<String,Object>>)ps.get(1).get("of");
        ModelUtils.checkType(ptc.get(0), "ceylon.language::String");
        ModelUtils.checkType(ptc.get(1), "ceylon.language::Singleton<ceylon.language::String>");
    }

    @Test @SuppressWarnings("unchecked")
    public void testParameterTypes4() {
        Map<String,Object> cls = (Map<String, Object>)model.get("ParmTypes4");
        List<Map<String, Object>> ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_TYPE_PARAMS);
        Assert.assertEquals("ParmTypes4 must have 1 parameter type", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "Element");
        ModelUtils.checkMap(ps.get(0), MetamodelGenerator.KEY_DS_VARIANCE, "out");
        ModelUtils.checkParam(cls, 0, "elems", "Element", false, true);
        ps = (List<Map<String, Object>>)cls.get(MetamodelGenerator.KEY_SATISFIES);
        Assert.assertEquals("ParmTypes4 should satisfy 1 interface", 1, ps.size());
        ModelUtils.checkType(ps.get(0), "ceylon.language::Iterable<Element>");
        Map<String,Map<String,Object>> m2 = (Map<String,Map<String,Object>>)cls.get(MetamodelGenerator.KEY_ATTRIBUTES);
        ModelUtils.checkType(m2.get("primero"), "ceylon.language::Null|Element");
    }

    @Test @SuppressWarnings("unchecked")
    public void testNested() {
        Map<String,Object> cls = (Map<String,Object>)model.get("Nested1");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "Nested1",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_CLASS);
        ModelUtils.checkAnnotations(cls, "shared");
        cls = ((Map<String,Map<String,Object>>)cls.get(MetamodelGenerator.KEY_CLASSES)).get("Nested2");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "Nested2",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_CLASS);
        ModelUtils.checkAnnotations(cls, "shared");
        cls = ModelUtils.getPrivateMethod("innerMethod1", cls);
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "innerMethod1",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_METHOD);
        ModelUtils.checkType(cls, "ceylon.language::Anything");
    }

    @Test @SuppressWarnings("unchecked")
    public void testAlgebraicClasses() {
        Map<String,Object> cls = (Map<String,Object>)model.get("Algebraic1");
        ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, "Algebraic1",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_CLASS);
        ModelUtils.checkAnnotations(cls, "shared", "abstract");
        //"name" is an initializer parameter...
        ModelUtils.checkParam(cls, 0, "name", "ceylon.language::String", false, false);
        //and also a shared attribute
        Map<String, Map<String, Object>> m2 = (Map<String, Map<String, Object>>)cls.get(MetamodelGenerator.KEY_ATTRIBUTES);
        ModelUtils.checkMap(m2.get("name"), MetamodelGenerator.KEY_NAME, "name",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_ATTRIBUTE);
        ModelUtils.checkAnnotations(m2.get("name"), "shared");
        ModelUtils.checkType(m2.get("name"), "ceylon.language::String");
        List<Map<String, Object>> types = (List<Map<String, Object>>)cls.get("of");
        Assert.assertNotNull("Algebraic1 should have case types", types);
        Assert.assertEquals("Algebraic1 should have 3 case types", 3, types.size());
        ModelUtils.checkType(types.get(0), ".::AlgOne");
        ModelUtils.checkType(types.get(1), ".::AlgTwo");
        ModelUtils.checkType(types.get(2), ".::AlgThree");
        for (Map<String, Object> m3 : types) {
            cls = (Map<String, Object>)model.get((String)m3.get(MetamodelGenerator.KEY_NAME));
            Assert.assertNotNull("Missing top-level class " + m3.get(MetamodelGenerator.KEY_NAME), cls);
            ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, (String)m3.get(MetamodelGenerator.KEY_NAME),
                    MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_CLASS);
            ModelUtils.checkAnnotations(cls, "shared");
            cls = (Map<String,Object>)cls.get("super");
            ModelUtils.checkType(cls, ".::Algebraic1");
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void testAlgebraicObjects() {
        Map<String,Object> cls = (Map<String, Object>)model.get("Algebraic2");
        //"name" is an initializer parameter...
        ModelUtils.checkParam(cls, 0, "name", "ceylon.language::String", false, false);
        //and also a shared attribute
        Map<String, Map<String, Object>> m2 = (Map<String, Map<String, Object>>)cls.get(MetamodelGenerator.KEY_ATTRIBUTES);
        ModelUtils.checkMap(m2.get("name"), MetamodelGenerator.KEY_NAME, "name",
                MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_ATTRIBUTE);
        ModelUtils.checkAnnotations(m2.get("name"), "shared");
        ModelUtils.checkType(m2.get("name"), "ceylon.language::String");
        List<Map<String, Object>> types = (List<Map<String, Object>>)cls.get("of");
        Assert.assertNotNull("Algebraic2 should have case types", types);
        Assert.assertEquals("Algebraic2 should have 2 case types", 2, types.size());
        for (Map<String,Object> m3 : types) {
            //Get the object
            cls = (Map<String, Object>)model.get((String)m3.get(MetamodelGenerator.KEY_NAME));
            Assert.assertNotNull("Missing top-level object " + m3.get(MetamodelGenerator.KEY_NAME), cls);
            ModelUtils.checkMap(cls, MetamodelGenerator.KEY_NAME, (String)m3.get(MetamodelGenerator.KEY_NAME),
                    MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_OBJECT);
            ModelUtils.checkAnnotations(cls, "shared");
            types = (List<Map<String,Object>>)cls.get(MetamodelGenerator.KEY_SATISFIES);
            ModelUtils.checkType(types.get(0), "ceylon.language::Iterable<ceylon.language::Integer>");
            ModelUtils.checkType((Map<String,Object>)cls.get("super"), ".::Algebraic2");
            m2 = (Map<String, Map<String, Object>>)cls.get(MetamodelGenerator.KEY_METHODS);
            ModelUtils.checkMap(m2.get("iterator"), MetamodelGenerator.KEY_NAME, "iterator",
                    MetamodelGenerator.KEY_METATYPE, MetamodelGenerator.METATYPE_METHOD);
            ModelUtils.checkAnnotations(m2.get("iterator"), "shared", "actual");
            ModelUtils.checkType(m2.get("iterator"), "ceylon.language::Iterator<ceylon.language::Integer>");
        }
    }

}
