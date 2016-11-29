package cmsc420.meeshquest.datastructures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;


public class Graph {
   private final Map<String, Vertex> graph; // mapping of vertex names to Vertex objects, built from a set of Edges
 
   /** One edge of the graph (only used by Graph constructor) */
   public static class Edge {
      public final String v1, v2;
      public final double dist;
      public Edge(String v1, String v2, int dist) {
         this.v1 = v1;
         this.v2 = v2;
         this.dist = dist;
      }
   }
 
   /** One vertex of the graph, complete with mappings to neighbouring vertices */
  public static class Vertex implements Comparable<Vertex>{
	public final String name;
	public double dist = Double.MAX_VALUE; // MAX_VALUE assumed to be infinity
	public Vertex previous = null;
	public final Map<Vertex, Double> neighbours = new HashMap<>();
    private static LinkedList<String> path = new LinkedList<String>();;
	
	
	public Vertex(String name)
	{
		this.name = name;
	}
 
	public int compareTo(Vertex other)
	{
		if (dist == other.dist)
			return name.compareTo(other.name);
 
		return Double.compare(dist, other.dist);
	}
 
	@Override public String toString()
	{
		return "(" + name + ", " + dist + ")";
	}
	
	   
   private void printPath()
	{
		if (this == this.previous)
		{
			path.add(this.name);
		}
		else if (this.previous == null)
		{
			path.clear();
		}
		else
		{
			this.previous.printPath();
			path.add(this.name);
		}
	}
	   
}
  
  public Graph() {
	  graph = new HashMap<String, Vertex>();
  }
  
  public boolean containsSourceVertexInEdge(String source) {
	  for (Entry<Vertex, Double> edge: graph.get(source).neighbours.entrySet()) {
		  if (source.compareTo(edge.getKey().name) == 0) {
			  return true;
		  }
	  }

	  return false;
  }
  
  public boolean containsDestVertexInEdge(String dest) {
	  for (Entry<Vertex, Double> edge: graph.get(dest).neighbours.entrySet()) {
		  if (dest.compareTo(edge.getKey().name) == 0) {
			  return true;
		  }
	  }

	  return false;
  }

   public void addVertex(String cityName) {
       if (!graph.containsKey(cityName)) graph.put(cityName, new Vertex(cityName));
   }
   
   public void addEdge(String startName, String endName, double dist) {
       if (!graph.containsKey(startName)) graph.put(startName, new Vertex(startName));
       if (!graph.containsKey(endName)) graph.put(endName, new Vertex(endName));
       graph.get(startName).neighbours.put(graph.get(endName), dist);
       graph.get(endName).neighbours.put(graph.get(startName), dist);
   }
   
   public boolean containsVertex(String cityName) {
	   if (graph.containsKey(cityName)) {
    	   return true;
       }
       return false;
   }
   
   public boolean containsEdge(String startName, String endName) {
	   if (!containsVertex(startName)) return false;
	   if (!containsVertex(endName)) return false;
	   
       if (graph.get(startName).neighbours != null && graph.get(startName).neighbours.containsKey(graph.get(endName))
    		   && graph.get(endName).neighbours != null && graph.get(endName).neighbours.containsKey(graph.get(startName))) {
    	   return true;
       }
       return false;
   }
 
   /** Runs dijkstra using a specified source vertex */ 
   public double dijkstra(String startName, String endName) {
      final Vertex source = graph.get(startName);
      NavigableSet<Vertex> q = new TreeSet<>();
 
      // set-up vertices
      for (Vertex v : graph.values()) {
         v.previous = v == source ? source : null;
         v.dist = v == source ? 0 : Double.MAX_VALUE;
         q.add(v);
      }
 
      return dijkstra(q, endName);
   }
 
   /** Implementation of dijkstra's algorithm using a binary heap. */
   private double dijkstra(final NavigableSet<Vertex> q, String endName) {      
      Vertex u = null, v = null;
      while (!q.isEmpty()) {
 
         u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
         if (u.dist == Double.MAX_VALUE) break; // we can ignore u (and any other remaining vertices) since they are unreachable
 
         //look at distances to each neighbour
         for (Map.Entry<Vertex, Double> a : u.neighbours.entrySet()) {
            v = a.getKey(); //the neighbour in this iteration
 
            final double alternateDist = u.dist + a.getValue();
            if (alternateDist < v.dist) { // shorter path to neighbour found
               q.remove(v);
               v.dist = alternateDist;
               v.previous = u;
               q.add(v);
            } else if (alternateDist == v.dist && v.previous.compareTo(v) < 0) {
            	//Make the previous v replace the current v in the set
            	q.remove(v);
                v.previous = v;
                q.add(v);
            } else if (alternateDist == v.dist && v.previous.compareTo(v) > 0) {
            	//Do Nothing Here
            }
         }
      }
      
      if (graph.get(endName) == null) {
    	  return -1.0;
      }
      return graph.get(endName).dist;
   }

   /** Prints a path from the source to the specified vertex */
   public LinkedList<String> getPath(String endName) {
	  Vertex.path.clear();
      graph.get(endName).printPath();
      if (Vertex.path.size() == 1) {
    	  Vertex.path.clear();
      } 
      return Vertex.path;
   }
}