/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.model.typechecker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A scope representing a block of a control structure. Note
 * that even though declarations belonging to this scope 
 * aren't visible in the containing scope, the language still
 * imposes a constraint of name uniqueness across the outer
 * scope and all contained control structure blocks!
 *
 */
public class ControlBlock extends Element implements Scope {
    
    private Set<Value> specifiedValues;
    private List<Declaration> members = new ArrayList<Declaration>(3);
    private boolean let;
    
    @Override
    public boolean isToplevel() {
        return false;
    }
    
    public boolean isLet() {
        return let;
    }
    
    public void setLet(boolean let) {
        this.let = let;
    }
    
    @Override
    public List<Declaration> getMembers() {
        return members;
    }
    
    @Override
    public void addMember(Declaration declaration) {
        members.add(declaration);
    }
    
    
    private int id;
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int ret = 17;
        ret = (31 * ret) + getContainer().hashCode();
        ret = (31 * ret) + id;
        return ret;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if (obj instanceof ControlBlock) {
            ControlBlock that = (ControlBlock) obj;
            return id==that.id && 
                    getContainer().equals(that.getContainer());
        }
        else {
            return false;
        }
    }

    public Set<Value> getSpecifiedValues() {
        return specifiedValues;
    }

    public void setSpecifiedValues(Set<Value> assigned) {
        this.specifiedValues = assigned;
    }
    
}
