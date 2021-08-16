/*
 * Copyright (c) 2021 VMware, Inc.
 * SPDX-License-Identifier: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.vmware.ddlog.ir;

import com.facebook.presto.sql.tree.Node;
import com.vmware.ddlog.util.Utilities;

import javax.annotation.Nullable;

public class DDlogEITE extends DDlogExpression {
    private final DDlogExpression cond;
    private final DDlogExpression then;
    @Nullable
    private final DDlogExpression eelse;

    public DDlogEITE(@Nullable Node node, DDlogExpression cond,
                     DDlogExpression then, @Nullable DDlogExpression eelse) {
        super(node, eelse == null ? then.getType() :
                (then.getType().mayBeNull ? eelse.getType() : then.getType()));
        if (eelse != null)
            DDlogType.checkCompatible(then.getType(), eelse.getType(), true);
        this.cond = this.checkNull(cond);
        this.then = this.checkNull(then);
        this.eelse = eelse;
        if (!(this.cond.type instanceof DDlogTBool))
            this.error("Condition is not Boolean");
    }

    @Override
    public boolean compare(DDlogExpression val, IComparePolicy policy) {
        if (!super.compare(val, policy))
            return false;
        if (!val.is(DDlogEITE.class)) return false;
        DDlogEITE other = val.to(DDlogEITE.class);
        if (!this.cond.compare(other.cond, policy))
            return false;
        if (!this.then.compare(other.then, policy))
            return false;
        switch (Utilities.canBeSame(this.eelse, other.eelse)) {
            case Yes:
                return true;
            case No:
                return false;
        }
        assert other.eelse != null;
        assert this.eelse != null;
        return this.eelse.compare(other.eelse, policy);
    }

    @Override
    public String toString() {
        String result =  "if (" + this.cond.toString() + ") {\n" +
                this.then.toString() + "}";
        if (this.eelse != null)
            result += " else {\n" +
                this.eelse.toString() + "}";
        return result;
    }
}
