/**
 * Copyright 2012-2013 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.concurrenttrees.radix.node.concrete.bytearray.functional;

import com.googlecode.concurrenttrees.radix.node.StampedNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.StampedByteArrayNodeFactory;
import com.googlecode.concurrenttrees.radixreversed.ConcurrentReversedRadixTreeTest;

/**
 * @author Niall Gallagher
 */
public class ByteArrayNodeFactory_ReversedRadixTreeTest extends ConcurrentReversedRadixTreeTest {

    private final StampedNodeFactory nodeFactory = new StampedByteArrayNodeFactory();
    @Override
    protected StampedNodeFactory getNodeFactory() {
        return nodeFactory;
    }
}
