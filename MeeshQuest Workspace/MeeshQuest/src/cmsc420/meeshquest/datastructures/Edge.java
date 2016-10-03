package cmsc420.meeshquest.datastructures;

/**The Edge class provides getter and setter methods for the 
 * cost of an edge, the source vertex of an edge and the destination
 * vertex of an edge.
 * 
 * @author Mikhail Sorokin
 *
 * @param <V>
 */
public class Edge <V extends Comparable<V>> {

  //These variables are all of the components that comprise
  //an Edge object.
  private V source, dest;
  private int weight;
  
  public Edge(V source, V dest) {
    this.source = source;
    this.dest = dest;
  }
  
  public Edge(V source, V dest, int cost) {
    this.source = source;
    this.dest = dest;
    weight = cost;
  }

  public int getWeight() {
    return weight;
  }
  
  public V getSource() {
    return source;
  }

  public V getDest() {
    return dest;
  }
  
  public void setSource(V source) {
    this.source = source;
  }

  public void setDest(V dest) {
    this.dest = dest;
  }

  public void setCost(int weight) {
    this.weight = weight;
  }

  
}
