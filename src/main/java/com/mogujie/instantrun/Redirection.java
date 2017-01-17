/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.mogujie.instantrun;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

/**
 * A redirection is the part of an instrumented method that calls out to a different implementation.
 */
public abstract class Redirection {

    /**
     * The position where this redirection should happen.
     */
    @NonNull
    private final LabelNode label;

    /**
     * The types to redirect.
     */
    @NonNull
    protected final List<Type> types;

    /**
     * The return type.
     */
    public final Type type;

    protected final String mtdName;

    protected final String mtdDesc;

    protected final String visitedClassName;

    protected final boolean isStatic;

    protected final String newMtdDesc;

    Redirection(@NonNull LabelNode label, String visitedClassName, @NonNull String mtdName, @NonNull String mtdDesc, @NonNull List<Type> types, @NonNull Type type, boolean isStatic) {
        this.label = label;
        this.types = types;
        this.type = type;
        this.visitedClassName = visitedClassName;
        this.mtdName = mtdName;
        this.mtdDesc = mtdDesc;
        this.isStatic = isStatic;
        newMtdDesc = isStatic ? mtdDesc : ("(L" + visitedClassName + ";" + mtdDesc.substring(1));

    }

    /**
     * Adds the instructions to do a generic redirection.
     * <p/>
     * Note that the generated bytecode does not have a direct translation to code, but as an
     * example, the following code block gets inserted.
     * <code>
     * if ($change != null) {
     * $change.access$dispatch($name, new object[] { arg0, ... argsN })
     * $anyCodeInsertedbyRestore
     * }
     * $originalMethodBody
     * </code>
     *
     * @param mv     the method visitor to add the instructions to.
     * @param change the local variable containing the alternate implementation.
     */
    protected void redirect(GeneratorAdapter mv, int change) {
        // code to check if a new implementation of the current class is available.
        Label l0 = new Label();
        mv.loadLocal(change);
        mv.visitJumpInsn(Opcodes.IFNULL, l0);
//        mv.loadLocal(change);
//        mv.push(InstantProguardMap.instance().getClassIndex());
//        mv.push(InstantProguardMap.instance().getMtdIndex());
//        mv.invokeStatic(IncrementalVisitor.MTD_MAP_TYPE, Method.getMethod("Object get(int,int)"));
//        mv.visitVarInsn(Opcodes.ASTORE,1);
//        mv.visitVarInsn(Opcodes.ALOAD,1);
//        mv.visitJumpInsn(Opcodes.IFNULL, l0);
        doRedirect(mv, change);

        // Return
        if (type == Type.VOID_TYPE) {
            mv.pop();
        } else {
            ByteCodeUtils.unbox(mv, type);
        }
        mv.returnValue();

        // jump label for classes without any new implementation, just invoke the original
        // method implementation.
        mv.visitLabel(l0);
    }

    abstract void doRedirect(GeneratorAdapter mv, int change);

    public LabelNode getPosition() {
        return label;
    }
}