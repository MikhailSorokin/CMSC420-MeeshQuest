package cmsc420.meeshquest.junittests;

import java.util.TreeMap;

import org.junit.Test;

import cmsc420.meeshquest.datastructures.StringComparator;
import cmsc420.sortedmap.AvlGTree;

public class AvlGTreeTests {

	@Test
	public void testBasics() {
		AvlGTree<String, Integer> avl = new AvlGTree<String, Integer>(new StringComparator(),1);
        TreeMap<String, Integer> treemap = new TreeMap<String, Integer>(new StringComparator());
        
        for (int i = 0; i < 10; i++) {
            String key = "key"+i;
            avl.put(key, i);
        }
        for (int i = 0; i < 10; i++) {
            String key = "key"+i;
            treemap.put(key, i);
        }

        assert(avl.equals(treemap));
        assert(avl.toString().equals(treemap.toString()));
        
        System.out.println(avl.lastKey());
        System.out.println(treemap.lastKey());
        assert(avl.firstKey().equals(treemap.firstKey().toString()));
	}
	
}