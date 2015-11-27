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
package com.googlecode.concurrenttrees.radix.node.concrete.charsequence;

import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.util.AtomicReferenceArrayListAdapter;
import com.googlecode.concurrenttrees.radix.node.util.AtomicStampedReferenceArrayListAdapter;
import com.googlecode.concurrenttrees.radix.node.util.NodeCharacterComparator;
import com.googlecode.concurrenttrees.radix.node.util.NodeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Stores incoming edge as a {@link CharSequence} (a <i>view</i> onto the original key) rather than copying the edge
 * into a character array, and stores outgoing edges as an {@link AtomicReferenceArray}. Does not store a
 * value and returns {@code null} for the value.
 *
 * @author Niall Gallagher
 */
public class CharSequenceNodeNonLeafNullValue implements Node {


    // Characters in the edge arriving at this node from a parent node.
    // Once assigned, we never modify this...
    private final CharSequence incomingEdgeCharSequence;

    // References to child nodes representing outgoing edges from this node.
    // Once assigned we never add or remove references, but we do update existing references to point to new child
    // nodes provided new edges start with the same first character...
    private final AtomicStampedReference<Node> [] outgoingEdges;

    public CharSequenceNodeNonLeafNullValue(CharSequence edgeCharSequence, List<Node> outgoingEdges) {
        Node[] childNodeArray = outgoingEdges.toArray(new Node[outgoingEdges.size()]);
        // Sort the child nodes...
        Arrays.sort(childNodeArray, new NodeCharacterComparator());
        this.outgoingEdges = new AtomicStampedReference[childNodeArray.length];
        for(int i=0; i<childNodeArray.length; i++){
			this.outgoingEdges[i] = new AtomicStampedReference<Node>(childNodeArray[i], 0);
        }
        this.incomingEdgeCharSequence = edgeCharSequence;
    }

    @Override
    public CharSequence getIncomingEdge() {
        return incomingEdgeCharSequence;
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return incomingEdgeCharSequence.charAt(0);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Node getOutgoingEdge(Character edgeFirstCharacter) {
        // Binary search for the index of the node whose edge starts with the given character.
        // Note that this binary search is safe in the face of concurrent modification due to constraints
        // we enforce on use of the array, as documented in the binarySearchForEdge method...
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, edgeFirstCharacter);
        if (index < 0) {
            // No such edge exists...
            return null;
        }
        // Atomically return the child node at this index...
        return outgoingEdges[index].getReference();
    }
    
    @Override
    public Node getOutgoingEdge(Character edgeFirstCharacter, int [] stampHolder) {
        // Binary search for the index of the node whose edge starts with the given character.
        // Note that this binary search is safe in the face of concurrent modification due to constraints
        // we enforce on use of the array, as documented in the binarySearchForEdge method...
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, edgeFirstCharacter);
        if (index < 0) {
            // No such edge exists...
            return null;
        }
        // Atomically return the child node at this index...
        return outgoingEdges[index].get(stampHolder);
    }
    
    @Override
    public AtomicStampedReference<Node> getOutgoingStampedEdge(Character edgeFirstCharacter) {
        // Binary search for the index of the node whose edge starts with the given character.
        // Note that this binary search is safe in the face of concurrent modification due to constraints
        // we enforce on use of the array, as documented in the binarySearchForEdge method...
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, edgeFirstCharacter);
        if (index < 0) {
            // No such edge exists...
            return null;
        }
        // Atomically return the child node at this index...
        return outgoingEdges[index];
    }

    @Override
    public void updateOutgoingEdge(Node childNode) {
        // Binary search for the index of the node whose edge starts with the given character.
        // Note that this binary search is safe in the face of concurrent modification due to constraints
        // we enforce on use of the array, as documented in the binarySearchForEdge method...
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, childNode.getIncomingEdgeFirstCharacter());
        if (index < 0) {
            throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() +"', no such edge already exists: " + childNode);
        }
        // Atomically update the child node at this index...
        outgoingEdges[index].set(childNode, 0);
    }

    @Override
    public List<Node> getOutgoingEdges() {
        return new AtomicStampedReferenceArrayListAdapter<Node>(outgoingEdges);
    }
    

    @Override
    public boolean updateOutgoingEdge(Node expectedChildNode, Node newChildNode, int expectedStamp, int newStamp) {
        // Binary search for the index of the node whose edge starts with the given character.
        // Note that this binary search is safe in the face of concurrent modification due to constraints
        // we enforce on use of the array, as documented in the binarySearchForEdge method...
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, newChildNode.getIncomingEdgeFirstCharacter());
        if (index < 0) {
            throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + newChildNode.getIncomingEdgeFirstCharacter() +"', no such edge already exists: " + newChildNode);
        }
        // Atomically update the child node at this index...
        return outgoingEdges[index].compareAndSet(expectedChildNode, newChildNode, expectedStamp, newStamp);
  
    }
    
    @Override
    public boolean attemptStampChild(Node expectedChildNode, int newStamp){
    	int index = NodeUtil.binarySearchForEdge(outgoingEdges, expectedChildNode.getIncomingEdgeFirstCharacter());
        if (index < 0) {
            // No such edge exists...
            return false;
        }
        Node n =outgoingEdges[index].getReference();
    	return this.outgoingEdges[index].attemptStamp(n, newStamp);
    }

    @Override
    public void setStampChild(Node expectedChildNode, int newStamp){
    	int index = NodeUtil.binarySearchForEdge(outgoingEdges, expectedChildNode.getIncomingEdgeFirstCharacter());
        if (index < 0) {
            // No such edge exists...
        }
        Node n =outgoingEdges[index].getReference();
    	this.outgoingEdges[index].set(n, newStamp);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node{");
        sb.append("edge=").append(incomingEdgeCharSequence);
        sb.append(", value=null");
        sb.append(", edges=").append(getOutgoingEdges());
        sb.append("}");
        return sb.toString();
    }
    
    @Override
	public boolean hasChildStamped() {
		for(int i=0; i< this.outgoingEdges.length; i++)
			if(outgoingEdges[i].getStamp()!=0)
				return true;
		return false;
	}
    
    @Override
    public AtomicStampedReference<Node> [] getOutgoingStampedEdges() {
        return outgoingEdges;
    }
}
