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
package com.googlecode.concurrenttrees.radix.node.concrete.chararray;

import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Stores only incoming edge as a {@code char[]}, and a reference to a value. Does <b>not</b> store any outgoing
 * edges.
 *
 * @author Niall Gallagher
 */
public class CharArrayNodeLeafWithValue implements Node {


    // Characters in the edge arriving at this node from a parent node.
    // Once assigned, we never modify this...
    private final char[] incomingEdgeCharArray;

    // An arbitrary value which the application associates with a key matching the path to this node in the tree.
    // This value can be null...
    private final Object value;
    
 // his mark represent the reserve of node to work on this node
    private AtomicBoolean mark;
    
    // the partialWork corresponds to references of nodes created by a thread 
    // it is used to allow another incoming thread finishes the job of another stuck thread
    private AtomicMarkableReference<Pair<Node, Node>> partialWork;

    public CharArrayNodeLeafWithValue(CharSequence edgeCharSequence, Object value) {
        this.incomingEdgeCharArray = CharSequences.toCharArray(edgeCharSequence);
        this.value = value;
        this.mark = new AtomicBoolean(false);
        this.partialWork = new AtomicMarkableReference<Pair<Node, Node>>(null, false);
    }

    @Override
    public CharSequence getIncomingEdge() {
        return CharSequences.fromCharArray(incomingEdgeCharArray);
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return incomingEdgeCharArray[0];
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Node getOutgoingEdge(Character edgeFirstCharacter) {
        return null;
    }

    @Override
    public void updateOutgoingEdge(Node childNode) {
        throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() +"', no such edge already exists: " + childNode);
    }
    
    @Override
    public boolean updateOutgoingEdge(Node expectedNode, Node childNode) {
    	 throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() +"', no such edge already exists: " + childNode);
    }
    
    @Override
    public boolean updateOutgoingEdgeSentinel(Node expectedNode, Node childNode) {
    	 throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() +"', no such edge already exists: " + childNode);

    }

    @Override
    public List<Node> getOutgoingEdges() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node{");
        sb.append("edge=").append(incomingEdgeCharArray);
        sb.append(", value=").append(value);
        sb.append(", edges=[]");
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean attemptMark(){
    	return mark.compareAndSet(false, true);
    }
    
    @Override
    public boolean getMark(){
    	return mark.get();
    }
    
    @Override
    public void unMark(){
    	mark.set(false);
    }
    
    @Override
    public boolean compareAndSetPartialWork(Pair expectedPair, Pair newPair, boolean expectedMark, boolean newMark){
    	return this.partialWork.compareAndSet(expectedPair, newPair, expectedMark, newMark);

    }
    
    @Override
    public void unsetPartialWork(){
    	this.partialWork.set(null, false);
    }
    
    @Override
    public Pair<Node, Node> getPartialWork(boolean [] markHolder){
    	return this.partialWork.get(markHolder);
    }
}
