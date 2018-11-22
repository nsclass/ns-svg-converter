/*
 * Copyright 2017-present, Nam Seob Seo
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

package com.acrocontext.image.svg.model;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */

public class InterNodeList {
    private InterNode[] interNodes;

    @java.beans.ConstructorProperties({"interNodes"})
    public InterNodeList(InterNode[] interNodes) {
        this.interNodes = interNodes;
    }

    public double getValue(int sequenceIdx, int idx) {
        return interNodes[sequenceIdx].getThisPoint()[idx];
    }

    public InterNode[] getInterNodes() {
        return this.interNodes;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InterNodeList)) return false;
        final InterNodeList other = (InterNodeList) o;
        if (!java.util.Arrays.deepEquals(this.getInterNodes(), other.getInterNodes())) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getInterNodes());
        return result;
    }

    public String toString() {
        return "InterNodeList(interNodes=" + java.util.Arrays.deepToString(this.getInterNodes()) + ")";
    }
}
