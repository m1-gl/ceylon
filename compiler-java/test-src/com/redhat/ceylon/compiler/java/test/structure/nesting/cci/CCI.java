package com.redhat.ceylon.compiler.java.test.structure.nesting.cci;

interface C$CC$CCI<T> {
    
    public abstract com.redhat.ceylon.compiler.java.test.structure.nesting.cci.C<T>.CC $outer();
    
    public T m2();
}
class C<T> {
    
    private final <U>T m1(final U u) {
        throw new ceylon.language.Exception(null, null);
    }
    
    class CC {
        
        final class C$CC$CCI$impl {
            private final com.redhat.ceylon.compiler.java.test.structure.nesting.cci.C$CC$CCI<T> $this;
            
            private final com.redhat.ceylon.compiler.java.test.structure.nesting.cci.C<T>.CC $outer() {
                return com.redhat.ceylon.compiler.java.test.structure.nesting.cci.C.CC.this;
            }
            
            public T m2() {
                return m1(null);
            }
            
            C$CC$CCI$impl(com.redhat.ceylon.compiler.java.test.structure.nesting.cci.C$CC$CCI<T> $this) {
                this.$this = $this;
            }
        }
        
        CC() {
        }
    }
    
    C() {
    }
    
    
}
