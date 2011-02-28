package com.redhat.ceylon.compiler.typechecker.analyzer;

import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getDeclaration;
import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getDeclaringType;
import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getExternalDeclaration;
import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getLanguageModuleDeclaration;
import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getMemberDeclaration;
import static com.redhat.ceylon.compiler.typechecker.analyzer.Util.getTypeArguments;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ceylon.compiler.typechecker.context.Context;
import com.redhat.ceylon.compiler.typechecker.model.Class;
import com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.Import;
import com.redhat.ceylon.compiler.typechecker.model.Interface;
import com.redhat.ceylon.compiler.typechecker.model.Module;
import com.redhat.ceylon.compiler.typechecker.model.Package;
import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.TypeParameter;
import com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.Unit;
import com.redhat.ceylon.compiler.typechecker.tree.Node;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.compiler.typechecker.tree.Visitor;
import com.redhat.ceylon.compiler.typechecker.util.PrintUtil;

/**
 * Second phase of type analysis.
 * Scan the compilation unit looking for literal type 
 * declarations and maps them to the associated model 
 * objects. Also builds up a list of imports for the 
 * compilation unit. Finally, assigns types to the 
 * associated model objects of declarations declared 
 * using an explicit type (this must be done in
 * this phase, since shared declarations may be used
 * out of order in expressions).
 * 
 * @author Gavin King
 *
 */
public class TypeVisitor extends Visitor {
    
    private Unit unit;
    private Package importPackage;
    private Context context;

    public TypeVisitor(Unit u, Context context) {
        unit = u;
        this.context = context;
    }
    
    @Override
    public void visit(Tree.ImportPath that) {
        Module m = unit.getPackage().getModule();
        for (Package mp: m.getAllPackages()) {
            if ( hasName(that.getIdentifiers(), mp) ) {
                importPackage = mp;
                return;
            }
        }
        that.addError("Package not found: " + 
                PrintUtil.importNodeToString(that.getIdentifiers()) );
    }

