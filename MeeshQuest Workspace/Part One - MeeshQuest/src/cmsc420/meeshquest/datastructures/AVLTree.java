package cmsc420.meeshquest.datastructures;

import java.util.Comparator;

import org.w3c.dom.Element;

import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.utilities.XmlParser;

/*
 *  We are requried to use an AVL Tree from an outside source.
 *  Java Program to Implement AVL Tree. Taken from SanFoundry.
 *  Modifications made by Mikhail Sorokin.
 */
public class AVLTree implements Comparator<City>
{
    private AVLNode root;     

    /* Constructor */
    public AVLTree()
    {
        root = null;
    }
    /* Function to check if tree is empty */
    public boolean isEmpty()
    {
        return root == null;
    }
    /* Make the tree logically empty */
    public void makeEmpty()
    {
        root = null;
    }
    /* Function to insert data */
    public void insert(City data)
    {
        root = insert(data, root);
    }
    
    /* Function to get height of root */
    public int height()
    {
        return height(root);
    }
    
    /* Function to get height of node */
    private int height(AVLNode t )
    {
        return t == null ? -1 : t.height;
    }
    /* Function to max of left/right node */
    private int max(int lhs, int rhs)
    {
        return lhs > rhs ? lhs : rhs;
    }
    
	@Override
	public int compare(City inserted, City data) {
		return data.getName().compareTo(inserted.getName());
	}   
    
    /* Function to insert data recursively */
    private AVLNode insert(City x, AVLNode t)
    {
        if (t == null)
            t = new AVLNode(x);
        else if (compare(x, t.data) < 0)
        {
            t.left = insert( x, t.left );
            if( height( t.left ) - height( t.right ) == 2 )
                if(compare(x, t.left.data) < 0)
                    t = rotateWithLeftChild( t );
                else
                    t = doubleWithLeftChild( t );
        }
        else if(compare(x, t.data) > 0)
        {
            t.right = insert( x, t.right );
            if( height( t.right ) - height( t.left ) == 2 )
                if(compare(x, t.right.data) > 0)
                    t = rotateWithRightChild( t );
                else
                    t = doubleWithRightChild( t );
        }
        else
          ;  // Duplicate; do nothing
        t.height = max( height( t.left ), height( t.right ) ) + 1;
        return t;
    }
    /* Rotate binary tree node with left child */     
    private AVLNode rotateWithLeftChild(AVLNode k2)
    {
        AVLNode k1 = k2.left;
        k2.left = k1.right;
        k1.right = k2;
        k2.height = max( height( k2.left ), height( k2.right ) ) + 1;
        k1.height = max( height( k1.left ), k2.height ) + 1;
        return k1;
    }

    /* Rotate binary tree node with right child */
    private AVLNode rotateWithRightChild(AVLNode k1)
    {
        AVLNode k2 = k1.right;
        k1.right = k2.left;
        k2.left = k1;
        k1.height = max( height( k1.left ), height( k1.right ) ) + 1;
        k2.height = max( height( k2.right ), k1.height ) + 1;
        return k2;
    }
    /**
     * Double rotate binary tree node: first left child
     * with its right child; then node k3 with new left child */
    private AVLNode doubleWithLeftChild(AVLNode k3)
    {
        k3.left = rotateWithRightChild( k3.left );
        return rotateWithLeftChild( k3 );
    }
    /**
     * Double rotate binary tree node: first right child
     * with its left child; then node k1 with new right child */      
    private AVLNode doubleWithRightChild(AVLNode k1)
    {
        k1.right = rotateWithLeftChild( k1.right );
        return rotateWithRightChild( k1 );
    }    
    /* Functions to count number of nodes */
    public int countNodes()
    {
        return countNodes(root);
    }
    private int countNodes(AVLNode r)
    {
        if (r == null)
            return 0;
        else
        {
            int l = 1;
            l += countNodes(r.left);
            l += countNodes(r.right);
            return l;
        }
    }
    /* Functions to search for an element */
    public boolean search(City val)
    {
        return search(root, val);
    }
    private boolean search(AVLNode r, City val)
    {
        boolean found = false;
        while ((r != null) && !found)
        {
            City rval = r.data;
            if (compare(val, rval) < 0)
                r = r.left;
            else if (compare(val, rval) > 0)
                r = r.right;
            else
            {
                found = true;
                break;
            }
            found = search(r, val);
        }
        return found;
    }
    /* Function for inorder traversal */
    public void inorder()
    {
        inorder(root);
    }
    private void inorder(AVLNode r)
    {
        if (r != null)
        {
            inorder(r.left);
            //TODO: Call element stuff
            inorder(r.right);
        }
    }
    /* Function for preorder traversal */
    public void preorder(Element parentElement)
    {
        preorder(root, parentElement);
    }
    private void preorder(AVLNode r, Element parentElement)
    {
        if (r != null)
        {
        	Element nodeElement = XmlParser.results.createElement("node");
        	nodeElement.setAttribute("name", r.data.getName());
        	nodeElement.setAttribute("radius", Integer.toString(r.data.getRadius()));
        	parentElement.appendChild(nodeElement);
            preorder(r.left, nodeElement);             
            preorder(r.right, nodeElement);
        } else {
        	//If Null, create empty children Node
        	Element emptyChildElement = XmlParser.results.createElement("emptyChild"); 
        	parentElement.appendChild(emptyChildElement);
        }
    }
    /* Function for postorder traversal */
    public void postorder()
    {
        postorder(root);
    }
    private void postorder(AVLNode r)
    {
        if (r != null)
        {
            postorder(r.left);             
            postorder(r.right);
            System.out.print(r.data +" ");
        }
    }
  
}