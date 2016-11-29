package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * E. Wang's AVL-g tree implementation.
 */
public class AvlGTree<K, V> extends AbstractMap<K,V> implements SortedMap<K,V> {
    
	public final int g;
   
    private Comparator<? super K> comparator = null;
    private AvlNode<K, V> root = null;
    private int modCount = 0;
    private long size = 0;

    public AvlGTree() {
        this.g = 1;
    }

    public AvlGTree(final Comparator<? super K> comp) {
        this.comparator = comp;
        this.g = 2;
    }

    public AvlGTree(final int g) {
        this.g = g;
    }

    public AvlGTree(final Comparator<? super K> comp, final int g) {
        this.comparator = comp;
        this.g = g;
    }

    public Comparator<? super K> comparator() {
        return comparator;
    }

    public void clear() {
        size = 0;
        root = null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        if (size > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        else
            return (int) size;
    }

    public int height() {
        return root.getHeight();
    }

    public boolean containsKey(Object key) {
        if (key == null)
            throw new NullPointerException();
        return getNode(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        return nodeContainsValue(root, value);
    }

    public V get(Object key) {
        if (key == null)
            throw new NullPointerException();

        AvlNode<K, V> p = getNode(key);
        return (p == null ? null : p.value);
    }

    public V put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();

        AvlNode<K, V> t = root;
        if (t == null) {
            root = new AvlNode<K, V>(key, value, comparator);
            size = 1;
            modCount++;
            return null;
        }
        AvlNode<K, V> e = new AvlNode<K, V>(key, value, comparator);
        V oldValue = root.add(e);

        if (oldValue == null) {
            fixAfterModification(e);
            size++;
            modCount++;
            return null;
        } else {
            return oldValue;
        }
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    //For use in the AVL Node class
    private static boolean eq(Object o1, Object o2) {
    	return o1 == null ? o2 == null : o1.equals(o2);
    }
    
    static final class AvlNode<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        public AvlNode<K, V> left = null;
        public AvlNode<K, V> right = null;
        public AvlNode<K, V> parent = null;
        Comparator<? super K> comparator;
        private int leftHeight;
        private int rightHeight;

        AvlNode(K key, V value, Comparator<? super K> comp) {
            this.key = key;
            this.value = value;
            this.parent = null;
            this.comparator = comp;
            this.leftHeight = 0;
            this.rightHeight = 0;
        }

        public V add(AvlNode<K, V> node) {
            int cmp = compare(node.key, this.key);
            if (cmp < 0) {
                if (left == null) {
                    leftHeight = 1;
                    left = node;
                    left.parent = this;
                    return null;
                } else {
                    V ret = this.left.add(node);
                    if (ret == null)
                        leftHeight = left.getHeight();
                    return ret;
                }
            } else if (cmp > 0) {
                if (right == null) {
                    rightHeight = 1;
                    right = node;
                    right.parent = this;
                    return null;
                } else {
                    V ret = this.right.add(node);
                    if (ret == null)
                        rightHeight = right.getHeight();
                    return ret;
                }
            } else {
                return this.setValue(node.value);
            }
        }

        public int hashCode() {
            int keyHash = (key == null ? 0 : key.hashCode());
            int valueHash = (value == null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value;
        }
        
        //Added equals method for comparing stuff
        public boolean equals(Object o) {
        	if (!(o instanceof Map.Entry))
        		return false;
        		Map.Entry<K,V> e = (Map.Entry)o;
    			return eq(key, e.getKey()) && eq(value, e.getValue());
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public int getHeight() {
            return 1 + Math.max(leftHeight, rightHeight);
        }

        public int getBalance() {
            return leftHeight - rightHeight;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        @SuppressWarnings({ "unchecked" })
        private int compare(Object k1, Object k2) {
            return comparator == null ? ((Comparable<? super K>) k1)
                    .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
        }
        
        /* Mikhail - Make this to make the coordinate for a point easier.*/
        private String coordinateToString(City cityKey) {
        	return "(" + (int)cityKey.getPoint().getX() + "," + (int)cityKey.getPoint().getY() + ")";
        }

        public Node buildXmlNode(final Node parent) {
            final Element e = parent.getOwnerDocument().createElement("node");
            e.setAttribute("key", key.toString());
            e.setAttribute("value", coordinateToString((City)key));

            if (left != null) {
                e.appendChild(left.buildXmlNode(e));
            } else {
                e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
            }

            if (right != null) {
                e.appendChild(right.buildXmlNode(e));
            } else {
                e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
            }
            return e;
        }
    }

    private final AvlNode<K, V> getNode(Object key) {
        AvlNode<K, V> p = root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }

    private final boolean nodeContainsValue(AvlNode<K, V> node, Object value) {
        if (node == null)
            return false;

        if (node.value.equals(value))
            return true;
        else
            return nodeContainsValue(node.left, value)
                    || nodeContainsValue(node.right, value);
    }

    private void fixAfterModification(AvlNode<K, V> e) {
        if (e.getBalance() > g) {
            if (e.left.getBalance() >= 0)
                e = rotateRight(e);
            else
                e = rotateLeftRight(e);
        } else if (e.getBalance() < -g) {
            if (e.right.getBalance() <= 0)
                e = rotateLeft(e);
            else
                e = rotateRightLeft(e);
        }

        if (e.parent != null)
            fixAfterModification(e.parent);
        else
            this.root = e;
    }

    private AvlNode<K, V> rotateRight(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> l = p.left;
        p.left = l.right;
        if (l.right != null)
            l.right.parent = p;
        l.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.right == p)
                p.parent.right = l;
            else
                p.parent.left = l;
        }
        l.right = p;
        p.parent = l;

        p.leftHeight = l.rightHeight;
        l.rightHeight = p.getHeight();
        updateHeight(l);
        return l;
    }

    private AvlNode<K, V> rotateLeft(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
        }
        r.left = p;
        p.parent = r;

        p.rightHeight = r.leftHeight;
        r.leftHeight = p.getHeight();
        updateHeight(r);
        return r;

    }

    private AvlNode<K, V> rotateRightLeft(AvlNode<K, V> p) {
        p.right = rotateRight(p.right);
        return rotateLeft(p);
    }

    private AvlNode<K, V> rotateLeftRight(AvlNode<K, V> p) {
        p.left = rotateLeft(p.left);
        return rotateRight(p);
    }

    private void updateHeight(AvlNode<K, V> n) {
        if (n.parent == null)
            return;

        if (n.parent.left == n)
            n.parent.leftHeight = n.getHeight();
        else
            n.parent.rightHeight = n.getHeight();

        if (n.parent != null)
            updateHeight(n.parent);

    }

    @SuppressWarnings("unchecked")
    private final int compare(Object k1, Object k2) {
        return comparator == null ? ((Comparable<? super K>) k1)
                .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }
   
    public Node createXml(final Node parent) {
        final Element rootNode = parent.getOwnerDocument().createElement(
                "AvlGTree");
        rootNode.setAttribute("height",
                root == null ? "-1" : String.valueOf(root.getHeight()-1));
        rootNode.setAttribute("maxImbalance", String.valueOf(g));
        rootNode.setAttribute("cardinality", String.valueOf(size()));
        rootNode.appendChild(root == null ? parent.getOwnerDocument()
                .createElement("emptyChild") : root.buildXmlNode(rootNode));
        return rootNode;
    }

	@Override
	public K firstKey() {
		AvlNode<K, V> firstElem = root;
		if (firstElem == null)
			throw new NoSuchElementException();
		else 
			while (firstElem.left != null)
				//This gets the leftmost, or lowest element
				firstElem = firstElem.left;
			return firstElem.key;
	}

	@Override
	public SortedMap<K, V> headMap(K arg0) {
		return null;
	}

	@Override
	public K lastKey() {
		AvlNode<K, V> lastElem = root;
		if (lastElem == null)
			throw new NoSuchElementException();
		else 
			while (lastElem.right != null)
				//This gets the leftmost, or lowest element
				lastElem = lastElem.right;
			return lastElem.getKey();
	}

	//This class must return a SortedMap class, so must make one in the
	//first place.
	@Override
	public SortedMap<K, V> subMap(K startKey, K endKey) {
		return new SubMap(startKey, endKey);
	}
	
	protected class SubMap extends AbstractMap<K, V> implements SortedMap<K,V> {

		K startKey;
		K endKey;
		
		public SubMap(K startKey, K endKey) {
			if (startKey == null || endKey == null)
				throw new NullPointerException("Cannot have null keys!");
			if (AvlGTree.this.compare(startKey, endKey) > 0)
				throw new IllegalArgumentException("StartKey must be less than EndKey.");
			this.startKey = startKey;
			this.endKey = endKey;
		}

		@Override
		public void clear() {
			AvlGTree.this.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return AvlGTree.this.containsKey(key);
		}

		@Override
		public boolean containsValue(Object o) {
			return containsValueHelper(AvlGTree.this.root, o);
		}
		
		private boolean containsValueHelper(AvlNode<K, V> node, Object val) {
			V value = (V) val;
			if (node == null) {
				return false;
			} else {
				return (node.value.equals(value)) ? true : this.containsValueHelper(node.left, value) || this.containsValueHelper(node.right, value);
			}
		}

		@Override
		public V get(Object key) {
			K key2 = (K)key;
			AvlNode<K,V> value = findKey(key2);
			return value == null ? null : value.value;
		}
		
		private AvlNode<K,V> findKey(K Key) {
			return null;
		}
		
		public int hashCode() {
			return System.identityHashCode(this); 
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		//TODO: Implement in part 3
		@Override
		public Object put(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		//TODO: Implement in part 3
		@Override
		public void putAll(Map m) {
			throw new UnsupportedOperationException();
		}

		//TODO: Implement in part 3
		@Override
		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Comparator comparator() {
			return AvlGTree.this.comparator;
		}

		@Override
		public Set<Entry<K,V>> entrySet() {
			return new AbstractSet<Entry<K,V>>() {

				// Returns true if this set contains the specified element.
				public boolean contains(Object o) {
					return SubMap.this.containsKey(o);
				}
				
				@Override
				public int size() {
					return SubMap.this.size();
				}
				
				//Looked at the GC: AbstractMap class on grep
				final class SubmapIterator implements Iterator<Map.Entry<K, V>> {
					AvlNode<K, V> next;
					int expectedModCount;
					
					SubmapIterator(AvlNode<K,V> firstElem) {
						next = firstElem;
						expectedModCount = modCount;
					}

					@Override
					public boolean hasNext() {
						return next != null;
					}

					@Override
					public Map.Entry<K, V> next() {
						AvlNode<K, V> elem = next;
						if (hasNext()) {
							next = getNextElem(elem);
						}
						return elem;
					}
					
					AvlNode<K, V> getNextElem(AvlNode<K, V> curr) {
						if (curr == null) 
							throw new NoSuchElementException();
						if (modCount != expectedModCount)
							throw new ConcurrentModificationException();
						if (curr.right != null) {
							AvlNode<K, V> parent = curr.right;
							while (parent.left != null)
								parent = parent.left;
							return parent;
						} else {
							AvlNode<K, V> parent = curr.parent;
							AvlNode<K, V> child = curr;
							while (parent != null && child == parent.right) {
								child = parent;
								parent = parent.parent;
							}
							return parent;
						}
					}
				}
				
				@Override
				public Iterator<Entry<K,V>> iterator() {
					AvlNode<K, V> firstElem = root;
					if (firstElem != null)
						while (firstElem.left != null)
							//This gets the leftmost, or lowest element
							firstElem = firstElem.left;
					return new SubmapIterator(firstElem);
				}
				
				// Compares the specified object with this set for equality.
				public boolean equals(Object o) {
					if (!(o instanceof Collection))
						return false;
					Collection s = (Collection) o;
					if (s.size() == SubMap.this.size() && this.containsAll(s))
						return true;
					return false;
				}

				// Returns the hash code value for this set.
				public int hashCode() {
					return System.identityHashCode(this);
				}

				// Returns true if this set contains no elements.
				public boolean isEmpty() {
					return SubMap.this.size() == 0;
				}
				
			};
		}
		

		@Override
		public int size() {
			return sizeHelper(AvlGTree.this.root);
		}
		
		private int sizeHelper(AvlNode<K, V> currentNode) {
			if (currentNode == null) {
				return 0;
			}
			
			int subSize = 0;
			if (AvlGTree.this.compare(startKey, currentNode.key) > 0 || AvlGTree.this.compare(endKey, currentNode.key) <= 0) {
				subSize = 0;
			} else {
				subSize = 1;
			}
			if (AvlGTree.this.comparator.compare(currentNode.key, startKey) > 0) {
				subSize += this.sizeHelper(currentNode.left);
			}
			if (AvlGTree.this.comparator.compare(currentNode.key, endKey) < 0) {
				subSize += this.sizeHelper(currentNode.right);
			}
			
			return subSize;
		}

		@Override
		public K firstKey() {
			if (size() <= 0 || AvlGTree.this.root == null) {
				throw new NoSuchElementException();
			} else {
				return firstKeyHelper(AvlGTree.this.root);
			}
		}
		
		private K firstKeyHelper(AvlNode<K, V> currNode) {
			return currNode.left == null ? currNode.key : this.firstKeyHelper(currNode.left);
		}

		//TODO: Implement in part 3
		@Override
		public SortedMap headMap(Object toKey) {
			throw new UnsupportedOperationException();
		}

		//TODO: Implement in part 3
		@Override
		public Set keySet() {
			throw new UnsupportedOperationException();
		}

		@Override
		public K lastKey() {
			if (size() <= 0 || AvlGTree.this.root == null) {
				throw new NoSuchElementException();
			} else {
				return lastKeyHelper(AvlGTree.this.root);
			}
		}
		
		private K lastKeyHelper(AvlNode<K, V> currNode) {
			return currNode.right == null ? currNode.key : this.firstKeyHelper(currNode.right);
		}

		@Override
		public SortedMap<K,V> subMap(K fromKey, K toKey) {
			if (AvlGTree.this.compare(startKey, fromKey) > 0 || AvlGTree.this.compare(endKey, toKey) <= 0) 
				throw new IllegalArgumentException("StartKey must be less than EndKey.");
			return AvlGTree.this.subMap(fromKey, toKey);
		}

		//TODO: Implement in part 3
		@Override
		public SortedMap tailMap(Object fromKey) {
			throw new UnsupportedOperationException();
		}

		//TODO: Implement in part 3
		@Override
		public Collection values() {
			throw new UnsupportedOperationException();
		}

		
	}

	//TODO: Implement in part 3
	@Override
	public SortedMap<K, V> tailMap(K arg0) {
		throw new UnsupportedOperationException();
	}

	//Looked at the GC: AbstractMap class on grep
	final class AvlNodeIterator implements Iterator<Map.Entry<K, V>> {
		AvlNode<K, V> next;
		int expectedModCount;
		
		AvlNodeIterator(AvlNode<K,V> firstElem) {
			next = firstElem;
			expectedModCount = modCount;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Map.Entry<K, V> next() {
			AvlNode<K, V> elem = next;
			if (hasNext()) {
				next = getNextElem(elem);
			}
			return elem;
		}
		
		AvlNode<K, V> getNextElem(AvlNode<K, V> curr) {
			if (curr == null) 
				throw new NoSuchElementException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			if (curr.right != null) {
				AvlNode<K, V> parent = curr.right;
				while (parent.left != null)
					parent = parent.left;
				return parent;
			} else {
				AvlNode<K, V> parent = curr.parent;
				AvlNode<K, V> child = curr;
				while (parent != null && child == parent.right) {
					child = parent;
					parent = parent.parent;
				}
				return parent;
			}
		}
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K, V>>() {

			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				AvlNode<K, V> firstElem = root;
				if (firstElem != null)
					while (firstElem.left != null)
						//This gets the leftmost, or lowest element
						firstElem = firstElem.left;
				return new AvlNodeIterator(firstElem);
			}

			@Override
			public int size() {
				return AvlGTree.this.size();
			}
			
			//Taken and adopted from projectbook class notes
			public boolean remove(Object o) {
				try {
	            Map.Entry me = (Map.Entry)o;

	            boolean b = AvlGTree.this.containsKey(me.getKey());
	            AvlGTree.this.remove(me.getKey());
	            return b;
				} catch (ClassCastException e) {
					return false;
				}
	            // throws a ClassCastException if this fails,           
	            // as per the API for Set
	        }
	        public boolean contains(Object o) {
	            Map.Entry me = (Map.Entry)o;
	            return AvlGTree.this.containsKey(me.getKey()) &&
	                (me.getValue() == null ?
	                    AvlGTree.this.get(me.getKey()) == null :
	                    me.getValue().equals(AvlGTree.this.get(me.getKey())));
	        }
			
		};
	}
}