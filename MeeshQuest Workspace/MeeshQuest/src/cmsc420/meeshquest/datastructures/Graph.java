package cmsc420.meeshquest.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import cmsc420.meeshquest.citymapobjects.City;

/*Author: Mikhail Sorokin, University of Maryland College Park
 msorokin, 113198021, Discussion section: 0203. */

///Summary of Graph Class - FROM 132

/**Summary:
 * This Graph class contains a set of vertices and a set of edges. Vertices are
 * represented by the generic type V, which means it could be an integer,
 * a string character, etc. Edges are ordered pairs of vertices with weights or
 *  costs.associated with each edge lying in between two vertices.
 */

public class Graph<V> {

	  public int hops = 0;	
	  /*All the vertices are stored in the vertices list*/
	  private List<V> allVertices;
	  /*The edge's source, dest and cost is stored in this 
	   * arraylis, which also uses methods found in the 
	   * Edge class itself..*/
	  private List<Edge<V>> edges;
	  
	  private Set<V> settledNodes;
      private Set<V> unSettledNodes;
      private Map<V, V> predecessors;
      private Map<V, Double> distance;
	
	  /**
	   * Class constructor creating a Graph object in order to create a 
	   * way to store vertices and edges.
	   */
	  public Graph() {
	    allVertices = new LinkedList<V>();
	    edges = new LinkedList<Edge<V>>();
	  }
	
	  /**
	   * This method adds a vertex with the parameter 
	   * data vertex onto the current graph object
	   *
	   * @param vertex - data to add to current object Graph
	   */
	  public void addVertex(V vertex) {
	    if (vertex == null) {
	      throw new NullPointerException();
	    } else if (!allVertices.contains(vertex)) {
	      allVertices.add(vertex);
	    }
	  }
	
	  /**
	   * This method should return true if the current Graph object contains a
	   * vertex. Else, it will return false.
	   *
	   * @param vertex
	   *          data to check if this exact vertex is contained within the current
	   *          graph object
	   */
	  public boolean isVertex(V vertex) {
	    if (vertex == null) {
	      throw new NullPointerException();
	    } else
	      return (allVertices.contains(vertex)) ? true : false;
	  }
	
	  /**
	   * This method should return a LinkedList which implements 
	   * Java's Collection interface, and contains all of the vertices in 
	   * the current object graph. If it doesn't have any vertices, then 
	   * an empty collection with no elements should be returned.
	   * 
	   * @return the current collection which holds all the vertices
	   */
	  public Collection<V> getVertices() {
	    return allVertices;
	  }
	
	  /**
	   * This method should remove the vertex from its current object graph. If
	   * vertex is not in the graph, then the method should just throw a
	   * NoSuchElementException, otherwise it should just remove vertex.
	   *
	   * @param vertex
	   *          data to remove if it exists in the Graph object
	   */
	  public void removeVertex(V vertex) throws NoSuchElementException {
	    /*
	     * Since a vertex cannot exist without an edge, I would have to remove
	     * the edges that contain these vertices.
	     */
	    List<Edge<V>> edgesToRemove = new LinkedList<Edge<V>>();
	    if (allVertices.contains(vertex)) {
	      allVertices.remove(vertex);
	      for (Edge<V> edge : edges) {
	        if (edge.getSource().equals(vertex) || 
	            edge.getDest().equals(vertex)) {
	          edgesToRemove.add(edge);
	        }
	      }
	      //Remove all edges that the current vertex is a part of.
	      edges.removeAll(edgesToRemove);
	    } else
	      throw new NoSuchElementException();
	  }
	
	  /**
	   * This method should add a new edge onto its current object graph
	   * which goes from vertex source to vertex dest (directed edge). 
	   * If the edge is already in the graph, otherwise it should just 
	   * remove vertex.
	   *
	   * @param vertex
	   *          data to remove if it exists in the Graph object
	   * @param dest
	   *          the final destination
	   * @param cost
	   *          the distane between the source and dest
	   */
	  public void addEdge(V source, V dest, double cost)
	      throws IllegalArgumentException {
	    if (source == null || dest == null) {
	      throw new NullPointerException();
	    }
	    //If there is no source or dest in the vertices, then create new
	    //vertexes that have source and dest.
	    if (!allVertices.contains(source)) {
	      allVertices.add(source);
	    } if (!allVertices.contains(dest)) {
	      allVertices.add(dest);
	    }
	    Edge<V> edge = new Edge<V>(source, dest, cost);
	    if (edges.contains(edge) || cost < 0) {
	      /*
	       * There cannot be duplicate edges and the cost cannot be negative, so an
	       * exception is thrown
	       */
	      throw new IllegalArgumentException();
	    } else
	      edges.add(edge);
	  }
	  
	  public boolean containsEdge(V source, V dest) {
		  for (Edge<V> edge: edges) {
			  City start = (City)edge.getSource();
			  City end = (City)edge.getDest();
			  
			  City sourceCity = (City)source;
			  City destCity = (City)dest;
			  
			  if (start.getName().compareTo(sourceCity.getName()) == 0
					  && end.getName().compareTo(destCity.getName()) == 0) {
				  return true;
			  }
		  }
	
		  return false;
	  }
	  
