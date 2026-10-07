import java.util.ArrayList;
import java.util.List;

/** Shared graph node; labels may repeat, so algorithms must key by identity. */
public final class GraphNode {
    public int value;
    public final List<GraphNode> neighbors = new ArrayList<>();

    public GraphNode(int value) {
        this.value = value;
    }
}
