package cmsc420.meeshquest.datastructures;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.utilities.XmlParser;

/**
 * E. Wang's AVL-g tree implementation.
 */
public class AvlGTree<K, V> {
    
	public final int g;
   
    private Comparator<? super K> comparator = null;
    private AvlNode<K, V> root = null;
    private long size = 0;

    public AvlGTree() {
        this.g = XmlParser.maxImbalance;
    }

    public AvlGTree(final Comparator<? super K> comp) {
        this.comparator = comp;
        this.g = XmlParser.maxImbalance;
    }

    public AvlGTree(final int g) {
        this.g = XmlParser.maxImbalance;
    }

    public AvlGTree(final Comparator<? super K> comp, final int g) {
        this.comparator = comp;
        this.g = XmlParser.maxImbalance;
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
            return null;
        }
        AvlNode<K, V> e = new AvlNode<K, V>(key, value, comparator);
        V oldValue = root.add(e);

        if (oldValue == null) {
            fixAfterModification(e);
            size++;
            return null;
        } else {
            return oldValue;
        }
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    static final class AvlNode<K, V> {
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

        public Node buildXmlNode(final Node parent) {
            final Element e = parent.getOwnerDocument().createElement("node");
            e.setAttribute("name", key.toString());
            e.setAttribute("radius", value.toString());

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
    
    /* Function for preorder traversal */
    public void preorder(Element parentElement)
    {
        preorder(root, parentElement);
    }
    private void preorder(AvlNode<K, V> r, Element parentElement)
    {
        if (r != null)
        {
        	Element nodeElement = XmlParser.results.createElement("node");
        	nodeElement.setAttribute("key", (String) ((City) r.getKey()).getName());
        	nodeElement.setAttribute("value", "(" + (int)((City) r.getKey()).getX() + "," + (int)((City) r.getKey()).getY() + ")");
        	parentElement.appendChild(nodeElement);
            preorder(r.left, nodeElement);             
            preorder(r.right, nodeElement);
        } else {
        	//If Null, create empty children Node
        	Element emptyChildElement = XmlParser.results.createElement("emptyChild"); 
        	parentElement.appendChild(emptyChildElement);
        }
    }
	
}