	  public boolean containsSourceVertexInEdge(V source) {
		  for (Edge<V> edge: edges) {
			  City start = (City)edge.getSource();
			  
			  City sourceCity = (City)source;
			  
			  if (start.getName().compareTo(sourceCity.getName()) == 0) {
				  return true;
			  }
		  }
	
		  return false;
	  }
	  
	  public boolean containsDestVertexInEdge(V dest) {
		  for (Edge<V> edge: edges) {
			  City end = (City)edge.getDest();
			  
			  City destCity = (City)dest;
			  
			  if (end.getName().compareTo(destCity.getName()) == 0) {
				  return true;
			  }
		  }
	
		  return false;
	  }
	
	  /**
	   * This method should check if there is an edge with the same source
	   *  and dest as there is inside the edges arraylist and then return the
	   *  cost of that edge.
	   *
	   * @param vertex
	   *          data to remove if it exists in the Graph object
	   * @param dest
	   *          the end of which the source connects to
	   * @param cost
	   *          the distance between the source and dest
	   */
	  public double getEdgeCost(V source, V dest) {
	    for (Edge<V> edge : edges) {
	      if (edge.getDest().equals(dest) && edge.getSource().equals(source)) {
	        return edge.getWeight();
	      }
	    }
	    return -1;
	  }
	
	  /**
	   * This method should modify the cost of the edge in its current object graph
	   * that goes from vertex source to vertex dest so that it has cost newCost
	   * instead of its current value.
	   * 
	   * @param source
	   *          is where the edge originates
	   * @param dest
	   *          is where the edge ends
	   * @param newCost
	   *          is the newCost between the edges
	   * @throw IllegalArgumentException if the cost is negative
	   * @throw NoSuchElementException if there is no edge that exists
	   *  with the source and dest in the parameter
	   * */
	  public void changeEdgeCost(V source, V dest, double newCost)
	      throws IllegalArgumentException, NoSuchElementException {
	    int count = 0;
	    /*The cost can never be negative.*/
	    if (newCost < 0) {
	      throw new IllegalArgumentException();
	    }
	    for (Edge<V> edge : edges) {
	      //An edge exists in the edges list if there is a source 
	      //and a dest that is contained somewhere in the edges list
	      if (edge.getDest().equals(dest) && 
	          edge.getSource().equals(source)) {
	        edge.setCost(newCost);
	        count++;
	      }
	    }
	    //If there was no edge that was in the edges list, then a
	    //NoSuchElementException should be thrown
	    if (count == 0) {
	      throw new NoSuchElementException();
	    }
	  }
	
	  /**
	   * This method should remove the edge going from source 
	   * to dest in its current object graph.
	   * 
	   * @param source
	   *          where the edge originates
	   * @param dest
	   *          where the edge ends
	   * @throws NoSuchElementException
	   *           if either source or dest are not present in the graph
	   */
	  public void removeEdge(V source, V dest) 
	      throws NoSuchElementException {
	    int count = 0;
	    Edge<V> oneToRemove = null;
	    /**
	     * Get a reference to the one element to remove in order to see 
	     * if that element even exists. If it does exist, then get a reference 
	     * to the current edge in the for loop and remove that edge outside 
	     * the for loop.
	     */
	    for (Edge<V> edge : edges) {
	      if (edge.getDest().equals(dest) && edge.getSource().equals(source)) {
	        oneToRemove = edge;
	        count++;
	      }
	    }
	    if (count == 1) {
	      edges.remove(oneToRemove);
	    } else
	      throw new NoSuchElementException();
	  }
	
	  /**
	   * This method should return some type of object of a class that implements
	   * Java's Collection interface, which contains all of the neighbors of the
	   * parameter vertex in its current object graph.
	   * 
	   * @param vertex
	   *          the vertex to get the neighbors around
	   * @return the collection of neighbors of the parameter vertex
	   * @throws IllegalArgumentException
	   *           if vertex is present but has no neighbors
	   */
	  private Collection<V> getNeighbors(V node) {
          List<V> neighbors = new ArrayList<V>();
          for (Edge<V> edge : edges) {
                  if (edge.getSource().equals(node)
                                  && !isSettled(edge.getDest())) {
                          neighbors.add(edge.getDest());
                  }
          }
          return neighbors;
  }

	
	  /**
	   * This method should return some type of object of a class that 
	   * implements Java's Collection interface, which contains all of 
	   * the predecessors of the parameter vertex in its current object graph.
	   * 
	   * @param vertex
	   *          is the original vertex to get information about the predecessors
	   * @return the collection of predecessors of the parameter vertex
	   * @throws IllegalArgumentException
	   *           if vertex is present, but has no predecessors
	   */
	  public Collection<V> getPredecessors(V vertex)
	      throws IllegalArgumentException {
	    boolean atLeastOne = false;
	    List<V> predecessors = new LinkedList<V>();
	    for (Edge<V> edge : edges) {
	      if (edge.getDest().equals(vertex) && 
	          isVertex(edge.getSource())) {
	        predecessors.add(edge.getSource());
	        atLeastOne = true;
	      }
	    }
	    if (atLeastOne) {
	      return predecessors;
	    } else
	      throw new IllegalArgumentException();
	  }
	