    private boolean hasName(List<Tree.Identifier> importPath, Package mp) {
        if (mp.getName().size()==importPath.size()) {
            for (int i=0; i<mp.getName().size(); i++) {
                if (!mp.getName().get(i).equals(importPath.get(i).getText())) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public void visit(Tree.ImportMemberOrType that) {
        Import i = new Import();
        Tree.Alias alias = that.getAlias();
        if (alias==null) {
            i.setAlias(that.getIdentifier().getText());
        }
        else {
            i.setAlias(alias.getIdentifier().getText());
        }
        Declaration d = getExternalDeclaration(importPackage, that.getIdentifier(), context);
        if (d==null) {
            that.addError("imported declaration not found: " + 
                    that.getIdentifier().getText());
        }
        else {
            if (!d.isShared()) {
                that.addError("imported declaration is not shared: " +
                        that.getIdentifier().getText());
            }
            i.setDeclaration(d);
            unit.getImports().add(i);
        }
    }
        
    @Override 
    public void visit(Tree.BaseType that) {
        super.visit(that);
        TypeDeclaration d = (TypeDeclaration) getDeclaration(that.getScope(), that.getUnit(), that.getIdentifier(), context);
        if (d==null) {
            that.addError("type declaration not found: " + 
                    that.getIdentifier().getText());
        }
        else {
            ProducedType outerType;
            if (d.isMemberType()) {
                outerType = getDeclaringType(that, d);
            }
            else {
                outerType = null;
            }
            visitType(that, outerType, d);
        }
    }

    public void visit(Tree.QualifiedType that) {
        super.visit(that);
        ProducedType pt = that.getOuterType().getTypeModel();
        if (pt!=null) {
            TypeDeclaration d = (TypeDeclaration) getMemberDeclaration(pt.getDeclaration(), that.getIdentifier(), context);
            if (d==null) {
                that.addError("member type declaration not found: " + 
                        that.getIdentifier().getText());
            }
            else {
                visitType(that, pt, d);
            }
        }
    }

    private void visitType(Tree.StaticType that, ProducedType ot, TypeDeclaration d) {
        List<ProducedType> typeArguments = getTypeArguments(that.getTypeArgumentList());
        //if (acceptsTypeArguments(d, typeArguments, that.getTypeArgumentList(), that)) {
            ProducedType pt = d.getProducedType(ot, typeArguments);
            that.setTypeModel(pt);
            that.setTarget(pt);
            /*if (typeArguments!=null) {
                typeArguments.add(pt);
            }*/
        //}
    }
    
    @Override 
    public void visit(Tree.VoidModifier that) {
        that.setTypeModel(getVoidDeclaration().getType());
    }

    @Override 
    public void visit(Tree.TypedDeclaration that) {
        super.visit(that);
        setType(that, that.getType(), that.getDeclarationModel());
    }

    @Override 
    public void visit(Tree.TypedArgument that) {
        super.visit(that);
        setType(that, that.getType(), that.getDeclarationModel());
    }
        
    private void setType(Node that, Tree.Type type, TypedDeclaration td) {
        if (type==null) {
            that.addError("missing type of declaration: " + td.getName());
        }
        else if (!(type instanceof Tree.LocalModifier)) { //if the type declaration is missing, we do type inference later
            ProducedType t = type.getTypeModel();
            if (t==null) {
                //TODO: this case is temporary until we
                //      add support for sequenced parameters
            }
            else {
                td.setType(t);
            }
        }
    }
    
    private ProducedType getExtendedType(Tree.ExtendedType et) {
        ProducedType tm = et.getType().getTypeModel();
        if (tm.getDeclaration() instanceof TypeParameter) {
            et.getType().addError("directly extends a type parameter");
        }
        if (tm.getDeclaration() instanceof Interface) {
            et.getType().addError("extends an interface");
        }
        return tm;
    }

    private List<ProducedType> getSatisfiedTypes(Tree.SatisfiedTypes st, boolean typeParameter) {
        List<ProducedType> list = new ArrayList<ProducedType>();
        if (st!=null) {
            for (Tree.StaticType t: st.getTypes()) {
                ProducedType tm = t.getTypeModel();
                if (tm!=null) {
                    if (!typeParameter && tm.getDeclaration() instanceof TypeParameter) {
                        t.addError("directly satisfies type parameter");
                    }
                    if (!typeParameter && tm.getDeclaration() instanceof Class) {
                        t.addError("satisfies a class");
                    }
                    list.add(tm);
                }
            }
        }
        return list;
    }
    
    @Override 
    public void visit(Tree.TypeParameterDeclaration that) {
        super.visit(that);
        that.getDeclarationModel().setExtendedType(getVoidDeclaration().getType());
    }
    
    @Override
    public void visit(Tree.TypeConstraint that) {
        super.visit(that);
        TypeParameter p = (TypeParameter) getDeclaration(that.getScope(), that.getUnit(), that.getIdentifier(), context);
        if (p==null) {
            //already added error
            //that.addError("no matching type parameter for constraint");
        }
        else {
            Tree.SatisfiedTypes st = that.getSatisfiedTypes();
            if (st!=null) {
                p.setSatisfiedTypes(getSatisfiedTypes(st, true));
            }
        }
    }

    @Override 
    public void visit(Tree.ClassOrInterface that) {
        super.visit(that);
        ClassOrInterface ci = that.getDeclarationModel();
        if (ci==null) {
            //TODO: this case is temporary until we get aliases
        }
        else {
            Tree.SatisfiedTypes st = that.getSatisfiedTypes();
            if (st!=null) {
                ci.setSatisfiedTypes(getSatisfiedTypes(st, false));
            }
        }
    }

    @Override 
    public void visit(Tree.AnyClass that) {
        super.visit(that);
        Class c = that.getDeclarationModel();
        if (c==null) {
            //TODO: this case is temporary until we get aliases
        }
        else {
            Class vd = getVoidDeclaration();
            if (c!=vd) {
                Tree.ExtendedType et = that.getExtendedType();
                if (et==null) {
                    c.setExtendedType(getBaseObjectDeclaration().getType());
                }
                else {
                    c.setExtendedType(getExtendedType(et));
                }
            }
        }
    }

    @Override 
    public void visit(Tree.AnyInterface that) {
        super.visit(that);
        Interface i = that.getDeclarationModel();
        if (i==null) {
            //TODO: this case is temporary until we get aliases
        }
        else {
            i.setExtendedType(getObjectDeclaration().getType());
        }
    }

    @Override 
    public void visit(Tree.ObjectDefinition that) {
        super.visit(that);
        TypeDeclaration td = that.getDeclarationModel().getTypeDeclaration();
        if (td!=null) {
            Tree.ExtendedType et = that.getExtendedType();
            if (et!=null) {
                td.setExtendedType(getExtendedType(et));
            }
            Tree.SatisfiedTypes st = that.getSatisfiedTypes();
            if (st!=null) {
                td.setSatisfiedTypes(getSatisfiedTypes(st, false));
            }
        }
    }
    
    @Override 
    public void visit(Tree.ObjectArgument that) {
        super.visit(that);
        TypeDeclaration td = that.getDeclarationModel().getTypeDeclaration();
        if (td!=null) {
            Tree.ExtendedType et = that.getExtendedType();
            if (et!=null) {
                td.setExtendedType(getExtendedType(et));
            }
            Tree.SatisfiedTypes st = that.getSatisfiedTypes();
            if (st!=null) {
                td.setSatisfiedTypes(getSatisfiedTypes(st, false));
            }
        }
    }
    
    private Class getObjectDeclaration() {
        return (Class) getLanguageModuleDeclaration("Object", context);
    }

    private Class getVoidDeclaration() {
        return (Class) getLanguageModuleDeclaration("Void", context);
    }
    
    private Class getBaseObjectDeclaration() {
        return (Class) getLanguageModuleDeclaration("BaseObject", context);
    }
    
}