	  /**
	   * This method should return true if its current object graph is a clique, and
	   * false if not. A clique is a graph in which every vertex has an edge to
	   * every other vertex.
	   * 
	   * @return true if current object graph contains a clique
	   */
	  public boolean isClique() {
	    /**
	     * There are two counts, which differentiate in the fact that
	     * one count will count all the vertices that point to all
	     * the other vertices in the graph. The countitself variable
	     * only counts the variables that have an edge pointing
	     * to itself.
	     */
	    int countVertices = 0, countItself = 0;
	    for (V vertex : allVertices) {
	      for (Edge<V> edge : edges) {
	        if (edge.getSource().equals(vertex)
	            || edge.getSource().equals(edge.getDest())) {
	          countVertices++;
	        }
	        if (edge.getSource().equals(edge.getDest())) {
	          countItself++;
	        }
	      }
	    }
	    if (countVertices == allVertices.size()
	        || countVertices == allVertices.size() + countItself) {
	      return true;
	    } else {
	      return false;
	    }
	  }
	
	  /**
	   * This method should determine the shortest path from the starting 
	   * vertex to every other vertex. The return value will be the cost of 
	   * the path between source and dest. If source and dest are the same
	   * vertex, then 0 should be returned, and shortestPath should become
	   * an empty list containing no elements.
	   * 
	   * @param sourceVertex
	   *          this is where the pathfinding starts
	   * @param destVertex
	   *          this is where the pathfinding should end
	   * @param shortestPath
	   *          the actual shortest path between source dest, meaning 
	   *          the vertices that comprise that path. If source and dest 
	   *          are the same vertex, then 0 should be returned. Will 
	   *          either contain 0 or 2 and greater vertices.
	   * @return the cost for path between source and dest
	   * @throws IllegalArgumentException
	   *           if either source or dest are not in the graph
	   */
	  public double dijkstra(V sourceVertex, V destVertex) {
		  settledNodes = new HashSet<V>();
          unSettledNodes = new HashSet<V>();
          distance = new HashMap<V, Double>();
          predecessors = new HashMap<V, V>();

          distance.put(sourceVertex, 0.0);
          unSettledNodes.add(sourceVertex);
          while (unSettledNodes.size() > 0) {
                  V node = getMinimum(unSettledNodes);
                  settledNodes.add(node);
                  unSettledNodes.remove(node);
                  findMinimalDistances(node, destVertex);
          }
	   
          if (distance.get(destVertex) == null) {
        	  return -1.0;
          }
          return distance.get(destVertex);
	  } 
	  
	  /*Everything below has been taken and adapted from http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html*/
	  private void findMinimalDistances(V node, V lastV) {
		  Collection<V> adjacentNodes = getNeighbors(node);
          for (V target : adjacentNodes) {
                  if (getShortestDistance(target) > getShortestDistance(node)
                                  + getDistance(node, target)) {
                          distance.put(target, getShortestDistance(node)
                                          + getDistance(node, target));
                          predecessors.put(target, node);
                          unSettledNodes.add(target);
                  }
          }
	  }
	  
	  private double getDistance(V node, V target) {
	          for (Edge<V> edge : edges) {
	                  if (edge.getSource().equals(node)
	                                  && edge.getDest().equals(target)) {
	                          return edge.getWeight();
	                  }
	          }
	          throw new RuntimeException("Should not happen");
	  }
	
	  private V getMinimum(Set<V> vertexes) {
	          V minimum = null;
	          for (V vertex : vertexes) {
	                  if (minimum == null) {
	                          minimum = vertex;
	                  } else {
	                          if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
	                                  minimum = vertex;
	                          }
	                  }
	          }
	          return minimum;
	  }
	
	  private boolean isSettled(V vertex) {
	          return settledNodes.contains(vertex);
	  }
	
	  private double getShortestDistance(V destination) {
	          Double d = distance.get(destination);
	          if (d == null) {
	                  return Double.MAX_VALUE;
	          } else {
	                  return d;
	          }
	  }
	
	  /*
	   * This method returns the path from the source to the selected target and
	   * NULL if no path exists
	   */
	  public LinkedList<V> getPath(V target) {
	          LinkedList<V> path = new LinkedList<V>();
	          V step = target;
	          // check if a path exists
	          if (predecessors.get(step) == null) {
	              return null;
	          }
	          path.add(step);
	          while (predecessors.get(step) != null) {
	                  step = predecessors.get(step);
	                  path.add(step);
	          }
	          // Put it into the correct order
	          Collections.reverse(path);
	          return path;
	  }
	  
	  
	  public String toString() {
	    String empty = "";
	    for (Edge<V> edge : edges) {
	      empty += "Source: {" + edge.getSource() + "} Dest: {" + edge.getDest()
	          + "} Cost: {" + edge.getWeight() + "}" + "\n";
	    }
	    return empty;
	  }
	
}
